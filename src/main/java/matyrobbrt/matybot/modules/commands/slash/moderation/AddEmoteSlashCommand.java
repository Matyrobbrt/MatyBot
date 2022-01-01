package matyrobbrt.matybot.modules.commands.slash.moderation;

import com.jagrosh.jdautilities.command.SlashCommand;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

public class AddEmoteSlashCommand extends SlashCommand {

	// @RegisterSlashCommand
	private static final AddEmoteSlashCommand CMD = new AddEmoteSlashCommand();

	public AddEmoteSlashCommand() {
		name = "add-emote";
		help = "Adds an emote from the uuid of an already-exising emote ID";
	}

	@Override
	protected void execute(SlashCommandEvent event) {
	}

}
