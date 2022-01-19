package io.github.matyrobbrt.matybot.event;

import io.github.matyrobbrt.matybot.MatyBot;
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
	}

}
