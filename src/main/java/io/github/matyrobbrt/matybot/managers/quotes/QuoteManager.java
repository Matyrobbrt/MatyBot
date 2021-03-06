package io.github.matyrobbrt.matybot.managers.quotes;

import java.util.List;

import io.github.matyrobbrt.matybot.MatyBot;
import net.dv8tion.jda.api.entities.Guild;

public class QuoteManager {

	public static List<Quote> getQuotesForGuild(long guidId) {
		return MatyBot.nbtDatabase().getDataForGuild(guidId).getQuotes();
	}

	public static List<Quote> getQuotesForGuild(final Guild guild) {
		return getQuotesForGuild(guild.getIdLong());
	}

	public static void addQuote(Quote quote, long guildId) {
		getQuotesForGuild(guildId).add(quote);
		MatyBot.nbtDatabase().setDirtyAndSave();
	}

	public static Quote removeQuote(int index, long guildId) {
		var quotesForGuild = getQuotesForGuild(guildId);
		if (index < quotesForGuild.size()) {
			var toRet = quotesForGuild.remove(index);
			MatyBot.nbtDatabase().setDirtyAndSave();
			return toRet;
		}
		return null;
	}

}
