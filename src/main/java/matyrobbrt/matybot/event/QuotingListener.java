package matyrobbrt.matybot.event;

import java.awt.Color;
import java.util.regex.Pattern;

import matyrobbrt.matybot.MatyBot;
import matyrobbrt.matybot.util.MarkdownUtils;
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

		final var matcher = MESSAGE_LINK_PATTERN.matcher(msg[0]);
		if (matcher.find()) {
			try {
				var origianlWithoutLink = matcher.replaceAll("");
				final long guildId = Long.parseLong(origianlWithoutLink.substring(0, origianlWithoutLink.indexOf('/')));
				origianlWithoutLink = origianlWithoutLink.substring(origianlWithoutLink.indexOf('/') + 1);
				final long channelId = Long
						.parseLong(origianlWithoutLink.substring(0, origianlWithoutLink.indexOf('/')));
				origianlWithoutLink = origianlWithoutLink.substring(origianlWithoutLink.indexOf('/') + 1);
				final long messageId = Long.parseLong(origianlWithoutLink);
				MatyBot.getInstance().getJDA().getGuildById(guildId).getTextChannelById(channelId)
						.retrieveMessageById(messageId).queue(message -> {
							event.getChannel().sendMessageEmbeds(quote(message, event.getMember())).queue();
							if (msg.length == 1) {
								originalMsg.delete().reason("Quote successful").queue();
							}
						});
			} catch (NumberFormatException e) {
				// Do nothing
			}
		}
	}

	private static boolean isStringQuoting(final String string) {
		// The zero-width space
		return string.equalsIgnoreCase(".") || string.equalsIgnoreCase("​");
	}

	public static MessageEmbed quote(final Message message, final Member quoter) {
		final var msgLink = MarkdownUtils.createMessageLink(message);
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
