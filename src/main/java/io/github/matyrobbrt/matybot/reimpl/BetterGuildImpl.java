package io.github.matyrobbrt.matybot.reimpl;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.GuildImpl;

public class BetterGuildImpl extends GuildImpl implements BetterGuild {

	public BetterGuildImpl(JDAImpl api, long id) {
		super(api, id);
	}

	public BetterGuildImpl(final Guild guild) {
		super((JDAImpl) guild.getJDA(), guild.getIdLong());
	}

	@Override
	public BetterMember getMember(User user) {
		return new BetterMemberImpl(super.getMember(user));
	}

}
