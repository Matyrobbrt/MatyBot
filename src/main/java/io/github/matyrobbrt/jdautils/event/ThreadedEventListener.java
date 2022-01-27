package io.github.matyrobbrt.jdautils.event;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.EventListener;

@Slf4j
public record ThreadedEventListener(EventListener listener, Executor threadPool) implements EventListener {

	public ThreadedEventListener(@NonNull final EventListener listener) {
		this(listener, Executors.newSingleThreadExecutor(r -> {
			final var thread = new Thread(r);
			thread.setDaemon(true);
			return thread;
		}));
	}

	@Override
	public void onEvent(GenericEvent event) {
		try {
			threadPool.execute(() -> listener.onEvent(event));
		} catch (Exception e) {
			log.error("Error while executing threaded event!", e);
		}
	}

}
