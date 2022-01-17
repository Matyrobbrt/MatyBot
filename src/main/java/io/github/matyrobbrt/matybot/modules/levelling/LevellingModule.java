package io.github.matyrobbrt.matybot.modules.levelling;

import io.github.matyrobbrt.matybot.MatyBot;
import io.github.matyrobbrt.matybot.api.event.EventListenerWrapper;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

// TODO make this module actually work (a.k.a. implement levelling)
public class LevellingModule extends io.github.matyrobbrt.matybot.api.modules.Module {

	public LevellingModule(final JDA bot) {
		super(() -> MatyBot.generalConfig().isLevellingModuleEnabled(), bot);
	}

	@Override
	public void register() {
		bot.addEventListener(new EventListenerWrapper(new LevellingHandler()));
	}

	public static boolean isLevellingEnabled(final Guild guild) {
		return MatyBot.getConfigForGuild(guild).isLevellingEnabled();
	}

	public static int getLevelForXP(final int xp, final int multiplier) {
		return (int) Math.sqrt((xp - 15 * multiplier) / multiplier);
	}

	public static int getLevelForXP(final int xp, final Guild guild) {
		return getLevelForXP(xp, MatyBot.getConfigForGuild(guild).xpMultiplier);
	}

	public static int getXPForLevel(final int level, final int multiplier) {
		return (int) (multiplier * Math.pow(level, 2) + 15 * multiplier);
	}

	public static int getXPForLevel(final int level, final Guild guild) {
		return getXPForLevel(level, MatyBot.getConfigForGuild(guild).xpMultiplier);
	}

	public static int getUserXP(final Guild guild, final long userId) {
		return MatyBot.nbtDatabase().getDataForGuild(guild).getLevelDataForUser(userId).getXp();
	}

	public static int getUserXP(final Member member) {
		return getUserXP(member.getGuild(), member.getIdLong());
	}

	public static void setUserXP(final Guild guild, final long userId, final int xp) {
		MatyBot.nbtDatabase().getDataForGuild(guild).getLevelDataForUser(userId).setXp(xp);
		MatyBot.nbtDatabase().setDirty(true);
	}

	public static void setUserXP(final Member member, final int xp) {
		setUserXP(member.getGuild(), member.getIdLong(), xp);
	}

}
