package matyrobbrt.matybot.util.helper;

import net.dv8tion.jda.api.entities.IMentionable;

public class MentionHelper {

	public static IMentionable user(final long userId) {
		return new IMentionable() {

			@Override
			public long getIdLong() { return userId; }

			@Override
			public String getAsMention() { return "<@" + userId + ">"; }
		};
	}

}
