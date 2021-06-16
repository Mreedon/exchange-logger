package com.exchangelogger;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("exchangelogger")
public interface ExchangeLoggerConfig extends Config
{
	@ConfigItem(
		position = 1,
		keyName = "filePath",
		name = "File Path",
		description = "Log file location, changing path will delete the old log and it's data"
	)
	default String filePath()
	{
		return "exchange.log";
	}

	@ConfigItem(
		position = 2,
		keyName = "oneFile",
		name = "One File",
		description = "Delete old log data when using the plugin in a new session"
	)
	default boolean oneFile()
	{
		return false;
	}
}
