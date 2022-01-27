package io.github.matyrobbrt.matybot.util;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import io.github.matyrobbrt.matybot.MatyBot;
import io.github.matyrobbrt.matybot.event.EmoteReactionEventHandler;
import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Message;

@UtilityClass
public class Emotes {

	// Normal
	private static Emote stabolb = null;
	private static Emote concern = null;
	private static Emote pog = null;
	private static Emote pogchamp = null;
	private static Emote harold = null;

	// Animated
	private static Emote animatedBanBolb = null;
	private static Emote stabby = null;

	public static void register() {
		stabolb = getEmoteById(MatyBot.generalConfig().stabolbEmoteID);
		concern = getEmoteById(MatyBot.generalConfig().concernEmoteID);
		pog = getEmoteById(MatyBot.generalConfig().pogEmoteID);
		pogchamp = getEmoteById(MatyBot.generalConfig().pogchampEmoteID);
		harold = getEmoteById(MatyBot.generalConfig().haroldEmoteID);

		animatedBanBolb = getEmoteById(MatyBot.generalConfig().animatedBanBolbEmoteID);
		stabby = getEmoteById(MatyBot.generalConfig().stabbyEmoteID);

		EmoteReactionEventHandler.registerEmotes();
	}

	private static Emote getEmoteById(long id) {
		return MatyBot.getInstance().getEmoteById(id);
	}

	public static CompletableFuture<Void> reactNoComplete(@Nonnull final Message message,
			@Nonnull final EmoteType emoteType) {
		final Emote emote = emoteType.get();
		if (emote != null) { return message.addReaction(emote).submit(); }
		return CompletableFuture.failedFuture(new NullPointerException());
	}

	public static void react(@Nonnull final Message message, @Nonnull final EmoteType emoteType) {
		try {
			reactNoComplete(message, emoteType).get();
		} catch (InterruptedException | ExecutionException e) {}
	}

	public enum EmoteType implements Supplier<Emote> {

		STABOLB(() -> stabolb), CONCERN(() -> concern), POG(() -> pog), POGCHAMP(() -> pogchamp), HAROLD(() -> harold),
		ANIMATED_BAN_BOLB(() -> animatedBanBolb), STABBY(() -> stabby);

		private final Supplier<Emote> emote;

		private EmoteType(Supplier<Emote> emote) {
			this.emote = emote;
		}

		@Override
		public Emote get() {
			return emote.get();
		}

	}

}
