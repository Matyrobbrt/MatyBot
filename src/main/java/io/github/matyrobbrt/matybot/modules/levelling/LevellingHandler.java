package io.github.matyrobbrt.matybot.modules.levelling;

import static io.github.matyrobbrt.matybot.modules.levelling.LevellingModule.getLevelForXP;
import static io.github.matyrobbrt.matybot.modules.levelling.LevellingModule.getUserXP;
import static io.github.matyrobbrt.matybot.modules.levelling.LevellingModule.setUserXP;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import io.github.matyrobbrt.matybot.MatyBot;
import io.github.matyrobbrt.matybot.reimpl.BetterMember;
import io.github.matyrobbrt.matybot.reimpl.BetterMemberImpl;
import io.github.matyrobbrt.matybot.util.Constants;
import io.github.matyrobbrt.matybot.util.Emotes;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class LevellingHandler extends ListenerAdapter {

	private static final String[] LEVELUP_MESSAGES = new String[] {
			"Congrats, %s, you levelled up to level: %s! " + Emoji.fromMarkdown("U+1F389").getAsMention(),
			"Poggers, %s, you are now level: %s! " + Emotes.EmoteType.POG.get().getAsMention(),
			"%s Yooooo! You are now level: %s!",
			"GG, %s. You are now level: %s! " + Emoji.fromMarkdown("U+1F60E").getAsMention()
	};

	private static final Random RANDOM = new Random();

	private final List<Long> cooldowns = new ArrayList<>();
	private final Timer timer = new Timer("LevellingCooldown", true);

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if (!event.isFromGuild() || event.getAuthor().isBot() || event.getAuthor().isSystem()) { return; }
		if (!isOnCooldown(event.getMember())) {
			awardXp(event.getMessage(), new BetterMemberImpl(event.getMember()));
		}
	}

	public void awardXp(final Message message, final BetterMember member) {
		float messageMultiplier = message.getContentRaw().length() > 128 ? 1.5f : 1f;
		if (message.getContentRaw().length() <= 0) {
			messageMultiplier = 0f;
		}

		var boostMultiplier = 1f;

		if (member.getTimeBoosted() != null) {
			final var decimalFormat = new DecimalFormat("#.####");
			final var months = Float.parseFloat(decimalFormat
					.format((member.getTimeBoosted().toInstant().toEpochMilli() - System.currentTimeMillis())
							/ Constants.MONTH_TO_MILLI));
			boostMultiplier += months / 12f;
		}

		final var guild = member.getGuild();

		final int oldXp = getUserXP(member);
		final int oldLevel = getLevelForXP(oldXp, guild);
		setUserXP(member, oldXp + Math.round((RANDOM.nextInt(10) + 5) * boostMultiplier * messageMultiplier));
		final int newXp = getUserXP(member);
		final int newLevel = getLevelForXP(newXp, guild);
		if (newLevel > oldLevel) {
			message.getChannel().sendMessage(getLevelupMessage(member, message.getTextChannel(), newLevel)).queue();

			final var role = guild.getRoleById(MatyBot.getConfigForGuild(guild).getRoleForLevel(newLevel));
			if (role != null) {
				guild.addRoleToMember(member, role).reason("The member reached level " + newLevel).queue();
			}
		}

		cooldowns.add(member.getIdLong());
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				cooldowns.remove(member.getIdLong());
			}
		}, member.getBetterGuild().getConfig().levellingCooldown * 1000l);
	}

	public boolean isOnCooldown(final Member member) {
		return cooldowns.contains(member.getIdLong());
	}

	public static String getLevelupMessage(final Member member, final TextChannel channel, final int newLevel) {
		final var userSettings = MatyBot.nbtDatabase().getSettingsForUser(member);
		return LEVELUP_MESSAGES[RANDOM.nextInt(LEVELUP_MESSAGES.length)].formatted(
				userSettings.doesLevelUpPing() ? member.getAsMention() : member.getUser().getAsTag(),
				newLevel);
	}

}
