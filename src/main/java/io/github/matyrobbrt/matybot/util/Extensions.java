package io.github.matyrobbrt.matybot.util;

import java.util.Arrays;

import io.github.matyrobbrt.matybot.MatyBot;
import io.github.matyrobbrt.matybot.util.database.dao.nbt.GuildData;
import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.entities.Guild;

@UtilityClass
public class Extensions {

	public static GuildData getGuildData(Guild guild) {
		return MatyBot.nbtDatabase().getDataForGuild(guild);
	}

	public static <T> boolean arrayContains(T[] array, T value) {
		return Arrays.asList(array).contains(value);
	}

}
