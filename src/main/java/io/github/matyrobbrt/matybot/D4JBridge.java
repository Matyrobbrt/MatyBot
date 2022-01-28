package io.github.matyrobbrt.matybot;

import java.util.function.Consumer;

import lombok.NonNull;

public abstract class D4JBridge {

	@NonNull
	public static D4JBridge instance;

	public static void setInstance(final D4JBridge newBridge) {
		if (instance == null) {
			instance = newBridge;
		}
	}

	public static void executeOnInstance(final Consumer<D4JBridge> consumer) {
		if (instance != null) {
			consumer.accept(instance);
		}
	}

	public abstract void executeMain(final String[] args);

}
