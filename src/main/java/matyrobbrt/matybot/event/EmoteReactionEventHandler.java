package matyrobbrt.matybot.event;

import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;

import matyrobbrt.matybot.annotation.EventSubscriber;
import matyrobbrt.matybot.event.api.AnnotationEventListener;
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

		atEveryoneEmotes.addAll(Lists.newArrayList(Emotes.ANIMATED_FAST_BAN));
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
