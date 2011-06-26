package com.github.turmericbot.jobs;

import static org.quartz.DateBuilder.futureDate;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

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
 * This class is used to process a weather command.  It takes in as parameters from the
 * JobDetail the airport code and the channel that sent the request.
 * 
 * It handles all processing and formatting of the weather report.  All API calls make
 * use of the Weather Undeground's REST XML web services.
 * 
 * @author dcarver
 *
 */
public class GetJiraIssueJob extends AbstractJob implements Job {

	private static final String JIRA_ISSUE_URL = "https://www.ebayopensource.org/jira/si/jira.issueviews:issue-xml/TURMERIC-";

	private TurmericBot bot = null;
	private String channel = null;

	public static void scheduleJob(String channel, String message,
			Scheduler scheduler) {
		String issueNo = message.substring("jira".length() + 1);
		JobDetail jobDetail = newJob(GetJiraIssueJob.class).withIdentity("get_jira_issue")
				.usingJobData("channel", channel)
				.usingJobData("issue", issueNo).build();

		Trigger trigger = newTrigger().withIdentity("get_jira_trigger")
				.startAt(futureDate(20, IntervalUnit.MILLISECOND)).build();

		try {
			scheduler.scheduleJob(jobDetail, trigger);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}

	}

	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		JobDataMap dataMap = getDataMap(context);
		bot = TurmericBot.getInstance();
		String issueNo = dataMap.getString("issue");
		channel = dataMap.getString("channel");
		processJiraRequest(issueNo);
	}

	private void processJiraRequest(String issueNo) {
		InputStream restXML = null;
		try {
			restXML = retrieveURL(issueNo);
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

			Node rss = doc.getRootElement();

			String summary = jiraSummary(rss);

			bot.sendMessage(channel, summary);

		} catch (DocumentException e) {
			sendErrorMessage(channel, e.getMessage());
		}
	}

	private String jiraSummary(Node rss) {
		String title = rss.valueOf("channel/item/title");
		String status = rss.valueOf("channel/item/status");
		String link = rss.valueOf("channel/item/link");
		
		String summary = status + " - " + title + " - " + link;
		return summary;
	}

	public InputStream retrieveURL(String issueNo)
			throws MalformedURLException, IOException {
		String url = JIRA_ISSUE_URL + issueNo + "/TURMERIC-" + issueNo + ".xml";
		URLConnection conn = new URL(url).openConnection();
		return conn.getInputStream();
	}

}
