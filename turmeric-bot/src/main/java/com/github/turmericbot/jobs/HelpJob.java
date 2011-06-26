package com.github.turmericbot.jobs;

import static org.quartz.DateBuilder.futureDate;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.DateBuilder.IntervalUnit;

import com.github.turmericbot.TurmericBot;

public class HelpJob implements Job {

	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobDetail jobdetail = context.getJobDetail();
		JobDataMap dataMap = jobdetail.getJobDataMap();

		TurmericBot bot = TurmericBot.getInstance();
		String channel = dataMap.getString("channel");
		bot.sendMessage(channel, "Turmeric Bot 2...stronger...faster...and smarter than Turmeric Bot. I understand the following commands:");
		bot.sendMessage(channel, "1. help - displays this message.");
		bot.sendMessage(channel, "2. forecast <param> - replace <param> with the airport code or city.  If using city use the following format: city,state. I'll tell you the current/future forecast for your area.");
		bot.sendMessage(channel, "3. weather <param> - replace <param> with the airport code to get weather conditions for that airport.");
		bot.sendMessage(channel, "4. jira <param> - param is the issue number.  To retrieve turmeric issue number 1261, jira 1261");
		bot.sendMessage(channel, "5. quartz - test job for testing the quartz scheduler.");
		bot.sendMessage(channel, "6. hudson <param> - param is the build number to get status updates on.  i.e: hudson #909");
		bot.sendMessage(channel, "In addition, I monitor the Hudson build server and will report the status of any updated jobs for the Turmeric CI dashboard. These notifications are automaticly sent to the channel");
	}
	
	public static void scheduleJob(String channel, String message, Scheduler scheduler) {
		JobDetail jobDetail = newJob(HelpJob.class).withIdentity("help")
		.usingJobData("channel", channel)
		.build();
				
		Trigger trigger = newTrigger()
				.withIdentity("helptrigger")
				.startAt(futureDate(5, IntervalUnit.MILLISECOND))
				.build();
		try {
			scheduler.scheduleJob(jobDetail, trigger);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
		return;

		
	}

}
