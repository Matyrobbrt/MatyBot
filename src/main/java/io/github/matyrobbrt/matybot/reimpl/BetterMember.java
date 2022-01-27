package io.github.matyrobbrt.matybot.reimpl;

import java.util.List;

import javax.annotation.Nonnull;

import io.github.matyrobbrt.matybot.MatyBot;
import io.github.matyrobbrt.matybot.managers.CustomPingManager.CustomPing;
import io.github.matyrobbrt.matybot.util.database.dao.nbt.LevelData;
import io.github.matyrobbrt.matybot.util.database.dao.nbt.UserSettings;
import net.dv8tion.jda.api.entities.Member;

public interface BetterMember extends Member {

	default UserSettings getGlobalSettings() {
		return MatyBot.nbtDatabase().getSettingsForUser(getIdLong());
	}

	default BetterGuild getBetterGuild() {
		return new BetterGuildImpl(getGuild());
	}

	@Nonnull
	default LevelData getLevelData() {
		return getBetterGuild().getData().getLevelDataForUser(this);
	}

	default List<CustomPing> getCustomPings() {
		return getBetterGuild().getData().getCustomPings(this);
	}
}
