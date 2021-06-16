package com.exchangelogger;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GrandExchangeOfferChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = "Exchange Logger",
	description = "Stores all GE transactions in a log file"
)
public class ExchangeLoggerPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ExchangeLoggerConfig config;

	public static final String CONFIG_GROUP = "exchangelogger";
	private final String fileType = ".log";
	private boolean rewrite;
	public String logPath;
	private ExchangeLoggerWriter writer;

	@Override
	protected void startUp() throws Exception
	{
		rewrite = config.oneFile(); //Add to construct
		logPath = config.filePath();

		writer = new ExchangeLoggerWriter(logPath);
/*
		System.out.println("Creating file obj");
		File file = new File("test\\temp.txt");
		System.out.println("File exist: " + file.isFile());
		System.out.println("File name: " + file.getName());
		file.renameTo(new File("test\\bajs.txt"));
		System.out.println("File path: " + file.toString());*/
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("ExchangeLogger stopped!");
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (event.getGroup().equals(CONFIG_GROUP))
		{
			if (event.getKey().equals("filePath"))
			{
				System.out.println("Config changed: filepath");
				if (!logPath.equals(config.filePath()))	//File path changed
				{
					logPath = config.filePath();
					writer.changePath(logPath);					//remove old log and create a new one
				}	//handle path typo? \\ etc
			}
			else if (event.getKey().equals("rewriteLog"))
			{	//Delete all old data when logging in. Only uses one log file

			}	//Should be changed to ONE log file, resets on startup
		}
	}

	@Subscribe
	public void onGrandExchangeOfferChanged(GrandExchangeOfferChanged offerEvent)
	{
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			// Trades are cleared by the client during LOGIN_SCREEN/HOPPING/LOGGING_IN, ignore those
			writer.GrandExchangeEvent(offerEvent);
		}
	}

	@Provides
	ExchangeLoggerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ExchangeLoggerConfig.class);
	}
}
