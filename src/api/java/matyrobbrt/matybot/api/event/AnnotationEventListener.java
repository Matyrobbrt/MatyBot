package matyrobbrt.matybot.api.event;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.Nonnull;

import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReconnectedEvent;
import net.dv8tion.jda.api.events.ResumedEvent;
import net.dv8tion.jda.api.events.UpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import net.dv8tion.jda.internal.utils.ClassWalker;

/**
 * For a class extending this to work, you <b>HAVE TO</b> override
 * {@link #onEventHandleAnnotation(GenericEvent)}, call the super and annotate
 * the method with {@link SubscribeEvent}
 * 
 * @author matyrobbrt
 *
 */
public class AnnotationEventListener extends ListenerAdapter {

	private static final MethodHandles.Lookup lookup = MethodHandles.lookup();
	private static final ConcurrentMap<Class<?>, MethodHandle> methods = new ConcurrentHashMap<>();
	private static final Set<Class<?>> unresolved;
	static {
		unresolved = ConcurrentHashMap.newKeySet();
		Collections.addAll(unresolved, Object.class, // Objects aren't events
				Event.class, // onEvent is final and would never be found
				UpdateEvent.class, // onGenericUpdate has already been called
				GenericEvent.class // onGenericEvent has already been called
		);
	}

	@SuppressWarnings("deprecation")
	public void onEventHandleAnnotation(@Nonnull GenericEvent event) {
		onGenericEvent(event);
		if (event instanceof UpdateEvent) {
			onGenericUpdate((UpdateEvent<?, ?>) event);
		}

		// TODO: Remove once deprecated methods are removed
		if (event instanceof ResumedEvent resEvent) {
			onResume(resEvent);
		} else if (event instanceof ReconnectedEvent recEvent) {
			onReconnect(recEvent);
		}

		for (Class<?> clazz : ClassWalker.range(event.getClass(), GenericEvent.class)) {
			if (unresolved.contains(clazz)) {
				continue;
			}
			MethodHandle mh = methods.computeIfAbsent(clazz, AnnotationEventListener::findMethod);
			if (mh == null) {
				unresolved.add(clazz);
				continue;
			}

			try {
				mh.invoke(this, event);
			} catch (Throwable throwable) {
				if (throwable instanceof RuntimeException re) { throw re; }
				if (throwable instanceof Error e) { throw e; }
				throw new IllegalStateException(throwable);
			}
		}
	}

	private static MethodHandle findMethod(Class<?> clazz) {
		String name = clazz.getSimpleName();
		MethodType type = MethodType.methodType(Void.TYPE, clazz);
		try {
			name = "on" + name.substring(0, name.length() - "Event".length());
			return lookup.findVirtual(AnnotationEventListener.class, name, type);
		} catch (NoSuchMethodException | IllegalAccessException ignored) {} // this means this is probably a custom
		// event!
		return null;
	}

}
