package com.github.turmericbot.jobs;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import com.github.turmericbot.TurmericBot;

public class HelloJob implements Job {

	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobDetail jobdetail = context.getJobDetail();
		JobDataMap dataMap = jobdetail.getJobDataMap();

		TurmericBot bot = TurmericBot.getInstance();
		String channel = dataMap.getString("channel");
		bot.sendAction(channel, "saying hello world through a quartz scheduled job.");
	}

}
