package io.github.matyrobbrt.jdautils.modules;

import java.util.function.BooleanSupplier;

import io.github.matyrobbrt.jdautils.event.EventListenerWrapper;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Module extends ListenerAdapter {

	private final BooleanSupplier isEnabled;
	protected final JDA bot;

	public Module(final BooleanSupplier isEnabled, final JDA bot) {
		this.isEnabled = isEnabled;
		this.bot = bot;
	}

	public boolean isEnabled() {
		return isEnabled.getAsBoolean();
	}

	public void register() {
		if (isEnabled.getAsBoolean()) {
			bot.addEventListener(
					bot.getEventManager() instanceof AnnotatedEventManager ? new EventListenerWrapper(this) : this);
		}
	}

}
