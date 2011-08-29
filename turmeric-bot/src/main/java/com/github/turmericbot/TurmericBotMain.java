package com.github.turmericbot;

import java.util.Properties;

public class TurmericBotMain {

	
	
	public static void main(String[] args) throws Exception {
		Properties p = TurmericProperties.getProperties();

		TurmericBot bot = TurmericBot.getInstance();
		
		bot.setVerbose(true);
		bot.connect(p.getProperty(TurmericProperties.SERVER));
		
		String[] channels = p.getProperty(TurmericProperties.CHANNEL).split(";");
		
		for(String channel : channels) {
			bot.joinChannel(channel);
		}
	}
}
