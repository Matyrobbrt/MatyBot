package io.github.matyrobbrt.matybot.modules.logging;

import io.github.matyrobbrt.jdautils.event.EventListenerWrapper;
import io.github.matyrobbrt.matybot.MatyBot;
import io.github.matyrobbrt.matybot.modules.logging.events.JoinLeaveEvents;
import io.github.matyrobbrt.matybot.modules.logging.events.RoleEvents;
import io.github.matyrobbrt.matybot.modules.logging.events.ScamDetector;
import io.github.matyrobbrt.matybot.modules.logging.events.UserEvents;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

public class LoggingModule extends io.github.matyrobbrt.jdautils.modules.Module {

	public LoggingModule(final JDA bot) {
		super(MatyBot.generalConfig()::isLoggingModuleEnabled, bot);
	}

	@Override
	public void register() {
		super.register();
		bot.addEventListener(JoinLeaveEvents.INSTANCE, new RoleEvents(), new UserEvents(),
				new EventListenerWrapper(new ScamDetector()));
	}

	public static TextChannel getLoggingChannel(final Guild guild) {
		return guild.getTextChannelById(MatyBot.getConfigForGuild(guild.getIdLong()).loggingChannel);
	}

}
