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

	/**
	 * {@inheritDoc}
	 */
	@Override
	default BetterMember getMemberById(long userId) {
		final var member = Guild.super.getMemberById(userId);
		return member == null ? null : new BetterMemberImpl(member);
	}

	@Override
	default BetterMember getMemberById(String userId) {
		final var member = Guild.super.getMemberById(userId);
		return member == null ? null : new BetterMemberImpl(member);
	}

	@Override
	default BetterMember getMemberByTag(String tag) {
		final var member = Guild.super.getMemberByTag(tag);
		return member == null ? null : new BetterMemberImpl(member);
	}

	@Override
	default BetterMember getMemberByTag(String username, String discriminator) {
		final var member = Guild.super.getMemberByTag(username, discriminator);
		return member == null ? null : new BetterMemberImpl(member);
	}

}
