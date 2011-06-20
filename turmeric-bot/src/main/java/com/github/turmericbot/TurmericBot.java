package com.github.turmericbot;

import java.util.Date;

import org.jibble.pircbot.PircBot;
import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import static org.quartz.DateBuilder.*;

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
	
	public void onMessage(String channel, String sender, String login,
			String hostname, String message) {
		if (message.equals("quartz")) {
			JobDetail jobDetail = newJob(HelloJob.class).withIdentity("helloworld")
			.usingJobData("channel", channel)
			.build();
			
			Date runtime = evenMinuteDate(new Date());
			
			Trigger trigger = newTrigger()
					.withIdentity("helloworldtrigger")
					.startAt(runtime)
					.build();
			
			try {
				scheduler.scheduleJob(jobDetail, trigger);
			} catch (SchedulerException e) {
				e.printStackTrace();
			}
			
		}
		if (message.equalsIgnoreCase("time")) {
			String time = new java.util.Date().toString();

			sendMessage(channel, sender + ": The time is now " + time);
		}
		if (message.startsWith("weather")) {
			String airportCode =  message.substring("weather".length() + 1);
			JobDetail jobDetail = newJob(WeatherJob.class).withIdentity("weather")
			.usingJobData("channel", channel)
			.usingJobData("airportCode", airportCode)
			.build();
						
			Trigger trigger = newTrigger()
					.withIdentity("weathertrigger")
					.startAt(futureDate(1, IntervalUnit.SECOND))
					.build();
			
			try {
				scheduler.scheduleJob(jobDetail, trigger);
			} catch (SchedulerException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	@Override
	protected void onQuit(String sourceNick, String login, String hostName, String reasn) {
		sendMessage("#turmeric-dev", "Bye, bye " + sourceNick + " come back soon.");
	}
	
	@Override
	protected void onJoin(String channel, String sender, String login, String hostname) {
		if (sender.equals(getName())) {
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
