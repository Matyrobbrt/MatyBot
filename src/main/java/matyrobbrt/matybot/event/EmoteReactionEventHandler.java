package matyrobbrt.matybot.event;

import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;

import matyrobbrt.matybot.api.annotation.EventSubscriber;
import matyrobbrt.matybot.api.event.AnnotationEventListener;
import matyrobbrt.matybot.util.Emotes;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

@EventSubscriber(createInstance = true)
public class EmoteReactionEventHandler extends AnnotationEventListener {

	private static final Random RAND = new Random();

	private static List<Emote> atHereEmotes = Lists.newArrayList();
	private static List<Emote> atEveryoneEmotes = Lists.newArrayList();

	public static void registerEmotes() {
		atHereEmotes.addAll(Lists.newArrayList(Emotes.STABOLB, Emotes.CONCERN));

		atEveryoneEmotes.addAll(Lists.newArrayList(Emotes.ANIMATED_BAN_BOLB));
	}

	@SubscribeEvent
	@Override
	public void onEventHandleAnnotation(GenericEvent event) {
		super.onEventHandleAnnotation(event);
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if (event.isWebhookMessage() || !event.isFromGuild()) { return; }
		final Message message = event.getMessage();
		final Member member = event.getMember();

		final var contentRawLowerCase = message.getContentRaw().toLowerCase();

		if (contentRawLowerCase.startsWith("pogchamp")) {
			message.addReaction(Emotes.POGCHAMP).queue();
		} else if (contentRawLowerCase.startsWith("pog")) {
			message.addReaction(Emotes.POG).queue();
		}

		if (contentRawLowerCase.startsWith("<:concern:")) {
			message.addReaction(Emotes.CONCERN).queue();
		}

		if (message.getContentRaw().contains("@here") && !member.hasPermission(Permission.MESSAGE_MENTION_EVERYONE)) {
			Emote reaction = atHereEmotes.get(RAND.nextInt(atHereEmotes.size()));
			message.addReaction(reaction).queue();
		}

		if (message.getContentRaw().contains("@everyone")
				&& !member.hasPermission(Permission.MESSAGE_MENTION_EVERYONE)) {
			Emote reaction = atEveryoneEmotes.get(RAND.nextInt(atEveryoneEmotes.size()));
			message.addReaction(reaction).queue();
		}
	}

}
