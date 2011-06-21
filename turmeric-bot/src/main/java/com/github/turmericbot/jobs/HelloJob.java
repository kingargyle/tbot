package com.github.turmericbot.jobs;

import static org.quartz.DateBuilder.evenMinuteDate;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.util.Date;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

import com.github.turmericbot.TurmericBot;

public class HelloJob implements Job {

	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobDetail jobdetail = context.getJobDetail();
		JobDataMap dataMap = jobdetail.getJobDataMap();

		TurmericBot bot = TurmericBot.getInstance();
		String channel = dataMap.getString("channel");
		bot.sendAction(channel, "saying hello world through a quartz scheduled job.");
	}
	
	public static void scheduleJob(String channel, String message, Scheduler scheduler) {
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
		return;

		
	}

}
