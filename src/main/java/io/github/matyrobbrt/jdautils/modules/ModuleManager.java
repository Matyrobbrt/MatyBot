package io.github.matyrobbrt.jdautils.modules;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.JDA;

public class ModuleManager {

	private final JDA bot;
	private final Logger logger;
	private final Map<Class<?>, Module> modules = new HashMap<>();

	public ModuleManager(final JDA bot, final Logger logger) {
		this.bot = bot;
		this.logger = logger;
	}

	public ModuleManager(final JDA bot) {
		this.bot = bot;
		this.logger = LoggerFactory.getLogger(bot.getSelfUser().getName());
	}

	public void addModule(Module module) {
		addModule(module.getClass(), module);
	}

	public void addModule(Class<?> moduleClass, Module module) {
		this.modules.put(moduleClass, module);
	}

	public JDA getBot() { return bot; }

	public void register() {
		modules.forEach((clz, module) -> {
			if (module.isEnabled()) {
				module.register();
				logger.info("The module {} is now up and running!", module.getClass().getSimpleName());
			} else {
				logger.warn("The module {} disabled! Its features will not be available.",
						module.getClass().getSimpleName());
			}
		});
	}
}
