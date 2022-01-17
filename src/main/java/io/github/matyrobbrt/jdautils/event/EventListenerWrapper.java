package io.github.matyrobbrt.jdautils.event;

import java.util.function.Supplier;

import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

/**
 * This class wraps {@link EventListener}s for use with
 * {@link AnnotatedEventManager}s
 * 
 * @author matyrobbrt
 *
 */
public class EventListenerWrapper {

	private final EventListener listener;
	private final Supplier<EventListener> listenerSupplier;

	public EventListenerWrapper(final EventListener listener) {
		this.listener = listener;
		this.listenerSupplier = null;
	}

	public EventListenerWrapper(final Supplier<EventListener> listenerSupplier) {
		this.listener = null;
		this.listenerSupplier = listenerSupplier;
	}

	@SubscribeEvent
	public void genericEvent(final GenericEvent e) {
		getListener().onEvent(e);
	}

	public EventListener getListener() {
		return listener == null ? listenerSupplier.get() : listener;
	}

}
