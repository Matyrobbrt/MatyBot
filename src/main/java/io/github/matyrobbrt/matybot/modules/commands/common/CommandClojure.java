package io.github.matyrobbrt.matybot.modules.commands.common;

import com.jagrosh.jdautilities.command.SlashCommand;

import io.github.matyrobbrt.matybot.api.annotation.RegisterCommand;
import io.github.matyrobbrt.matybot.api.annotation.RegisterSlashCommand;
import io.github.matyrobbrt.matybot.reimpl.MatyBotSlashCommand;
import io.github.matyrobbrt.matybot.reimpl.SlashCommandEvent;

public class CommandClojure extends MatyBotSlashCommand {

	@RegisterCommand
	@RegisterSlashCommand
	private static final CommandClojure CMD = new CommandClojure();

	public CommandClojure() {
		name = "clojure";
		guildOnly = true;
		help = "Does clojure related stuff.";
		children = new SlashCommand[] {
				// Evaluate is at index 0
				new Evaluate()
		};
	}

	@Override
	protected void execute(SlashCommandEvent event) {
	}

	public final class Evaluate extends MatyBotSlashCommand {

		public Evaluate() {
			name = "evaluate";
		}

		@Override
		protected void execute(SlashCommandEvent event) {
		}

	}

}
