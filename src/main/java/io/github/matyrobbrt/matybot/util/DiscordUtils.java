package io.github.matyrobbrt.matybot.util;

import java.util.regex.Pattern;

import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.entities.Message;

@UtilityClass
public class DiscordUtils {

	public static final Pattern MESSAGE_LINK_PATTERN = Pattern.compile("https://discord.com/channels/");

	public static String createMessageLink(final Message message) {
		return "https://discord.com/channels/" + message.getGuild().getIdLong() + "/" + message.getChannel().getIdLong()
				+ "/" + message.getIdLong();
	}

	public static void decodeMessageLink(final String link, MessageInfo consumer)
			throws MessageLinkException {
		final var matcher = MESSAGE_LINK_PATTERN.matcher(link);
		if (matcher.find()) {
			try {
				var origianlWithoutLink = matcher.replaceAll("");
				if (origianlWithoutLink.indexOf('/') > -1) {
					final long guildId = Long
							.parseLong(origianlWithoutLink.substring(0, origianlWithoutLink.indexOf('/')));
					origianlWithoutLink = origianlWithoutLink.substring(origianlWithoutLink.indexOf('/') + 1);
					if (origianlWithoutLink.indexOf('/') > -1) {
						final long channelId = Long
								.parseLong(origianlWithoutLink.substring(0, origianlWithoutLink.indexOf('/')));
						origianlWithoutLink = origianlWithoutLink.substring(origianlWithoutLink.indexOf('/') + 1);
						final long messageId = Long.parseLong(origianlWithoutLink);
						consumer.accept(guildId, channelId, messageId);
					} else {
						throw new MessageLinkException("Invalid Link");
					}
				} else {
					throw new MessageLinkException("Invalid Link");
				}
			} catch (NumberFormatException e) {
				throw new MessageLinkException(e);
			}
		} else {
			throw new MessageLinkException("Invalid Link");
		}
	}

	public static class MessageLinkException extends Exception {

		private static final long serialVersionUID = -2805786147679905681L;

		public MessageLinkException(Throwable e) {
			super(e);
		}

		public MessageLinkException(String message) {
			super(message);
		}

	}
	
	@FunctionalInterface
	public static interface MessageInfo {

		void accept(final long guildId, final long channelId, final long messageId);
		
	}
}
