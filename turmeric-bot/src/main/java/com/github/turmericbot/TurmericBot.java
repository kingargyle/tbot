package com.github.turmericbot;

import java.util.Properties;

import org.jibble.pircbot.PircBot;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import com.github.turmericbot.jobs.ForecastJob;
import com.github.turmericbot.jobs.GetJiraIssueJob;
import com.github.turmericbot.jobs.HelloJob;
import com.github.turmericbot.jobs.HelpJob;
import com.github.turmericbot.jobs.HudsonJobStatusJob;
import com.github.turmericbot.jobs.WeatherJob;
import com.github.turmericbot.jobs.background.ForumPostsJob;
import com.github.turmericbot.jobs.background.HudsonStatusJob;

public class TurmericBot extends PircBot {
	
	private Scheduler scheduler = null;
	
	private static TurmericBot bot = new TurmericBot();
	
	private String botName;
	private String botChannel;
	private String jiraURL;
	private String jiraPrefix;
	
	private TurmericBot() {
		Properties p = TurmericProperties.getProperties();
		botName = p.getProperty(TurmericProperties.BOT_NAME);
		botChannel = p.getProperty(TurmericProperties.CHANNEL);
		jiraURL = p.getProperty(TurmericProperties.JIRA_URL);
		jiraPrefix = p.getProperty(TurmericProperties.JIRA_PREFIX);
		
		setName(botName);
	}
	
	public static TurmericBot getInstance() {
		return bot;
	}
	
	@Override
	public void onMessage(String channel, String sender, String login,
			String hostname, String message) {
		if (message.equals("quartz")) {
			HelloJob.scheduleJob(channel, message, scheduler);
			return;
		}
		
		if (message.equalsIgnoreCase("time")) {
			String time = new java.util.Date().toString();
			sendMessage(channel, sender + ": The time is now " + time);
			return;
		}
		
		if (message.startsWith("weather")) {
			WeatherJob.scheduleJob(channel, message, scheduler);
			return;
		}
		
		if (message.startsWith("forecast")) {
			ForecastJob.scheduleJob(channel, message, scheduler);
			return;
		}
		
		if (message.startsWith("jira")) {
			GetJiraIssueJob.scheduleJob(channel, message, jiraURL, jiraPrefix, scheduler);
			return;
		}
		
		if (message.equals("help")) {
			HelpJob.scheduleJob(channel, message, scheduler);
			return;
		}
		
		if (message.startsWith("hudson")) {
			HudsonJobStatusJob.scheduleJob(channel, message, scheduler);
			return;
		}
	}
	
	@Override
	protected void onQuit(String sourceNick, String login, String hostName, String reasn) {
		sendMessage(botChannel, "Bye, bye " + sourceNick + " come back soon.");
	}
	
	@Override
	protected void onJoin(String channel, String sender, String login, String hostname) {
		if (sender.equals(getName())) {
			sendMessage(channel,  "Reporting for duty!");
			HudsonStatusJob.scheduleJob(channel, null, scheduler);
			ForumPostsJob.scheduleJob(channel, null, scheduler);
			return;
		}
		
		this.sendAction(channel, " tigger pounces on " + sender + ".");
	}
	
	@Override
	protected void onConnect() {
		super.onConnect();
		try {
			scheduler = StdSchedulerFactory.getDefaultScheduler();
			scheduler.start();
			
		} catch (SchedulerException ex) {
			
		}
	}
	
	@Override
	protected void onDisconnect() {
		super.onDisconnect();
		try {
			scheduler.shutdown();
		} catch (SchedulerException ex) {
			
		}
	}
	
	@Override
	protected void onAction(String sender, String login, String hostname, String target,
			String action) {
		String channel = botChannel;
		
		if (sender.equals(getName())) {
			return;
		}
				
		if (action.contains("trout")) {
				sendAction(channel, "grabs the trout and tosses it back in the water.");
				sendMessage(channel, sender + ", TROUTS HAVE FEELINGS YOU KNOW!!!");
				sendMessage(channel,"FREE THE TROUTS!!! FREE THE TROUTS!!!");
		}
	}
	
}
