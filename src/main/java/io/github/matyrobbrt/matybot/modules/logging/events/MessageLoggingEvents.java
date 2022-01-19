package io.github.matyrobbrt.matybot.modules.logging.events;

import java.awt.Color;
import java.time.Instant;

import io.github.matyrobbrt.matybot.MatyBot;
import io.github.matyrobbrt.matybot.modules.logging.LoggingModule;
import io.github.matyrobbrt.matybot.util.DiscordUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.MarkdownUtil;

public class MessageLoggingEvents extends ListenerAdapter {

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if (!event.isFromGuild()) { return; }
		MatyBot.messageCache().computeMessageData(event.getMessage());
	}

	@Override
	public void onMessageUpdate(MessageUpdateEvent event) {
		if (!event.isFromGuild()) { return; }
		final var message = event.getMessage();
		final var oldContent = MatyBot.messageCache().computeMessageData(message).getContent();
		final var author = event.getAuthor();
		final var embed = new EmbedBuilder()
				.setDescription("**A message sent by %s in %s has been edited**. %s".formatted(author.getAsMention(),
						event.getChannel().getAsMention(),
						MarkdownUtil.maskedLink("Jump to message.", DiscordUtils.createMessageLink(message))))
				.setAuthor(author.getAsTag(), null, author.getAvatarUrl()).setColor(Color.BLUE)
				.setFooter("Author ID: %s".formatted(author.getIdLong())).setTimestamp(Instant.now())
				.addField("Before", oldContent, false).addField("After", event.getMessage().getContentRaw(), false);
		LoggingModule.inLoggingChannel(event.getGuild(),
				loggingChannel -> loggingChannel.sendMessageEmbeds(embed.build()).queue());
		MatyBot.messageCache().getMessageData(event).setContent(message.getContentRaw());
		MatyBot.messageCache().setDirty();
	}

	@Override
	public void onMessageDelete(MessageDeleteEvent event) {
		if (!event.isFromGuild()) { return; }
		final var msgData = MatyBot.messageCache().getMessageData(event);
		if (msgData == null) {
			return;
		}
		final var oldContent = msgData.getContent();
		final var author = MatyBot.getInstance().getUserById(msgData.getAuthorId());
		final var embed = new EmbedBuilder()
				.setDescription("""
						**A message sent by %s in %s has been deleted**.
						%s""".formatted(author == null ? msgData.getAuthorId() : author.getAsMention(),
						event.getChannel().getAsMention(), oldContent))
				.setColor(Color.RED)
				.setFooter("Author ID: %s | Message ID: %s".formatted(msgData.getAuthorId(), event.getMessageId()))
				.setTimestamp(Instant.now());
		if (author != null) {
			embed.setAuthor(author.getAsTag(), null, author.getAvatarUrl());
		}
		LoggingModule.inLoggingChannel(event.getGuild(),
				loggingChannel -> loggingChannel.sendMessageEmbeds(embed.build()).queue());
	}
}
