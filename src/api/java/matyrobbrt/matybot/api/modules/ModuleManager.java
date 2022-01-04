package matyrobbrt.matybot.api.modules;

import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;

import matyrobbrt.matybot.MatyBot;
import net.dv8tion.jda.api.JDA;

public class ModuleManager {

	private final JDA bot;
	private final Map<Class<?>, Module> modules = new HashedMap<>();

	public ModuleManager(final JDA bot) {
		this.bot = bot;
	}

	public void addModule(Module module) {
		addModule(module.getClass(), module);
	}

	public void addModule(Class<?> moduleClazz, Module module) {
		this.modules.put(moduleClazz, module);
	}

	public JDA getBot() { return bot; }

	public void register() {
		modules.forEach((clz, module) -> {
			if (module.isEnabled()) {
				module.register();
				MatyBot.LOGGER.info("The module {} is now up and running!", module.getClass().getSimpleName());
			} else {
				MatyBot.LOGGER.warn("The module {} disabled! Its features will not be available.",
						module.getClass().getSimpleName());
			}
		});
	}
}
