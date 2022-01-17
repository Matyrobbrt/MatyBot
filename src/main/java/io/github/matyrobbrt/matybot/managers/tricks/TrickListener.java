package io.github.matyrobbrt.matybot.managers.tricks;

import java.util.Locale;

import io.github.matyrobbrt.matybot.modules.commands.CommandsModule;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class TrickListener extends ListenerAdapter {

	private final long guildId;

	public TrickListener(final long guildId) {
		this.guildId = guildId;
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if (!event.isFromGuild() || event.getGuild().getIdLong() != guildId || !checkPrefix(event)) { return; }
		final String[] args = event.getMessage().getContentRaw().split(" ");
		if (args.length < 1) { return; }
		final String prefix = getPrefix(event);
		final String trickName = args[0].substring(prefix.length());
		TrickManager.getTricksForGuild(guildId).forEach(trick -> {
			if (trick.getNames().contains(trickName)) {
				final String[] trickArgs = new String[args.length - 1];
				System.arraycopy(args, 1, trickArgs, 0, trickArgs.length);
				event.getMessage().reply(trick.getMessage(trickArgs)).queue();
			}
		});
	}

	public static boolean checkPrefix(final MessageReceivedEvent msgEvent) {
		return msgEvent.getMessage().getContentRaw().toLowerCase(Locale.ROOT).startsWith(getPrefix(msgEvent));
	}

	public static String getPrefix(final MessageReceivedEvent event) {
		final var cmdClient = CommandsModule.getInstance().getCommandClient();
		return cmdClient.getPrefixFunction() == null ? cmdClient.getPrefix()
				: cmdClient.getPrefixFunction().apply(event);
	}

}
