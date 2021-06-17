/*
 * Copyright (c) 2021, Anton <https://github.com/istid>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.exchangelogger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.runelite.api.GrandExchangeOffer;
import net.runelite.api.GrandExchangeOfferState;
import static net.runelite.api.GrandExchangeOfferState.*;

public class ExchangeLoggerFormatting
{
	String line;

	public String plainText(GrandExchangeOffer offer, int slot, String time)
	{
		//First offer for item
		if (offer.getQuantitySold() == 0 && anyEqualState(offer.getState(), BUYING, SELLING))
		{
			//Differentiate the first offer state from subsequent ones
			String firstState = ((offer.getState() == BUYING) ? "BUY" : "SELL");

			line = (time + " state: " + firstState + " slot: " + slot + " item: " + offer.getItemId()
					+ " max: " + offer.getTotalQuantity() + " offer: " + offer.getPrice());
		}
		else if (anyEqualState(offer.getState(), CANCELLED_BUY, CANCELLED_SELL))
		{
			line = (time + " state: " + offer.getState() + " slot: " + slot + " item: " + offer.getItemId()
					+ " qty: " + offer.getQuantitySold() + " worth: " + offer.getSpent() + " max: " + offer.getTotalQuantity());
		}
		else if (offer.getState() == EMPTY)
		{
			line = (time + " state: " + offer.getState() + " slot: " + slot);
		}
		else
		{
			line = (time + " state: " + offer.getState() + " slot: " + slot + " item: " + offer.getItemId()
					+ " qty: " + offer.getQuantitySold() + " worth: " + offer.getSpent());
		}
		return line;
	}

	public String tabular(GrandExchangeOffer offer, int slot, String time)
	{
		String[] split = time.split(" ", 2);
		line = (split[0] + "," + split[1] + "," + offer.getState()
				+ "," + slot + "," + offer.getItemId() + "," + offer.getQuantitySold()
				+ "," + offer.getSpent() + "," + offer.getTotalQuantity() + "," + offer.getPrice());

		return line;
	}

	public String json(GrandExchangeOffer offer, int slot, String time)
	{
		ExchangeLoggerSlotStatus status = new ExchangeLoggerSlotStatus();
		String[] split = time.split(" ", 2);

		status.date = split[0];
		status.time = split[1];
		status.state = offer.getState();
		status.slot = slot;
		status.item = offer.getItemId();
		status.qty = offer.getQuantitySold();
		status.worth = offer.getSpent();
		status.max = offer.getTotalQuantity();
		status.offer = offer.getPrice();

		GsonBuilder builder = new GsonBuilder();
		Gson gson = builder.create();
		String jsonString = gson.toJson(status);

		return jsonString;
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
