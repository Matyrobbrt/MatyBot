package io.github.matyrobbrt.matybot.modules.logging.events;

import java.awt.Color;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.StreamSupport;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import io.github.matyrobbrt.matybot.MatyBot;
import io.github.matyrobbrt.matybot.modules.logging.LoggingModule;
import lombok.experimental.ExtensionMethod;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.MarkdownUtil;

@ExtensionMethod(LoggingModule.class)
public class ScamDetector extends ListenerAdapter {

	public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
	public static final String SCAM_LINKS_DATA_URL = "https://phish.sinking.yachts/v2/all";

	public static final List<String> SCAM_LINKS = Collections.synchronizedList(new ArrayList<>());

	static {
		new Thread(ScamDetector::setupScamLinks, "Scam link collector").start();
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if (!event.isFromGuild()) { return; }
		final var msg = event.getMessage();
		final var member = msg.getMember();
		if (member == null || msg.getAuthor().isBot() || msg.getAuthor().isSystem()
				|| member.hasPermission(Permission.MANAGE_CHANNEL)) {
			return;
		}
		if (containsScam(msg)) {
			final var guild = msg.getGuild();
			final var embed = getLoggingEmbed(msg, "");
			msg.delete().reason("Scam link").queue($ -> {
				event.getGuild().inLoggingChannel(channel -> channel.sendMessageEmbeds(embed).queue());
				mute(guild, member);
			});
		}
	}

	@Override
	public void onMessageUpdate(MessageUpdateEvent event) {
		if (!event.isFromGuild()) { return; }
		final var msg = event.getMessage();
		final var member = msg.getMember();
		if (member == null || msg.getAuthor().isBot() || msg.getAuthor().isSystem()
				|| member.hasPermission(Permission.MANAGE_CHANNEL)) {
			return;
		}
		if (containsScam(msg)) {
			final var guild = msg.getGuild();
			final var embed = getLoggingEmbed(msg, ", by editing an old message");
			msg.delete().reason("Scam link").queue($ -> {
				event.getGuild().inLoggingChannel(channel -> channel.sendMessageEmbeds(embed).queue());
				mute(guild, member);
			});
		}
	}

	private static void mute(final Guild guild, final Member member) {
		// Timeout for 14 days instead of muting
		member.timeoutFor(Duration.ofDays(14)).reason("Sent a scam link!").queue(v -> {}, e -> {
			LoggingModule.logException(guild.getIdLong(),
					"timing out %s for sending a scam link!".formatted(member.getAsMention()), e);
			MatyBot.LOGGER.error(
					"I could not timeout the user {} in guild {} for sending a scam link due to an exception.",
					member.getUser().getAsTag(), member.getGuild().getName(), e);
		});

	}

	private static MessageEmbed getLoggingEmbed(final Message message, final String extraDescription) {
		final var member = message.getMember();
		return new EmbedBuilder().setTitle("Scam link detected!")
				.setDescription(
						"User %s sent a scam link in %s%s. Their message was deleted, and they were muted.".formatted(
								member.getUser().getAsTag(), message.getTextChannel().getAsMention(), extraDescription))
				.addField("Message Content", MarkdownUtil.codeblock(message.getContentRaw()), false).setColor(Color.RED)
				.setTimestamp(Instant.now()).setFooter("User ID: " + member.getIdLong())
				.setThumbnail(member.getEffectiveAvatarUrl()).build();
	}

	public static boolean containsScam(final Message message) {
		final String msgContent = message.getContentRaw().toLowerCase(Locale.ROOT);
		synchronized (SCAM_LINKS) {
			for (final var link : SCAM_LINKS) {
				if (msgContent.contains(link)) { return true; }
			}
		}
		return false;
	}

	private static void setupScamLinks() {
		SCAM_LINKS.clear();
		MatyBot.LOGGER.debug("Setting up scam links! Receiving data from {}.", SCAM_LINKS_DATA_URL);
		try (var is = new URL(SCAM_LINKS_DATA_URL).openStream()) {
			final String result = new String(is.readAllBytes(), StandardCharsets.UTF_8);
			SCAM_LINKS.addAll(StreamSupport.stream(GSON.fromJson(result, JsonArray.class).spliterator(), false)
					.map(JsonElement::getAsString).filter(s -> !s.contains("discordapp.co")).toList());
		} catch (final IOException e) {
			MatyBot.LOGGER.error("Error while setting up scam links!", e);
		}
	}
}
