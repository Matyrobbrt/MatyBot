package matyrobbrt.matybot.api.event;

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

	public EventListenerWrapper(final EventListener listener) {
		this.listener = listener;
	}

	@SubscribeEvent
	public void genericEvent(final GenericEvent e) {
		listener.onEvent(e);
	}

}
