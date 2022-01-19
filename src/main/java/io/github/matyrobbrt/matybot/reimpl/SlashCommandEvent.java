package io.github.matyrobbrt.matybot.reimpl;

public class SlashCommandEvent extends com.jagrosh.jdautilities.command.SlashCommandEvent {

	public SlashCommandEvent(com.jagrosh.jdautilities.command.SlashCommandEvent event) {
		super(event, event.getClient());
	}

	@Override
	public BetterGuild getGuild() {
		return new BetterGuildImpl(super.getGuild());
	}

	@Override
	public BetterMember getMember() {
		return new BetterMemberImpl(super.getMember());
	}

}
