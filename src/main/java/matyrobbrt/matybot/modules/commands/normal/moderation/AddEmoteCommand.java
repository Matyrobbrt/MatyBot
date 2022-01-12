package matyrobbrt.matybot.modules.commands.normal.moderation;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.collect.Lists;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import matyrobbrt.matybot.api.annotation.RegisterCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.EmbedType;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.Message;

public class AddEmoteCommand extends Command {

	@RegisterCommand
	private static final AddEmoteCommand CMD = new AddEmoteCommand();

	public AddEmoteCommand() {
		this.name = "add-emote";
		this.userPermissions = new Permission[] {
				Permission.MANAGE_EMOTES_AND_STICKERS
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
			AtomicReference<CompletableFuture<Icon>> toRun = new AtomicReference<>();
			final Message reply = event.getMessage().getMessageReference().getMessage();
			if (!reply.getAttachments().isEmpty()) {
				toRun.set(reply.getAttachments().get(0).retrieveAsIcon());
			} else {
				reply.getEmbeds().stream().filter(embed -> embed.getType() == EmbedType.IMAGE).findFirst()
						.ifPresentOrElse(embed -> {
							try {
								URL url = new URL(embed.getUrl());
								InputStream in = url.openStream();
								toRun.set(CompletableFuture.completedFuture(Icon.from(in)));
							} catch (Exception e) {
								event.getMessage().reply(
										"Exception while trying to add emote: **" + e.getLocalizedMessage() + "**")
										.queue();
							}
						}, () -> {
							event.getMessage().reply("The linked message does not have any images or embeds!").queue();
						});
			}
			toRun.get().thenAccept(icon -> {
				event.getGuild().createEmote(emoteName, icon).queue(e -> {
					event.getMessage().reply("New Emote!").mentionRepliedUser(false)
							.queue(h -> h.addReaction(e).queue());
				});
			}).get();
		} catch (Exception e) {
			event.getMessage().reply("Exception while trying to add emote: **" + e.getLocalizedMessage() + "**")
					.queue();
		}
	}

}
