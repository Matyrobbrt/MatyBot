package io.github.matyrobbrt.matybot.util;

import java.awt.Color;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Joiner;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jagrosh.jdautilities.command.SlashCommandEvent;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.matyrobbrt.matybot.MatyBot;
import io.github.matyrobbrt.matybot.util.DiscordUtils.MessageLinkException;
import io.github.matyrobbrt.matybot.util.database.dao.StickyRoles;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

@UtilityClass
public class BotUtils {

	public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

	public static final Dotenv DOT_ENV = Dotenv.load();

	public static String getBotToken() {
		return DOT_ENV.get("BOT_TOKEN");
	}

	public static String getGithubToken() {
		return DOT_ENV.get("GITHUB_TOKEN");
	}

	public static boolean isUserSelf(long userId) {
		return userId == MatyBot.getInstance().getJDA().getSelfUser().getIdLong();
	}

	/**
	 * Get the users roles when they leave the guild with the user leave event.
	 *
	 * @param  guild  The guild we are in.
	 * @param  userID The users ID.
	 * @return        A list of the roles the user had to save to a file for when
	 *                they return.
	 */
	@NotNull
	public static List<Role> getOldUserRoles(final Guild guild, final Long userID) {
		return MatyBot.database().withExtension(StickyRoles.class, roles -> roles.getRoles(userID, guild.getIdLong()))
				.stream().map(guild::getRoleById).filter(Objects::nonNull).toList();
	}

	public static void clearOldUserRoles(final long userID, final long guildId) {
		MatyBot.database().useExtension(StickyRoles.class, roles -> roles.clear(userID, guildId));
	}

	/**
	 * Get a non-null string from an OptionMapping.
	 *
	 * @param  option an OptionMapping to get as a string - may be null
	 * @return        the option mapping as a string, or an empty string if the
	 *                mapping was null
	 */
	public static String getOptionOrEmpty(@Nullable OptionMapping option) {
		return Optional.ofNullable(option).map(OptionMapping::getAsString).orElse("");
	}

	public static <T> T getOptionOr(@Nullable OptionMapping option, Function<OptionMapping, T> getter, T orElse) {
		return Optional.ofNullable(option).map(getter::apply).orElse(orElse);
	}

	/**
	 * Gets an argument from a slash command as a string.
	 *
	 * @param  event the slash command event
	 * @param  name  the name of the option
	 * @return       the option's value as a string, or an empty string if the
	 *               option had no value
	 */
	public static String getArgumentOrEmpty(SlashCommandEvent event, String name) {
		return getOptionOrEmpty(event.getOption(name));
	}

	public static int getIntArgumentOr(SlashCommandEvent event, String name, int orElse) {
		return getOptionOr(event.getOption(name), m -> (int) m.getAsDouble(), orElse);
	}

	public static void scheduleTask(Runnable task, long delay) {
		Constants.TIMER.schedule(new TimerTask() {

			@Override
			public void run() {
				task.run();
			}
		}, delay);
	}

	public static Color generateRandomColor() {
		final var red = Constants.RANDOM.nextFloat();
		final var green = Constants.RANDOM.nextFloat();
		final var blue = Constants.RANDOM.nextFloat();
		return new Color(red, green, blue);
	}

	public static Message getMessageByLink(final String link) throws MessageLinkException {
		final AtomicReference<Message> returnAtomic = new AtomicReference<>(null);
		DiscordUtils.decodeMessageLink(link, (guildId, channelId, messageId) -> {
			MatyBot.getInstance().getGuildOptional(guildId).ifPresent(guild -> {
				final var channel = guild.getTextChannelById(channelId);
				if (channel != null) {
					returnAtomic.set(channel.retrieveMessageById(messageId).complete());
				}
			});
		});
		return returnAtomic.get();
	}

	public static boolean memberHasRole(final Member member, List<Long> roles) {
		return member.getRoles().stream().anyMatch(role -> roles.contains(role.getIdLong()));
	}

	public static <T extends Thread> T setDaemon(@NonNull final T thread) {
		thread.setDaemon(true);
		return thread;
	}

	public static final Joiner LINE_JOINER = Joiner.on("\n");

	@UtilityClass
	public static final class Markers {

		public static final Marker EVENTS = MarkerFactory.getMarker("Events");

		public static final Marker DATABASE = MarkerFactory.getMarker("Database");

	}

}
