package io.github.matyrobbrt.matybot.util.console;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.helpers.MessageFormatter;

import com.google.common.collect.ImmutableMap;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.LayoutBase;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

public final class ConsoleChannelLayout extends LayoutBase<ILoggingEvent> {

	private static final String UNKNOWN_EMOTE = ":radio_button:";

	private static final Map<Level, String> LEVEL_TO_EMOTE = ImmutableMap.<Level, String>builder()
			.put(Level.ERROR, ":red_square:").put(Level.WARN, ":yellow_circle:")
			.put(Level.INFO, ":white_medium_small_square:").put(Level.DEBUG, ":large_blue_diamond:")
			.put(Level.TRACE, ":small_orange_diamond:").build();

	private boolean prependLevelName = true;

	public void setPrependLevelName(final boolean prependLevelNameIn) {
		this.prependLevelName = prependLevelNameIn;
	}

	private static Object tryConvertMentionables(final Object obj) {
		if (obj instanceof IMentionable mentionable) {
			String name = null;
			if (obj instanceof User user) {
				name = user.getAsTag();
			} else if (obj instanceof Role role) {
				name = role.getName();
			} else if (obj instanceof GuildChannel channel) {
				name = channel.getName();
			} else if (obj instanceof Emote emote) {
				name = emote.getName();
			}
			if (name != null) {
				return String.format("%s (%s;`%s`)", mentionable.getAsMention(), name, mentionable.getIdLong());
			} else {
				return String.format("%s (`%s`)", mentionable.getAsMention(), mentionable.getIdLong());
			}
		} else if (obj instanceof Collection) {
			final Stream<Object> stream = ((Collection<?>) obj).stream()
					.map(ConsoleChannelLayout::tryConvertMentionables);
			if (obj instanceof Set) { return stream.collect(Collectors.toSet()); }
			return stream.toList();

		} else if (obj instanceof Map<?, ?> map) {
			return map.entrySet().stream()
					.map(entry -> new AbstractMap.SimpleImmutableEntry<>(tryConvertMentionables(entry.getKey()),
							tryConvertMentionables(entry.getValue())))
					.collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));

		} else if (obj instanceof final Map.Entry<?, ?> entry) {
			return new AbstractMap.SimpleImmutableEntry<>(tryConvertMentionables(entry.getKey()),
					tryConvertMentionables(entry.getValue()));

		}
		return obj;
	}

	@Override
	public String doLayout(final ILoggingEvent event) {
		final var builder = new StringBuilder();
		builder.append(LEVEL_TO_EMOTE.getOrDefault(event.getLevel(), UNKNOWN_EMOTE));
		if (prependLevelName) {
			builder.append(" ").append(event.getLevel().toString());
		}
		builder.append(" [**").append(event.getLoggerName());
		if (event.getMarker() != null) {
			builder.append("**/**").append(event.getMarker().getName());
		}
		builder.append("**] - ").append(getFormattedMessage(event)).append(CoreConstants.LINE_SEPARATOR);
		return builder.toString();
	}

	private static String getFormattedMessage(final ILoggingEvent event) {
		final Object[] arguments = event.getArgumentArray();
		if (event.getArgumentArray() != null) {
			var newArgs = new Object[arguments.length];
			for (var i = 0; i < arguments.length; i++) {
				newArgs[i] = tryConvertMentionables(arguments[i]);
			}

			return MessageFormatter.arrayFormat(event.getMessage(), newArgs).getMessage();
		}
		return event.getFormattedMessage();
	}
}
