package com.github.turmericbot;

public class TurmericBotMain {

	public static void main(String[] args) throws Exception {
		TurmericBot bot = TurmericBot.getInstance();
		
		bot.setVerbose(true);
		bot.connect("irc.freenode.net");
		bot.joinChannel("#turmeric-dev");
	}
		
	
}
