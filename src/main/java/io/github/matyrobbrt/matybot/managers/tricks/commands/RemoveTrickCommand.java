package io.github.matyrobbrt.matybot.managers.tricks.commands;

import java.util.List;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;

import io.github.matyrobbrt.matybot.MatyBot;
import io.github.matyrobbrt.matybot.api.annotation.RegisterSlashCommand;
import io.github.matyrobbrt.matybot.api.command.slash.GuildSpecificSlashCommand;
import io.github.matyrobbrt.matybot.managers.tricks.TrickManager;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class RemoveTrickCommand extends GuildSpecificSlashCommand {

	// @RegisterCommand
	@RegisterSlashCommand
	private static final RemoveTrickCommand CMD = new RemoveTrickCommand();

	public RemoveTrickCommand() {
		super("");
		this.name = "remove-trick";
		aliases = new String[] {
				"remtrick"
		};
		arguments = "<trick_name>";
		help = "Removes a trick";
		enabledRolesGetter = guildId -> MatyBot.getConfigForGuild(guildId).trickManagerRoles.stream()
				.map(String::valueOf).toArray(String[]::new);
		options = List.of(new OptionData(OptionType.STRING, "trick", "The trick to delete.").setRequired(true));
	}

	@Override
	protected void execute(SlashCommandEvent event) {
		TrickManager.getTrick(event.getGuild().getIdLong(), event.getOption("trick").getAsString()).ifPresent(t -> {
			TrickManager.removeTrick(event.getGuild().getIdLong(), t);
			event.reply("Removed trick!").setEphemeral(true).queue();
		});
	}

	@Override
	protected void execute(CommandEvent event) {
		final var channel = event.getTextChannel();
		TrickManager.getTrick(event.getGuild().getIdLong(), event.getArgs().split(" ")[0]).ifPresent(t -> {
			TrickManager.removeTrick(event.getGuild().getIdLong(), t);
			channel.sendMessage("Removed trick!").queue();
		});
	}

}
