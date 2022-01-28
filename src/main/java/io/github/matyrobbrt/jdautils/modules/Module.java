package io.github.matyrobbrt.jdautils.modules;

import java.util.function.BooleanSupplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.matyrobbrt.jdautils.event.EventListenerWrapper;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@RequiredArgsConstructor
public class Module extends ListenerAdapter {

	private final BooleanSupplier isEnabled;
	protected final JDA bot;
	@Getter
	@NonNull
	private final Logger logger;

	public Module(final BooleanSupplier isEnabled, final JDA bot) {
		this.isEnabled = isEnabled;
		this.bot = bot;
		this.logger = LoggerFactory.getLogger(getClass());
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
