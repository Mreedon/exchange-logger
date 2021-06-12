package com.exchangelogger;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GameState;
import net.runelite.api.GrandExchangeOffer;
import net.runelite.api.GrandExchangeOfferState;
import net.runelite.api.events.GrandExchangeOfferChanged;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static net.runelite.api.GrandExchangeOfferState.BOUGHT;
import static net.runelite.api.GrandExchangeOfferState.BUYING;
import static net.runelite.api.GrandExchangeOfferState.CANCELLED_BUY;
import static net.runelite.api.GrandExchangeOfferState.CANCELLED_SELL;
import static net.runelite.api.GrandExchangeOfferState.EMPTY;
import static net.runelite.api.GrandExchangeOfferState.SELLING;
import static net.runelite.api.GrandExchangeOfferState.SOLD;

@Slf4j
public class ExchangeLoggerWriter {
    private File logFile;
    private final String fileType;
    private boolean fileExist;              //PLugin won't write anything if the file doesn't exist
    private int[] prevQuantity;             //Previous quantity for each GE slot event, needed to remove duplicates
    private GrandExchangeOfferState[] prevState;
    private String logPath;

    ExchangeLoggerWriter(String path){
        fileType = ".log";
        fileExist = true;
        logPath = path;

        prevQuantity = new int[8];
        prevState = new GrandExchangeOfferState[8];
        Arrays.fill(prevQuantity, -1);          //Default to -1, because 0 is a valid state

        logFile = new File(path);
        if(!logFile.isFile()){
            logFile = createLog(path);
        }

    }

    public void GrandExchangeEvent(GrandExchangeOfferChanged event){
        if(fileExist){
            final GrandExchangeOffer offer = event.getOffer();
            final int slot = event.getSlot();

            if(duplicateHandler(offer, slot)){              //Filter out duplicated events
                return;
            }

            //If time = 00 -> new file?
            //Setter and getter for a boolean show date and time
            Date date = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");	//"dd-MM-yyyy HH:mm:ss"
            String time = formatter.format(date);

            writeFile(offer, slot, time);
        }
    }

    //Messy formatting
    private void writeFile(GrandExchangeOffer offer, int slot, String time){
        //First offer for item
        if(offer.getQuantitySold() == 0 && anyEqualState(offer.getState(), BUYING, SELLING)){
            String firstState = ((offer.getState() == BUYING) ? "BUY" : "SELL");    //Differentiate the first offer state from subsequent ones

            System.out.println("time: " + time + " state: " + firstState + " slot: " + slot + " item: " + offer.getItemId() +
                " max: " +  offer.getTotalQuantity() + " offer: " + offer.getPrice());
        }
        else if(anyEqualState(offer.getState(), CANCELLED_BUY, CANCELLED_SELL)){
            System.out.println("time: " + time + " state: " + offer.getState() + " slot: " + slot + " item: " + offer.getItemId() +
                " qty: " + offer.getQuantitySold() + " worth: " + offer.getSpent() + " max: " +  offer.getTotalQuantity());
        }
        else if(offer.getState() == EMPTY){
            System.out.println("time: " + time + " state: " + offer.getState() + " slot: " + slot);
        }
        else{
            System.out.println("time: " + time + " state: " + offer.getState() + " slot: " + slot + " item: " + offer.getItemId() +
                " qty: " + offer.getQuantitySold() + " worth: " + offer.getSpent());
        }
    }

    //GE OfferChanged events sometimes send duplicates of buying,selling and cancelled..
    //This method will compare current event with the previous.
    // 2 buying/selling events in sequence in the same slot can't have the same QuantitySold
    // 2 cancelled_buy/sell events in sequence in the same slot isn't possible

    private boolean duplicateHandler(GrandExchangeOffer offer, int slot){
        boolean duplicate = false;

        if((prevQuantity[slot] == offer.getQuantitySold() && anyEqualState(offer.getState(), BUYING, SELLING))
                || (prevState[slot] == offer.getState() && anyEqualState(offer.getState(), CANCELLED_BUY, CANCELLED_SELL))){
            duplicate = true;
        }
        else{                       //EMPTY is always qty = 0, which makes next buy/sell assume it's a duplicate
            prevQuantity[slot] = ((offer.getState() == EMPTY) ? -1 : offer.getQuantitySold());
            prevState[slot] = offer.getState();
        }
        return duplicate;
    }

    private boolean anyEqualState(GrandExchangeOfferState expected, GrandExchangeOfferState ...array) {
        for(GrandExchangeOfferState state : array){
            if(state.equals(expected)){
                return true;
            }
        }
        return false;
    }


    public File createLog(String path){
        if(path.endsWith(fileType)) {
            try {
                File log = new File(path);
                if (log.createNewFile()) {
                    System.out.println("New log file created: " + log.getName());	//log.info()
                    fileExist = true;
                    return log;
                } else {
                    System.out.println("Attempted to create log: file already exists.");
                }
            } catch (IOException e) {
                System.out.println("An error occurred while creating a new log file");
                log.info(e.toString());
            }
        }
        else{
            System.out.println("An error occurred: log file path must end with .log");
        }
        fileExist = false;
        return null;
    }

    //Removes old log and creates a log at the new location. Does NOT move the file
    public void changeLog(String newPath){
        try{
            if (logFile.delete()) {
                System.out.println("New log path -> deleted the previous log file: " + logFile.getName());
            } else {
                System.out.println("New log path -> failed to delete the previous log file: " + logFile.getName());
            }
        } catch (Exception e){
            log.info(e.toString());
        }
        logPath = newPath;
        logFile = createLog(logPath);
    }
}
