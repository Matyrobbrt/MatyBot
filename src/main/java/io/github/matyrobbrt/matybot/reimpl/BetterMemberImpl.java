package io.github.matyrobbrt.matybot.reimpl;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.entities.MemberImpl;

public class BetterMemberImpl extends MemberImpl implements BetterMember {

	public BetterMemberImpl(GuildImpl guild, User user) {
		super(guild, user);
	}

	public BetterMemberImpl(Member member) {
		super((GuildImpl) member.getGuild(), member.getUser());
	}

}
