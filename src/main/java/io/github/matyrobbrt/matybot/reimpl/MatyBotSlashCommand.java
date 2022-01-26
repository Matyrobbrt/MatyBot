package io.github.matyrobbrt.matybot.reimpl;

import com.jagrosh.jdautilities.command.SlashCommand;

public abstract class MatyBotSlashCommand extends SlashCommand {

	@Override
	protected final void execute(com.jagrosh.jdautilities.command.SlashCommandEvent event) {
		try {
			execute(new SlashCommandEvent(event));
		} catch (Exception e) {
			event.deferReply(true).setContent("There was an exception while trying to execute that command! **%s**"
					.formatted(e.getLocalizedMessage()));
		}
	}

	protected abstract void execute(final SlashCommandEvent event);

}
