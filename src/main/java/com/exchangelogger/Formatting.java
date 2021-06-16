package com.exchangelogger;

import net.runelite.api.GrandExchangeOffer;
import net.runelite.api.GrandExchangeOfferState;

import static net.runelite.api.GrandExchangeOfferState.*;

public class Formatting
{
	String line;

	public String plainText(GrandExchangeOffer offer, int slot, String time)
	{
		//First offer for item
		if (offer.getQuantitySold() == 0 && anyEqualState(offer.getState(), BUYING, SELLING))
		{
			String firstState = ((offer.getState() == BUYING) ? "BUY" : "SELL");    //Differentiate the first offer state from subsequent ones

			line = (time + " state: " + firstState + " slot: " + slot + " item: " + offer.getItemId() +
				" max: " + offer.getTotalQuantity() + " offer: " + offer.getPrice());
		}
		else if (anyEqualState(offer.getState(), CANCELLED_BUY, CANCELLED_SELL))
		{
			line = (time + " state: " + offer.getState() + " slot: " + slot + " item: " + offer.getItemId() +
				" qty: " + offer.getQuantitySold() + " worth: " + offer.getSpent() + " max: " + offer.getTotalQuantity());
		}
		else if (offer.getState() == EMPTY)
		{
			line = (time + " state: " + offer.getState() + " slot: " + slot);
		}
		else
		{
			line = (time + " state: " + offer.getState() + " slot: " + slot + " item: " + offer.getItemId() +
				" qty: " + offer.getQuantitySold() + " worth: " + offer.getSpent());
		}
		return line;
	}

	public String tabular(GrandExchangeOffer offer, int slot, String time)
	{
		line = (time + "," + offer.getState() + "," + slot + "," + offer.getItemId() + "," + offer.getQuantitySold()
			+ "," + offer.getSpent() + "," + offer.getTotalQuantity() + "," + offer.getPrice());

		return line;
	}

	public boolean anyEqualState(GrandExchangeOfferState expected, GrandExchangeOfferState ...array)
	{
		for (GrandExchangeOfferState state : array)
		{
			if (state.equals(expected))
			{
				return true;
			}
		}
		return false;
	}

}
