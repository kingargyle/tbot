package com.github.turmericbot.jobs;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

import com.github.turmericbot.TurmericBot;

public class WeatherJob implements Job {

	private static final String WEATHER_REST_URL = "http://api.wunderground.com/auto/wui/geo/WXCurrentObXML/index.xml?query=";
	

	private TurmericBot bot = null;
	private String channel = null;

	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		JobDetail jobdetail = context.getJobDetail();
		JobDataMap dataMap = jobdetail.getJobDataMap();

		bot = TurmericBot.getInstance();

		String airPortCode = dataMap.getString("airportCode");
		channel = dataMap.getString("channel");
		InputStream restXML = null;
		try {
			restXML = retrieveURL(airPortCode);
			SAXReader reader = createXmlReader();
			
			try {
				Document doc = reader.read(restXML);
				
				Node observation = doc.getRootElement();
				
				String city = observation.valueOf("observation_location/full");
				String country = observation.valueOf("observation_location/country");
				String weather = observation.valueOf("weather");
				String temperature = observation.valueOf("temperature_string");
				String humidty = observation.valueOf("relative_humidity");
				String wind_string = observation.valueOf("wind_string");
				String wind_chill = observation.valueOf("windchill_string");
				
				String observations = "Current conditions for " + city + " in " + country + ". " + weather + ", " + temperature + ", relative humidty " +
				   humidty + ".  Wind is " + wind_string + " making it feel like " + wind_chill;
				
				bot.sendMessage(channel, observations);
				
			} catch (DocumentException e) {
				sendErrorMessage(channel, e.getMessage());
			}

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

	public InputStream retrieveURL(String airportCode)
			throws MalformedURLException, IOException {
		String url = WEATHER_REST_URL + airportCode;
		URLConnection conn = new URL(url).openConnection();
		return conn.getInputStream();
	}

	private void sendErrorMessage(String channel, String errorMessage) {
		bot.sendMessage(channel, errorMessage);
	}

	private SAXReader createXmlReader() {
		DocumentFactory factory = new DocumentFactory();

		SAXReader xmlReader = new SAXReader();
		xmlReader.setDocumentFactory(factory);
		return xmlReader;
	}

}
