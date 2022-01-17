package io.github.matyrobbrt.matybot.api.modules;

import java.util.function.Supplier;

import io.github.matyrobbrt.matybot.api.event.AnnotationEventListener;
import io.github.matyrobbrt.matybot.api.event.EventListenerWrapper;
import net.dv8tion.jda.api.JDA;

public class Module extends AnnotationEventListener {

	private final Supplier<Boolean> isEnabled;
	protected final JDA bot;

	public Module(final Supplier<Boolean> isEnabled, final JDA bot) {
		this.isEnabled = isEnabled;
		this.bot = bot;
	}

	public boolean isEnabled() { return isEnabled.get(); }

	public void register() {
		if (isEnabled.get()) {
			bot.addEventListener(new EventListenerWrapper(this));
		}
	}

}
