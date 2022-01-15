package matyrobbrt.matybot.modules.logging;

import matyrobbrt.matybot.MatyBot;
import matyrobbrt.matybot.api.event.EventListenerWrapper;
import matyrobbrt.matybot.modules.logging.events.RoleEvents;
import matyrobbrt.matybot.modules.logging.events.ScamDetector;
import matyrobbrt.matybot.modules.logging.events.JoinLeaveEvents;
import matyrobbrt.matybot.modules.logging.events.UserEvents;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

public class LoggingModule extends matyrobbrt.matybot.api.modules.Module {

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
