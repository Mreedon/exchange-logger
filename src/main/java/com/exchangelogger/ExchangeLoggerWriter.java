package com.exchangelogger;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GrandExchangeOffer;
import net.runelite.api.GrandExchangeOfferState;
import net.runelite.api.events.GrandExchangeOfferChanged;
import java.io.FileWriter;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;
import static net.runelite.api.GrandExchangeOfferState.BUYING;
import static net.runelite.api.GrandExchangeOfferState.CANCELLED_BUY;
import static net.runelite.api.GrandExchangeOfferState.CANCELLED_SELL;
import static net.runelite.api.GrandExchangeOfferState.EMPTY;
import static net.runelite.api.GrandExchangeOfferState.SELLING;

@Slf4j
public class ExchangeLoggerWriter
{
	private File logFile;
	private final String fileType;
	private boolean fileExist;              //Plugin won't write anything if the file doesn't exist
	private final int[] prevQuantity;             //Previous quantity for each GE slot event, needed to remove duplicates
	private final GrandExchangeOfferState[] prevState;
	private String logPath;
	private String logDate;
	private final Formatting formatting;

	ExchangeLoggerWriter(String path)
	{
		fileType = ".log";
		fileExist = true;
		logPath = path;
		logDate = currentDateTime("yyyy-MM-dd");    //Date for the current log, new log at 00:00
		formatting = new Formatting();

		prevQuantity = new int[8];
		prevState = new GrandExchangeOfferState[8];
		Arrays.fill(prevQuantity, -1);          //Default to -1, because 0 is a valid state


		logFile = new File(logPath);
		if (logFile.isFile())
		{
			System.out.println("Is File");
			//If user wants a file for each day
			fileDateCheck();    //Check if last log is for today's date
		}
		else
		{
			logFile = createLog(logPath);       //First time running plugin or new path in config
		}
	}

	public void GrandExchangeEvent(GrandExchangeOfferChanged event)
	{
		String time = currentDateTime("yyyy-MM-dd HH:mm:ss");

		if (!fileExist)
		{
			return;
		}
		else if (!logDate.equals(time.substring(0, logDate.length())))  //New log if date changed during run-time
		{
			preserveCurrentFile(logDate);
		}

		GrandExchangeOffer offer = event.getOffer();
		int slot = event.getSlot();

		if (duplicateHandler(offer, slot))         //Filter out duplicated events
		{
			return;
		}
		writeFile(offer, slot, time);
	}

	private void writeFile(GrandExchangeOffer offer, int slot, String time)
	{
		String writeLine;
		try
		{
			FileWriter writer = new FileWriter(logFile, true);

			if (true)
			{
				writeLine = formatting.plainText(offer, slot, time);

			}
			else if (true)
			{
				writeLine = formatting.tabular(offer, slot, time);
			}

			writer.write(writeLine + "\n");
			writer.flush();
			writer.close();

		}
		catch (IOException e)
		{
			log.info("An error occurred while writing to log file: " + e.toString());
		}
	}

	//GE OfferChanged events sometimes send duplicates of buying,selling and cancelled..
	//This method will compare current event with the previous.
	// 2 buying/selling events in sequence in the same slot can't have the same QuantitySold
	// 2 cancelled_buy/sell events in sequence in the same slot shouldn't be possible
	private boolean duplicateHandler(GrandExchangeOffer offer, int slot)
	{
		boolean duplicate = false;

		if ((prevQuantity[slot] == offer.getQuantitySold() && formatting.anyEqualState(offer.getState(), BUYING, SELLING))
				|| (prevState[slot] == offer.getState() && formatting.anyEqualState(offer.getState(), CANCELLED_BUY, CANCELLED_SELL)))
		{
			duplicate = true;
		}
		else    //EMPTY is always qty = 0, which makes next buy/sell assume it's a duplicate. Set it to -1
		{
			prevQuantity[slot] = ((offer.getState() == EMPTY) ? -1 : offer.getQuantitySold());
			prevState[slot] = offer.getState();
		}
		return duplicate;
	}

	private String currentDateTime(String form)
	{
		Date date = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat(form);   //"yyyy-MM-dd HH:mm:ss"
		return formatter.format(date);
	}

	//Adding _[fileDate] at the end of the current file name and creates a new log
	private void preserveCurrentFile(String fileDate)    //preserve file rename:exchange._time: 2021.log
	{
		int split = logFile.toString().lastIndexOf(".");
		String rename = logFile.toString().substring(0, split);
		rename = rename + "_" + fileDate + fileType;

		System.out.println("preserve file rename: " + rename);
		if (!logFile.renameTo(new File(rename)))
		{
			System.out.println("Failed to rename previous file to: " + rename);
		}
		logFile = createLog(logPath);
		//System.out.println("logfile path " + logFile.toString());
	}

	//If current date is not the same as in the active log file on start, store it and create a new one
	private void fileDateCheck()
	{
		String fileDate = "";
		System.out.println("fileDateCheck");

		try
		{
			Scanner reader = new Scanner(logFile);
			if (reader.hasNextLine())
			{
				fileDate = reader.nextLine().substring(0, logDate.length());
			}
			System.out.println("fileDateCheck, fileDate: " + fileDate);
			reader.close();
		}
		catch (IOException e)
		{
			System.out.println("An error occurred -> could not read file: " + logFile.getName());
			log.info(e.toString());
		}

		if (!fileDate.equals(currentDateTime("yyyy-MM-dd")) && !fileDate.equals(""))
		{
			System.out.println("preserve current file");
			preserveCurrentFile(fileDate);
		}
	}

	private File createLog(String path)
	{
		logDate = currentDateTime("yyyy-MM-dd");
		if (path.endsWith(fileType))
		{
			try
			{
				File log = new File(path);
				if (log.createNewFile())
				{
					System.out.println("New log file created: " + log.getName());	//log.info()
					fileExist = true;
					return log;
				}
				else
				{
					System.out.println("Attempted to create log: file already exists.");
				}
			}
			catch (IOException e)
			{
				System.out.println("An error occurred while creating a new log file");
				log.info(e.toString());	//log.debug?
			}
		}
		else
			{
			System.out.println("An error occurred: log file path must end with .log");
		}
		fileExist = false;
		return null;
	}

	//Removes old log and creates a log at the new location. Does NOT move the file
	public void changePath(String newPath)
	{
		try
		{
			if (logFile.delete())
			{
				System.out.println("New log path -> deleted the previous log file: " + logFile.getName());
			}
			else
			{
				System.out.println("New log path -> failed to delete the previous log file: " + logFile.getName());
			}
		}
		catch (Exception e)
		{
			log.info("Error deleting old log file: " + e.toString());
		}
		logPath = newPath;
		logFile = createLog(logPath);
	}
}
