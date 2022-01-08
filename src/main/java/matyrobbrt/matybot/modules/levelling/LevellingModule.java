package matyrobbrt.matybot.modules.levelling;

import matyrobbrt.matybot.MatyBot;
import net.dv8tion.jda.api.JDA;

public class LevellingModule extends matyrobbrt.matybot.api.modules.Module {

	public LevellingModule(final JDA bot) {
		super(() -> MatyBot.generalConfig().isLevellingModuleEnabled(), bot);
	}

}
