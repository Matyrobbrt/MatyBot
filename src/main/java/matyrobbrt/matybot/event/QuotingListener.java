package matyrobbrt.matybot.event;

import java.util.regex.Pattern;

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
		if (originalMsg.getMessageReference() == null) { return; }
		if (originalMsg.getContentRaw().equalsIgnoreCase(".")) {
			final var referencedMessage = originalMsg.getMessageReference().getMessage();
			event.getChannel().sendMessageEmbeds(quote(referencedMessage, event.getMember())).queue();
			originalMsg.delete().reason("Quote successful").queue();
		}

		// final var matcher =
		// MESSAGE_LINK_PATTERN.matcher(originalMsg.getContentRaw());
	}

	public static MessageEmbed quote(final Message message, final Member quoter) {
		final var msgLink = MarkdownUtils.createMessageLink(message);
		return new EmbedBuilder().setTimestamp(message.getTimeCreated())
				.appendDescription(MarkdownUtil.maskedLink("Quote ➤ ", msgLink))
				.appendDescription(message.getContentRaw())
				.setAuthor(message.getAuthor().getAsTag(), msgLink, message.getAuthor().getEffectiveAvatarUrl())
				.setFooter(quoter.getUser().getAsTag() + " quoted", quoter.getEffectiveAvatarUrl()).build();
	}

}
