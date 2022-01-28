package io.github.matyrobbrt.matybot.d4j;

import java.awt.Color;
import java.time.Instant;
import java.util.Optional;

import discord4j.core.DiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageDeleteEvent;
import discord4j.core.event.domain.message.MessageUpdateEvent;
import discord4j.gateway.intent.Intent;
import discord4j.gateway.intent.IntentSet;
import io.github.matyrobbrt.matybot.MatyBot;
import io.github.matyrobbrt.matybot.modules.logging.LoggingModule;
import io.github.matyrobbrt.matybot.util.BotUtils;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import reactor.core.publisher.Mono;

public class MatyBotD4J {

	@Getter
	public static DiscordClient client;

	private static boolean instantiated = false;

	public static void main(String[] args) {
		if (instantiated) { return; }
		instantiated = true;

		client = DiscordClient.create(BotUtils.getBotToken());

		final var gatewayBootstrap = client.gateway().setEnabledIntents(IntentSet.of(Intent.values())).login();

		gatewayBootstrap.subscribe(gateway -> {
			gateway.on(ReadyEvent.class, event -> {
				MatyBot.LOGGER.warn("The D4J version of myself is ready to work! Logged in as {}",
						event.getSelf().getTag());
				return Mono.empty();
			}).subscribe();

			gateway.on(MessageDeleteEvent.class, event -> {
				event.getGuild().subscribe(guild -> {
					event.getMessage().ifPresent(message -> {
						message.getChannel().subscribe(channel -> {
							final var oldContent = event.getMessage().isPresent()
									? event.getMessage().get().getContent()
									: "The old message content could not be determined!";
							final var authorId = message.getAuthor().flatMap(u -> Optional.of(u.getId().asLong()))
									.orElse(0l);
							final var author = MatyBot.getInstance().getUserById(authorId);
							final var embed = new EmbedBuilder()
									.setDescription("""
											**A message sent by %s in %s has been deleted**.
											%s""".formatted(
											author == null ? "The author could not be determined"
													: author.getAsMention(),
											event.getChannel().block().getMention(), oldContent))
									.setColor(Color.RED).setFooter("Author ID: %s | Message ID: %s".formatted(authorId,
											event.getMessageId().asLong()))
									.setTimestamp(Instant.now());
							if (author != null) {
								embed.setAuthor(author.getAsTag(), null, author.getAvatarUrl());
							}
							LoggingModule.inLoggingChannel(guild.getId().asLong(),
									loggingChannel -> loggingChannel.sendMessageEmbeds(embed.build()).queue());
						});
					});
				}, /* In this case, the message is from DMs */ e -> {});
				return Mono.empty();
			}).subscribe();

			gateway.on(MessageUpdateEvent.class, event -> {
				event.getGuild().subscribe(guild -> {
					event.getMessage().subscribe(message -> {
						message.getChannel().subscribe(channel -> {
							final var oldContent = event.getOld().flatMap(m -> Optional.of(m.getContent()))
									.orElse("The old message content could not be determined.");
							final var authorId = message.getAuthor().flatMap(u -> Optional.of(u.getId().asLong()))
									.orElse(0l);
							final var author = MatyBot.getInstance().getUserById(authorId);
							final var embed = new EmbedBuilder()
									.setDescription("**A message sent by %s in %s has been edited**. %s".formatted(
											author == null ? authorId : author.getAsMention(), channel.getMention(),
											MarkdownUtil.maskedLink("Jump to message.", createMessageLink(message))))
									.setAuthor(author.getAsTag(), null, author.getAvatarUrl()).setColor(Color.BLUE)
									.setFooter("Author ID: %s".formatted(author.getIdLong()))
									.setTimestamp(Instant.now()).addField("Before", oldContent, false)
									.addField("After", message.getContent(), false);
							LoggingModule.inLoggingChannel(guild.getId().asLong(),
									loggingChannel -> loggingChannel.sendMessageEmbeds(embed.build()).queue());
						});
					});
				}, /* In this case, the message is from DMs */ e -> {});
				return Mono.empty();
			}).subscribe();
		});
	}

	public static String createMessageLink(final discord4j.core.object.entity.Message message) {
		return "https://discord.com/channels/" + message.getGuildId().flatMap(s -> Optional.of(s.asLong())).orElse(0l)
				+ "/" + message.getChannelId().asLong() + "/" + message.getId().asLong();
	}
}
