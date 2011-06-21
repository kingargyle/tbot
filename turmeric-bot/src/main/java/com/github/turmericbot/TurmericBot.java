package com.github.turmericbot;

import org.jibble.pircbot.PircBot;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import com.github.turmericbot.jobs.ForecastJob;
import com.github.turmericbot.jobs.HelloJob;
import com.github.turmericbot.jobs.WeatherJob;

public class TurmericBot extends PircBot {
	
	private Scheduler scheduler = null;
	
	private static TurmericBot bot = new TurmericBot();
	
	
	private TurmericBot() {
		setName("turmeric-bot2");
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
	}
	
	@Override
	protected void onQuit(String sourceNick, String login, String hostName, String reasn) {
		sendMessage("#turmeric-dev", "Bye, bye " + sourceNick + " come back soon.");
	}
	
	@Override
	protected void onJoin(String channel, String sender, String login, String hostname) {
		if (sender.equals(getName())) {
			sendMessage(channel,  "Reporting for duty!");
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
	
}
