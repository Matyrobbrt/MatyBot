package io.github.matyrobbrt.matybot.event;

import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;

import io.github.matyrobbrt.matybot.util.Emotes;
import io.github.matyrobbrt.matybot.util.Emotes.EmoteType;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class EmoteReactionEventHandler extends ListenerAdapter {

	private static final Random RAND = new Random();

	private static List<EmoteType> atHereEmotes = Lists.newArrayList();
	private static List<EmoteType> atEveryoneEmotes = Lists.newArrayList();

	public static void registerEmotes() {
		atHereEmotes.addAll(Lists.newArrayList(Emotes.EmoteType.CONCERN, Emotes.EmoteType.STABOLB));

		atEveryoneEmotes.addAll(Lists.newArrayList(Emotes.EmoteType.ANIMATED_BAN_BOLB));
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if (event.isWebhookMessage() || !event.isFromGuild() || event.getAuthor().isBot()) { return; }
		final Message message = event.getMessage();
		final Member member = event.getMember();

		final var contentRawLowerCase = message.getContentRaw().toLowerCase();

		if (contentRawLowerCase.startsWith("pogchamp")) {
			Emotes.react(message, EmoteType.POGCHAMP);
		} else if (contentRawLowerCase.startsWith("pog")) {
			Emotes.react(message, EmoteType.POG);
		}

		if (contentRawLowerCase.startsWith("<:concern:")) {
			Emotes.react(message, EmoteType.CONCERN);
		}

		if (contentRawLowerCase.startsWith("<:harold:")) {
			Emotes.react(message, EmoteType.HAROLD);
		}

		if (contentRawLowerCase.startsWith("<:stabolb:") || contentRawLowerCase.contains("stabby")) {
			Emotes.react(message, EmoteType.STABBY);
		}

		if (message.getContentRaw().contains("@here") && !member.hasPermission(Permission.MESSAGE_MENTION_EVERYONE)) {
			Emotes.react(message, atHereEmotes.get(RAND.nextInt(atHereEmotes.size())));
		}

		if (message.getContentRaw().contains("@everyone")
				&& !member.hasPermission(Permission.MESSAGE_MENTION_EVERYONE)) {
			Emotes.react(message, atEveryoneEmotes.get(RAND.nextInt(atEveryoneEmotes.size())));
		}
	}

}
