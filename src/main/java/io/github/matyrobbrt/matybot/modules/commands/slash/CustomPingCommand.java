package io.github.matyrobbrt.matybot.modules.commands.slash;

import com.jagrosh.jdautilities.command.SlashCommand;

import io.github.matyrobbrt.matybot.MatyBot;
import io.github.matyrobbrt.matybot.api.annotation.RegisterSlashCommand;
import io.github.matyrobbrt.matybot.managers.CustomPingManager.CustomPing;
import io.github.matyrobbrt.matybot.reimpl.MatyBotSlashCommand;
import io.github.matyrobbrt.matybot.reimpl.SlashCommandEvent;
import io.github.matyrobbrt.matybot.util.BotUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class CustomPingCommand extends MatyBotSlashCommand {

	@RegisterSlashCommand
	private static final CustomPingCommand CMD = new CustomPingCommand();

	private CustomPingCommand() {
		name = "custom-pings";
		guildOnly = false;
		help = "Does stuff regarding custom pings in this guild.";
		children = new SlashCommand[] {
				new Add(), new Remove(), new List()
		};
	}

	@Override
	protected void execute(SlashCommandEvent event) {
	}

	private static final class Add extends MatyBotSlashCommand {

		public Add() {
			name = "add";
			help = "Adds a new custom ping.";
			options = java.util.List.of(
					new OptionData(OptionType.STRING, "pattern", "The regex to check for in messages.")
							.setRequired(true),
					new OptionData(OptionType.STRING, "text", "The custom ping text.").setRequired(true));
		}

		@Override
		protected void execute(SlashCommandEvent event) {
			if (!event.isFromGuild()) {
				event.deferReply(true).setContent("This command only works in guilds!").queue();
				return;
			}

			final var pattern = BotUtils.getArgumentOrEmpty(event, "pattern");
			final var text = BotUtils.getArgumentOrEmpty(event, "text");
			event.getGuild().getData().getCustomPings(event.getMember()).add(new CustomPing(pattern, text));
			MatyBot.nbtDatabase().setDirty();
			event.deferReply(true).setContent("The new custom ping has been added!").queue();
		}

	}

	private static final class Remove extends MatyBotSlashCommand {

		public Remove() {
			name = "remove";
			help = "Removes a custom ping.";
			options = java.util.List
					.of(new OptionData(OptionType.STRING, "pattern", "The regex that the custom ping to remove has.")
							.setRequired(true));
		}

		@Override
		protected void execute(SlashCommandEvent event) {
			if (!event.isFromGuild()) {
				event.deferReply(true).setContent("This command only works in guilds!").queue();
				return;
			}

			final var pattern = BotUtils.getArgumentOrEmpty(event, "pattern");
			final var customPings = event.getGuild().getData().getCustomPings(event.getMember());
			for (int i = 0; i < customPings.size(); i++) {
				if (customPings.get(i).pattern().pattern().equals(pattern)) {
					customPings.remove(i);
					MatyBot.nbtDatabase().setDirty();
					event.deferReply(true).setContent("Custom ping removed!").queue();
					break;
				}
			}
			event.deferReply(true).setContent("A custom ping with that pattern could not be found.");
		}

	}

	private static final class List extends MatyBotSlashCommand {

		public List() {
			name = "list";
			help = "Lists your custom pings.";
		}

		@Override
		protected void execute(SlashCommandEvent event) {
			if (!event.isFromGuild()) {
				event.deferReply(true).setContent("This command only works in guilds!").queue();
				return;
			}

			event.deferReply().queue(hook -> {
				final var customPings = event.getGuild().getData().getCustomPings(event.getMember());
				final var embed = new EmbedBuilder();
				for (int i = 0; i < customPings.size(); i++) {
					final var ping = customPings.get(i);
					embed.appendDescription("**%s** - %s".formatted(ping.pattern().pattern(), ping.text()));
					if (i != customPings.size() - 1) {
						embed.appendDescription(System.lineSeparator());
					}
				}
				hook.editOriginalEmbeds(embed.build()).queue();
			});
		}

	}
}
