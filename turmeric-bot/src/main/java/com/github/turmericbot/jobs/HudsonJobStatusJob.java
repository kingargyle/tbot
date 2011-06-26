package com.github.turmericbot.jobs;

import static org.quartz.DateBuilder.futureDate;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.DateBuilder.IntervalUnit;
import com.github.turmericbot.AbstractJob;
import com.github.turmericbot.TurmericBot;

/**
 * This class polls the Hudson build server's RSS feeds. It looks for all jobs that match
 * the build number that is passed to it.
 * 
 * @author dcarver
 * 
 */
public class HudsonJobStatusJob extends AbstractJob implements Job {

	private static final String HUDSON_STATUS_URL = "http://www.ebayopensource.org/hudson/view/Turmeric%20CI%20Dashboard/rssLatest";
	private XMLGregorianCalendar lastcheck;

	private TurmericBot bot = null;
	private String channel = null;
	private String buildno = null;

	public static void scheduleJob(String channel, String message,
			Scheduler scheduler) {

		String buildno = message.substring("hudson".length() + 1);

		try {
			JobDetail jobDetail = newJob(HudsonJobStatusJob.class)
					.withIdentity("hudson_build_status" + Math.random())
					.usingJobData("channel", channel)
					.usingJobData("buildno", buildno).build();

			Trigger trigger = newTrigger()
					.withIdentity("hudson_build_status" + Math.random())
					.startAt(futureDate(5, IntervalUnit.MILLISECOND)).build();

			scheduler.scheduleJob(jobDetail, trigger);
		} catch (SchedulerException e) {

		}

	}

	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		JobDataMap dataMap = getDataMap(context);
		bot = TurmericBot.getInstance();
		channel = dataMap.getString("channel");
		buildno = dataMap.getString("buildno");

		// bot.sendMessage(channel, "Checking Hudson Status");
		processHudsonStatus();
		// bot.sendMessage(channel, "Going back to sleep.");
	}

	private void processHudsonStatus() {
		InputStream restXML = null;
		try {
			restXML = retrieveURL();
			SAXReader reader = createXmlReader();
			sendCurrentConditions(restXML, reader);
		} catch (MalformedURLException ex) {
			sendErrorMessage(channel, ex.getMessage());
		} catch (IOException ex) {
			sendErrorMessage(channel, ex.getMessage());
		} finally {
			if (restXML != null) {
				try {
					restXML.close();
				} catch (IOException e) {

				}
			}
		}
	}

	private void sendCurrentConditions(InputStream restXML, SAXReader reader) {
		try {
			Document doc = reader.read(restXML);

			Node feed = doc.getRootElement();

			List<Node> nodes = feed.selectNodes("a:entry[contains(a:title, '"
					+ buildno + "')] ");
			
			if (nodes.isEmpty()) {
				bot.sendMessage(channel, "Unable to locate a job for build number " + buildno);
				return;
			}

			for (Node entryNode : nodes) {
				String title = entryNode.valueOf("a:title");
				title = title.replace("?", "running");

				String link = entryNode.valueOf("a:link/@href");
				String summary = title + " - " + link;
				bot.sendMessage(channel, "Job Status for " + summary);
			}
		} catch (DocumentException e) {
			sendErrorMessage(channel, e.getMessage());
		}
	}

	private InputStream retrieveURL() throws MalformedURLException, IOException {
		String url = HUDSON_STATUS_URL;
		URLConnection conn = new URL(url).openConnection();
		return conn.getInputStream();
	}

	@Override
	protected InputStream retrieveURL(String param)
			throws MalformedURLException, IOException {
		return retrieveURL();
	}

	@Override
	protected SAXReader createXmlReader() {
		DocumentFactory factory = new DocumentFactory();
		Map<String, String> uris = new ConcurrentHashMap<String, String>();
		uris.put("a", "http://www.w3.org/2005/Atom");
		factory.setXPathNamespaceURIs(uris);

		SAXReader xmlReader = new SAXReader();
		xmlReader.setDocumentFactory(factory);
		return xmlReader;
	}
}
