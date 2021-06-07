package com.exchangelogger;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GrandExchangeOfferChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
	private String fileType = ".log";
	private boolean rewrite;
	public String path;
	private File file;

	@Override
	protected void startUp() throws Exception
	{
		rewrite = config.rewriteLog();
		path = config.filePath();

		file = new File(path);
		if(!file.isFile()){
			createLog(path);
		}

		//
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Example stopped!");
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event) {
		if (event.getGroup().equals(CONFIG_GROUP)) {
			if (event.getKey().equals("filePath")) {
				System.out.println("Config changed in filepath");
				if(!path.equals(config.filePath())){	//File path changed
					changeLog(path);					//remove old log and create a new one
				}
			}
			else if(event.getKey().equals("rewriteLog")){	//Delete all old data when logging in. Only uses one log file

			}
		}
	}

	/**
	 * Based on github.com/runelite/runelite/commits/master/runelite-client/src/main/java/net/runelite/client/plugins/grandexchange
	 */
	@Subscribe
	public void onGrandExchangeOfferChanged(GrandExchangeOfferChanged offerEvent){
		final int slot = offerEvent.getSlot();
		final GrandExchangeOffer offer = offerEvent.getOffer();

		if (offer.getState() == GrandExchangeOfferState.EMPTY && client.getGameState() != GameState.LOGGED_IN)
		{
			// Trades are cleared by the client during LOGIN_SCREEN/HOPPING/LOGGING_IN, ignore those
			return;
		}
		Date date = new Date();

		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");	//"dd-MM-yyyy HH:mm:ss"
		String s = formatter.format(date);

		//System.out.println("GE offer updated: state: {}, slot: {}, item: {}, qty: {}, time: {}",
		//	offer.getState(), slot, offer.getItemId(), offer.getQuantitySold(), s);

		System.out.println("GE offer updated: state: " + offer.getState() + " slot: " + slot + " item: " + offer.getItemId() +
				" qty: " + offer.getQuantitySold() + " time: " + s);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			//client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Example says " + config.greeting(), null);
		}
	}

	@Provides
	ExchangeLoggerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ExchangeLoggerConfig.class);
	}

	private void createLog(String path){
		if(path.endsWith(fileType)) {
			try {
				File log = new File(path);
				if (log.createNewFile()) {
					file = log;
					System.out.println("New log file created: " + log.getName());	//log.info()
				} else {
					System.out.println("Create log -> file already exists.");

				}
			} catch (IOException e) {
				System.out.println("An error occurred while creating a new log file");
				e.printStackTrace();
			}
		}
		else{
			System.out.println("An error occurred: log file paths must end with .log");
		}
	}

	//Removes old log and creates a log at the new location. Does NOT move the file
	private void changeLog(String oldPath){
			File log = new File(oldPath);
			if (log.delete()) {
				System.out.println("New log path -> deleted the previous log file: " + log.getName());
			} else {
				System.out.println("New log path -> failed to delete the previous log file: " + log.getName());
			}
			path = config.filePath();
			createLog(path);
	}
}
