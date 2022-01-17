package io.github.matyrobbrt.matybot.event;

import java.awt.Color;
import java.util.regex.Pattern;

import io.github.matyrobbrt.matybot.util.BotUtils;
import io.github.matyrobbrt.matybot.util.DiscordUtils;
import io.github.matyrobbrt.matybot.util.DiscordUtils.MessageLinkException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.MarkdownUtil;

public class QuotingListener extends ListenerAdapter {

	public static final Pattern MESSAGE_LINK_PATTERN = Pattern.compile("https://discord.com/channels/");

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if (!event.isFromGuild()) { return; }
		final var originalMsg = event.getMessage();
		if (originalMsg.getMessageReference() != null && isStringQuoting(originalMsg.getContentRaw())) {
			final var referencedMessage = originalMsg.getMessageReference().getMessage();
			event.getChannel().sendMessageEmbeds(quote(referencedMessage, event.getMember())).queue();
			originalMsg.delete().reason("Quote successful").queue();
			return;
		}

		final String[] msg = originalMsg.getContentRaw().split(" ");
		if (msg.length < 1) { return; }

		final var matcher = DiscordUtils.MESSAGE_LINK_PATTERN.matcher(msg[0]);
		if (matcher.find()) {
			try {
				final var message = BotUtils.getMessageByLink(msg[0]);
				if (message != null) {
					event.getChannel().sendMessageEmbeds(quote(message, event.getMember())).queue();
					if (msg.length == 1) {
						originalMsg.delete().reason("Quote successful").queue();
					}
				}
			} catch (MessageLinkException e) {
				// Do nothing
			}
		}
	}

	private static boolean isStringQuoting(final String string) {
		// The zero-width space
		return string.equalsIgnoreCase(".") || string.equalsIgnoreCase("​");
	}

	public static MessageEmbed quote(final Message message, final Member quoter) {
		final var msgLink = DiscordUtils.createMessageLink(message);
		final var embed = new EmbedBuilder().setTimestamp(message.getTimeCreated()).setColor(Color.DARK_GRAY)
				.setAuthor(message.getAuthor().getAsTag(), msgLink, message.getAuthor().getEffectiveAvatarUrl());
		if (!message.getContentRaw().isBlank()) {
			embed.appendDescription(MarkdownUtil.maskedLink("Quote ➤ ", msgLink))
					.appendDescription(message.getContentRaw());
		} else {
			embed.appendDescription(MarkdownUtil.maskedLink("Jump to quoted message.", msgLink));
		}
		if (quoter.getIdLong() != message.getMember().getIdLong()) {
			embed.setFooter(quoter.getUser().getAsTag() + " quoted", quoter.getEffectiveAvatarUrl());
		}
		if (!message.getAttachments().isEmpty()) {
			embed.setImage(message.getAttachments().get(0).getUrl());
		}
		return embed.build();
	}

}
