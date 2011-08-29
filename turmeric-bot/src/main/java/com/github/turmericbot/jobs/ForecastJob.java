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
 * This class is used to retrieve a weather forecast.  It takes in as parameters from the
 * JobDetail the airport code and the channel that sent the request.
 * 
 * It handles all processing and formatting of the weather report.  All API calls make
 * use of the Weather Undeground's REST XML web services.
 * 
 * @author dcarver
 *
 */
public class ForecastJob extends AbstractJob implements Job {

	private static final String WEATHER_REST_URL = "http://api.wunderground.com/auto/wui/geo/ForecastXML/index.xml?query=";

	public static void scheduleJob(String channel, String message,
			Scheduler scheduler) {
		String airportCode = message.substring("forecast".length() + 1);
		JobDetail jobDetail = newJob(ForecastJob.class).withIdentity("forecast")
				.usingJobData("channel", channel)
				.usingJobData("airportCode", airportCode).build();

		Trigger trigger = newTrigger().withIdentity("forecasttrigger")
				.startAt(futureDate(1, IntervalUnit.SECOND)).build();

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
		String location = dataMap.getString("airportCode");
		channel = dataMap.getString("channel");
		processForecast(location);
	}

	private void processForecast(String location) {
		InputStream restXML = null;
		try {
			restXML = retrieveURL(location);
			SAXReader reader = createXmlReader();
			sendForecast(restXML, reader);
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


	private void sendForecast(InputStream restXML, SAXReader reader) {
		try {
			Document doc = reader.read(restXML);

			Node forecast = doc.getRootElement();

			sayPrediction(forecast);

		} catch (DocumentException e) {
			sendErrorMessage(channel, e.getMessage());
		}
	}

	private void sayPrediction(Node forecast) {
		String today = forecast.valueOf("txt_forecast/forecastday[1]/fcttext");
		String tonight = forecast.valueOf("txt_forecast/forecastday[2]/fcttext");
		String title1 = forecast.valueOf("txt_forecast/forecastday[1]/title");
		String title2 = forecast.valueOf("txt_forecast/forecastday[2]/title");
		
		bot.sendMessage(channel, title1 + ": " + today);
		bot.sendMessage(channel, title2 + ": " + tonight);
		
	}

	@Override
	public InputStream retrieveURL(String airportCode)
			throws MalformedURLException, IOException {
		String url = WEATHER_REST_URL + airportCode;
		URLConnection conn = new URL(url).openConnection();
		return conn.getInputStream();
	}


}
