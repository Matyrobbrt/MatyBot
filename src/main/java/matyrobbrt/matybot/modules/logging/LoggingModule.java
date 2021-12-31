package matyrobbrt.matybot.modules.logging;

import matyrobbrt.matybot.MatyBot;
import matyrobbrt.matybot.modules.logging.events.RoleEvents;
import matyrobbrt.matybot.modules.logging.events.StickyRolesEvents;
import matyrobbrt.matybot.modules.logging.events.UserEvents;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

public class LoggingModule {

	public static void setupLoggingModule() {
		if (MatyBot.config().isLoggingModuleEnabled()) {
			MatyBot.instance.getBot().addEventListener(StickyRolesEvents.INSTANCE, new RoleEvents(), new UserEvents());
			MatyBot.LOGGER.warn("Event logging module enabled and loaded.");
		} else {
			MatyBot.LOGGER
					.warn("Event logging module disabled via config, Discord event logging won't work right now!");
		}
	}

	public static TextChannel getLoggingChannel(final Guild guild) {
		return guild.getTextChannelById(MatyBot.config().loggingChannel);
	}

}
