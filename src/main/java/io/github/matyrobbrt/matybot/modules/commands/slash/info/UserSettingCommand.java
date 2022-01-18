package io.github.matyrobbrt.matybot.modules.commands.slash.info;

import java.util.List;

import com.jagrosh.jdautilities.command.CooldownScope;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;

import io.github.matyrobbrt.matybot.MatyBot;
import io.github.matyrobbrt.matybot.api.annotation.RegisterSlashCommand;
import io.github.matyrobbrt.matybot.util.BotUtils;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class UserSettingCommand extends SlashCommand {

	private static final Choice[] CHOICES = {
			makeChoice("level_up_ping")
	};

	private static Choice makeChoice(final String name) {
		return new Choice(name, name);
	}

	@RegisterSlashCommand
	private static final UserSettingCommand CMD = new UserSettingCommand();

	private UserSettingCommand() {
		name = "user-settings";
		help = "Configures your global user settings.";
		cooldown = 20;
		cooldownScope = CooldownScope.USER;
		options = List.of(new OptionData(OptionType.STRING, "key", "The configuration option").setRequired(true)
				.addChoices(CHOICES), new OptionData(OptionType.STRING, "new_value", "The value to set"));
		guildOnly = false;
	}

	@Override
	protected void execute(SlashCommandEvent event) {
		final var key = BotUtils.getArgumentOrEmpty(event, "key");
		final var value = BotUtils.getArgumentOrEmpty(event, "new_value");
		final var userSettings = MatyBot.nbtDatabase().getSettingsForUser(event.getMember());
		switch (key) {
		case "level_up_ping" -> {
			if (value.isBlank()) {
				event.deferReply(true).setContent(
						"Your current option for `level_up_ping` is **%s**".formatted(userSettings.doesLevelUpPing()))
						.queue();
				return;
			}

			userSettings.setLevelUpPing(Boolean.valueOf(value));
			event.deferReply(true).setContent("Your options have been updated!").queue();
			MatyBot.nbtDatabase().setDirtyAndSave();
		}
		default -> {}
		}
	}

}
