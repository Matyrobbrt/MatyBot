package matyrobbrt.matybot.modules.logging.events;

import java.awt.Color;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;

import matyrobbrt.matybot.MatyBot;
import matyrobbrt.matybot.modules.logging.LoggingModule;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

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
		if (containsScam(msg)) {
			final var member = msg.getMember();
			final var msgChannel = msg.getTextChannel();
			final var guild = msg.getGuild();
			final var mutedRoleID = MatyBot.getConfigForGuild(event.getGuild()).mutedRole;
			msg.delete().queue($ -> {
				final var embed = new EmbedBuilder().setTitle("Scam link detected!")
						.setDescription(String.format(
								"User %s sent a scam link in %s! Their message was deleted, and they were muted!",
								member.getAsMention(), msgChannel.getAsMention()))
						.setColor(Color.RED).setTimestamp(Instant.now()).setFooter("User ID: " + member.getIdLong())
						.setThumbnail(member.getEffectiveAvatarUrl());
				LoggingModule.getLoggingChannel(guild).sendMessageEmbeds(embed.build()).queue();
				msgChannel
						.sendMessage(String.format("%s did you *really* think that would work?", member.getAsMention()))
						.queue();
				guild.addRoleToMember(member, guild.getRoleById(mutedRoleID)).queue();
			});
		}
	}

	@Override
	public void onMessageUpdate(MessageUpdateEvent event) {
		if (!event.isFromGuild()) { return; }

		final var msg = event.getMessage();
		if (containsScam(msg)) {
			final var member = msg.getMember();
			final var msgChannel = msg.getTextChannel();
			final var guild = msg.getGuild();
			final var mutedRoleID = MatyBot.getConfigForGuild(guild).mutedRole;
			msg.delete().queue($ -> {
				final var embed = new EmbedBuilder().setTitle("Scam link detected!").setDescription(String.format(
						"User %s sent a scam link in %s, by editing an old message! Their message was deleted, and they were muted!",
						member.getAsMention(), msgChannel.getAsMention())).setColor(Color.RED)
						.setTimestamp(Instant.now()).setFooter("User ID: " + member.getIdLong())
						.setThumbnail(member.getEffectiveAvatarUrl());
				LoggingModule.getLoggingChannel(guild).sendMessageEmbeds(embed.build()).queue();
				msgChannel
						.sendMessage(String.format("%s did you *really* think that would work?", member.getAsMention()))
						.queue();
				guild.addRoleToMember(member, guild.getRoleById(mutedRoleID)).queue();
			});
		}
	}

	public static boolean containsScam(final Message message) {
		final String msgContent = message.getContentRaw().toLowerCase();
		for (final var link : SCAM_LINKS) {
			if (msgContent.contains(link)) { return true; }
		}
		return false;
	}

	private static void setupScamLinks() {
		MatyBot.LOGGER.debug("Setting up scam links! Receiving data from {}.", SCAM_LINKS_DATA_URL);
		try (var is = new URL(SCAM_LINKS_DATA_URL).openStream()) {
			final String result = new String(is.readAllBytes(), StandardCharsets.UTF_8);
			GSON.fromJson(result, JsonArray.class).forEach(link -> SCAM_LINKS.add(link.getAsString()));
		} catch (final IOException e) {
			MatyBot.LOGGER.error("Error while setting up scam links!", e);
		}
	}
}
