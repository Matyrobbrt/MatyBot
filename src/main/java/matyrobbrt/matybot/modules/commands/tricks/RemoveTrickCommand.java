package matyrobbrt.matybot.modules.commands.tricks;

import java.util.List;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;

import matyrobbrt.matybot.MatyBot;
import matyrobbrt.matybot.annotation.RegisterSlashCommand;
import matyrobbrt.matybot.tricks.TrickManager;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class RemoveTrickCommand extends SlashCommand {

	// @RegisterCommand
	@RegisterSlashCommand
	private static final RemoveTrickCommand CMD = new RemoveTrickCommand();

	public RemoveTrickCommand() {
		this.name = "remove-trick";
		aliases = new String[] {
				"remtrick"
		};
		arguments = "<trick_name>";
		help = "Removes a trick";
		guildOnly = true;
		defaultEnabled = false;
		enabledRoles = MatyBot.config().trickManagerRoles.stream().map(String::valueOf).toArray(String[]::new);
		options = List.of(new OptionData(OptionType.STRING, "trick", "The trick to delete.").setRequired(true));
	}

	@Override
	protected void execute(SlashCommandEvent event) {
		TrickManager.getTrick(event.getOption("trick").getAsString()).ifPresent(t -> {
			TrickManager.removeTrick(t);
			event.reply("Removed trick!").setEphemeral(true).queue();
		});
	}

	@Override
	protected void execute(CommandEvent event) {
		final var channel = event.getTextChannel();
		TrickManager.getTrick(event.getArgs().split(" ")[0]).ifPresent(t -> {
			TrickManager.removeTrick(t);
			channel.sendMessage("Removed trick!").queue();
		});
	}

}
