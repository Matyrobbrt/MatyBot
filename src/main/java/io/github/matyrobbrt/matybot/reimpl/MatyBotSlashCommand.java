package io.github.matyrobbrt.matybot.reimpl;

import com.jagrosh.jdautilities.command.SlashCommand;

public abstract class MatyBotSlashCommand extends SlashCommand {

	@Override
	protected final void execute(com.jagrosh.jdautilities.command.SlashCommandEvent event) {
		execute(new SlashCommandEvent(event));
	}

	protected abstract void execute(final SlashCommandEvent event);

}
