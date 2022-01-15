package matyrobbrt.matybot.util;

import java.awt.Color;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.TimerTask;
import java.util.function.Consumer;
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
import matyrobbrt.matybot.MatyBot;
import matyrobbrt.matybot.util.database.dao.StickyRoles;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.internal.requests.restaction.MessageActionImpl;

public class BotUtils {

	public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

	public static final Dotenv DOT_ENV = Dotenv.load();

	public static String getBotToken() { return DOT_ENV.get("BOT_TOKEN"); }

	public static String getGithubToken() { return DOT_ENV.get("GITHUB_TOKEN"); }

	public static boolean isUserSelf(long userId) {
		return userId == MatyBot.instance.getBot().getSelfUser().getIdLong();
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

	/**
	 * Calls the given consumer only if the text channel with the given ID is known
	 * to the bot.
	 *
	 * @param channelID The channel ID
	 * @param consumer  The consumer of the text channel
	 * @see             net.dv8tion.jda.api.JDA#getTextChannelById(long)
	 */
	public static void getChannelIfPresent(final long channelID, final Consumer<TextChannel> consumer) {
		final var channel = MatyBot.instance.getBot().getTextChannelById(channelID);
		if (channel != null) {
			consumer.accept(channel);
		}
	}

	/**
	 * Creates a message that should be sent in the provided {@code channel}
	 * 
	 * @param  channel the channel in which the message will be sent when
	 *                 {@link RestAction#queue()} will be called
	 * 
	 * @return         the created message
	 */
	public static MessageAction createMessage(final TextChannel channel) {
		return new MessageActionImpl(MatyBot.getJDA(), null, channel);
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

	public static final Joiner LINE_JOINER = Joiner.on("\n");

	public static final class Markers {

		public static final Marker EVENTS = MarkerFactory.getMarker("Events");

		public static final Marker DATABASE = MarkerFactory.getMarker("Database");

	}

}
