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
 * This class is used to process a weather command.  It takes in as parameters from the
 * JobDetail the airport code and the channel that sent the request.
 * 
 * It handles all processing and formatting of the weather report.  All API calls make
 * use of the Weather Undeground's REST XML web services.
 * 
 * @author dcarver
 *
 */
public class WeatherJob extends AbstractJob implements Job {

	private static final String WEATHER_REST_URL = "http://api.wunderground.com/auto/wui/geo/WXCurrentObXML/index.xml?query=";

	private TurmericBot bot = null;
	private String channel = null;

	public static void scheduleJob(String channel, String message,
			Scheduler scheduler) {
		String airportCode = message.substring("weather".length() + 1);
		JobDetail jobDetail = newJob(WeatherJob.class).withIdentity("weather")
				.usingJobData("channel", channel)
				.usingJobData("airportCode", airportCode).build();

		Trigger trigger = newTrigger().withIdentity("weathertrigger")
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
		String airPortCode = dataMap.getString("airportCode");
		channel = dataMap.getString("channel");
		processWeather(airPortCode);
	}

	private void processWeather(String airPortCode) {
		InputStream restXML = null;
		try {
			restXML = retrieveURL(airPortCode);
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

			Node observation = doc.getRootElement();

			String observations = currentConditions(observation);

			bot.sendMessage(channel, observations);

		} catch (DocumentException e) {
			sendErrorMessage(channel, e.getMessage());
		}
	}

	private String currentConditions(Node observation) {
		String city = observation.valueOf("observation_location/full");
		String country = observation.valueOf("observation_location/country");
		String weather = observation.valueOf("weather");
		String temperature = observation.valueOf("temperature_string");
		String humidty = observation.valueOf("relative_humidity");
		String wind_string = observation.valueOf("wind_string");
		String wind_chill = observation.valueOf("windchill_string");

		String observations = "Current conditions for " + city + " in "
				+ country + ". " + weather + ", " + temperature
				+ ", relative humidty " + humidty + ".  Wind is " + wind_string
				+ " making it feel like " + wind_chill;
		return observations;
	}

	public InputStream retrieveURL(String airportCode)
			throws MalformedURLException, IOException {
		String url = WEATHER_REST_URL + airportCode;
		URLConnection conn = new URL(url).openConnection();
		return conn.getInputStream();
	}

}
