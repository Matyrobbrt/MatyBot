package io.github.matyrobbrt.matybot.event;

import java.awt.Color;
import java.time.Instant;

import io.github.matyrobbrt.matybot.MatyBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MiscEvents extends ListenerAdapter {

	@Override
	public void onShutdown(ShutdownEvent event) {
		MatyBot.nbtDatabase().setDirtyAndSave();
	}

	@Override
	public void onReady(ReadyEvent event) {
		MatyBot.LOGGER.warn("I am ready to work! Logged in as {}", event.getJDA().getSelfUser().getAsTag());

		final var consoleChannel = event.getJDA().getTextChannelById(MatyBot.generalConfig().consoleChannelId);
		if (consoleChannel != null) {
			consoleChannel
					.sendMessage(
							"<--------------------------------------------------------------------------------------->")
					.setEmbeds(new EmbedBuilder()
							.setAuthor(event.getJDA().getSelfUser().getName(), null,
									event.getJDA().getSelfUser().getAvatarUrl())
							.setColor(Color.GREEN).setTitle("Start-up").setTimestamp(Instant.now())
							.setDescription(
									"The bot has been started up! You should see most of the console logging happening in this channel.")
							.build())
					.queue();
		}
	}

}
