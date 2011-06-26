package com.github.turmericbot;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import org.dom4j.DocumentFactory;
import org.dom4j.io.SAXReader;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;

public abstract class AbstractJob implements Job {

	protected TurmericBot bot = null;
	protected String channel = null;

	protected void sendErrorMessage(String channel, String errorMessage) {
		bot.sendMessage(channel, errorMessage);
	}

	protected abstract InputStream retrieveURL(String param) throws MalformedURLException, IOException;
	
	protected SAXReader createXmlReader() {
		DocumentFactory factory = new DocumentFactory();

		SAXReader xmlReader = new SAXReader();
		xmlReader.setDocumentFactory(factory);
		return xmlReader;
	}
	
	protected JobDataMap getDataMap(JobExecutionContext context) {
		JobDetail jobdetail = context.getJobDetail();
		JobDataMap dataMap = jobdetail.getJobDataMap();
		return dataMap;
	}
	

}
