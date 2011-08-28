package com.github.turmericbot;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class TurmericProperties {
	public static final String PROPERTY_FILE = "turmeric.properties";
	
	public static final String SERVER = "turmeric.server";
	public static final String CHANNEL = "turmeric.channel";
	public static final String BOT_NAME = "turmeric.botname";
	
	public static final String JIRA_URL = "turmeric.jira.url";
	public static final String JIRA_PREFIX = "turmeric.jira.prefix";
	
	private static Properties p = new Properties();
	
	public static Properties getProperties(boolean forceReload) {
		// Stored properties
		if (!p.isEmpty() && !forceReload) return p;
		
		// If properties exist, load them, otherwise load default
		if (new File(PROPERTY_FILE).exists()) {
			loadProperties();
			return p;
		} else {
			p.clear();
			
			// Keeping old properties by default as an homage to the creator :-)
			p.setProperty(SERVER, "irc.freenode.net");
			p.setProperty(CHANNEL, "#turmeric-dev");
			p.setProperty(BOT_NAME, "turmeric-bot2");
			p.setProperty(JIRA_URL, "https://www.ebayopensource.org/jira/si/jira.issueviews:issue-xml/");
			p.setProperty(JIRA_PREFIX, "TURMERIC");
			
			return p;
		}
	}
	
	public static Properties getProperties() {
		return getProperties(false);
	}
	
	private static void loadProperties() {
		// Read properties file.
		p.clear();
		try {
		    p.load(new FileInputStream(PROPERTY_FILE));
		} catch (IOException e) {
			p = null;
		}
	}
}
