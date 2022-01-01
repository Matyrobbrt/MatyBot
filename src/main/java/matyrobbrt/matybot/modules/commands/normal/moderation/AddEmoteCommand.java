package matyrobbrt.matybot.modules.commands.normal.moderation;

import java.util.List;

import com.google.common.collect.Lists;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import matyrobbrt.matybot.annotation.RegisterCommand;
import net.dv8tion.jda.api.Permission;

public class AddEmoteCommand extends Command {

	@RegisterCommand
	private static final AddEmoteCommand CMD = new AddEmoteCommand();

	public AddEmoteCommand() {
		this.name = "add-emote";
		this.userPermissions = new Permission[] {
				Permission.MANAGE_EMOTES
		};
		this.guildOnly = true;
	}

	@Override
	protected void execute(CommandEvent event) {
		final List<String> args = event.getArgs().isEmpty() ? Lists.newArrayList()
				: Lists.newArrayList(event.getArgs().split(" "));
		if (args.isEmpty()) {
			event.getMessage().reply("Please give a name for the emote!").mentionRepliedUser(false).queue();
			return;
		}
		final String emoteName = args.get(0);
		if (event.getMessage().getMessageReference() == null) {
			event.getMessage().reply("Please reply to a message containing an attachement!").mentionRepliedUser(false)
					.queue();
			return;
		}
		try {
			event.getGuild().createEmote(emoteName, event.getMessage().getMessageReference().getMessage()
					.getAttachments().get(0).retrieveAsIcon().get()).queue(e -> {
						event.getMessage().reply("New Emote!").mentionRepliedUser(false)
								.queue(h -> h.addReaction(e).queue());
					});
		} catch (Exception e) {
			event.getMessage().reply("Exception while trying to add emote: **" + e.getLocalizedMessage() + "**");
		}
	}

}
