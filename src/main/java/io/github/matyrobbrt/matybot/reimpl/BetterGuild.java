package io.github.matyrobbrt.matybot.reimpl;

import java.util.List;

import io.github.matyrobbrt.matybot.MatyBot;
import io.github.matyrobbrt.matybot.managers.quotes.Quote;
import io.github.matyrobbrt.matybot.managers.quotes.QuoteManager;
import io.github.matyrobbrt.matybot.util.config.GuildConfig;
import io.github.matyrobbrt.matybot.util.database.dao.nbt.GuildData;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

public interface BetterGuild extends Guild {

	default GuildData getData() {
		return MatyBot.nbtDatabase().getDataForGuild(this.getIdLong());
	}

	default GuildConfig getConfig() {
		return MatyBot.getConfigForGuild(this.getIdLong());
	}

	default List<Quote> getQuotes() {
		return QuoteManager.getQuotesForGuild(this.getIdLong());
	}

	@Override
	BetterMember getMember(final User user);

	@Override
	default BetterMember getMemberById(long userId) {
		return new BetterMemberImpl(Guild.super.getMemberById(userId));
	}

	@Override
	default BetterMember getMemberById(String userId) {
		return new BetterMemberImpl(Guild.super.getMemberById(userId));
	}

	@Override
	default BetterMember getMemberByTag(String tag) {
		return new BetterMemberImpl(Guild.super.getMemberByTag(tag));
	}

	@Override
	default BetterMember getMemberByTag(String username, String discriminator) {
		return new BetterMemberImpl(Guild.super.getMemberByTag(username, discriminator));
	}

}
