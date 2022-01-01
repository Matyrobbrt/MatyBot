package matyrobbrt.matybot.util;

import matyrobbrt.matybot.MatyBot;
import matyrobbrt.matybot.event.EmoteReactionEventHandler;
import net.dv8tion.jda.api.entities.Emote;

public class Emotes {

	public static Emote STABOLB = null;
	public static Emote CONCERN = null;
	public static Emote ANIMATED_FAST_BAN = null;

	public static void register() {
		STABOLB = MatyBot.instance.getBot().getEmoteById(926590733887107123l);
		CONCERN = getEmoteById(926611796519952454l);
		ANIMATED_FAST_BAN = getEmoteById(926634258431348798l);

		EmoteReactionEventHandler.registerEmotes();
	}

	private static Emote getEmoteById(long id) {
		return MatyBot.instance.getBot().getEmoteById(id);
	}

}
