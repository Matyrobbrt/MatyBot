package matyrobbrt.matybot.modules.commands.normal;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import matyrobbrt.matybot.annotation.RegisterCommand;
import net.dv8tion.jda.api.Permission;

public class ReactCommand extends Command {

	@RegisterCommand
	private static final ReactCommand CMD = new ReactCommand();

	public ReactCommand() {
		this.name = "react";
		this.userPermissions = new Permission[] {
				Permission.MANAGE_CHANNEL
		};
		this.guildOnly = true;
	}

	@Override
	protected void execute(CommandEvent event) {
		final String[] args = event.getArgs().split(" ");
		final long msgId = Long.parseLong(args[0]);
		final var emote = args[1].replace("<", "").replace(">", "");
		event.getChannel().getHistory().getChannel().addReactionById(msgId, emote).queue();
	}

}
