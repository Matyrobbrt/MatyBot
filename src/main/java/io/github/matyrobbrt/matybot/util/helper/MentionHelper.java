package io.github.matyrobbrt.matybot.util.helper;

import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.entities.IMentionable;

@UtilityClass
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
