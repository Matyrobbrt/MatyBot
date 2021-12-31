package matyrobbrt.matybot.modules.commands.normal;

import com.google.common.collect.Lists;
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
		final var args = Lists.newArrayList(event.getArgs().split(" "));
		final long msgId = event.getMessage().getMessageReference() == null ? Long.parseLong(args.get(0))
				: Long.parseLong(event.getMessage().getMessageReference().getMessageId());
		final var emote = args.get(event.getMessage().getMessageReference() == null ? 1 : 0).replace("<", "")
				.replace(">", "");
		event.getChannel().getHistory().getChannel().addReactionById(msgId, emote).queue();
	}

}
