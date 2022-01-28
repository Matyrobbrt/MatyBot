package io.github.matyrobbrt.matybot.d4j;

import io.github.matyrobbrt.matybot.D4JBridge;

public class D4JBridgeImpl extends io.github.matyrobbrt.matybot.D4JBridge {

	public static void setupInstance() {
		D4JBridge.setInstance(new D4JBridgeImpl());
	}

	@Override
	public void executeMain(String[] args) {
		MatyBotD4J.main(args);
	}

}
