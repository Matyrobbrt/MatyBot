package io.github.matyrobbrt.matybot.modules.logging;

import java.awt.Color;
import java.util.function.Consumer;

import io.github.matyrobbrt.jdautils.event.EventListenerWrapper;
import io.github.matyrobbrt.matybot.MatyBot;
import io.github.matyrobbrt.matybot.modules.logging.events.JoinLeaveEvents;
import io.github.matyrobbrt.matybot.modules.logging.events.RoleEvents;
import io.github.matyrobbrt.matybot.modules.logging.events.ScamDetector;
import io.github.matyrobbrt.matybot.modules.logging.events.UserEvents;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.hooks.EventListener;

public class LoggingModule extends io.github.matyrobbrt.jdautils.modules.Module {

	public LoggingModule(final JDA bot) {
		super(MatyBot.generalConfig()::isLoggingModuleEnabled, bot);
	}

	@Override
	public void register() {
		super.register();
		bot.addEventListener(wrap(JoinLeaveEvents.INSTANCE), wrap(new RoleEvents()), wrap(new UserEvents()),
				wrap(new ScamDetector()));
	}

	private static EventListenerWrapper wrap(EventListener listener) {
		return new EventListenerWrapper(listener);
	}

	public static TextChannel getLoggingChannel(final Guild guild) {
		return guild.getTextChannelById(MatyBot.getConfigForGuild(guild.getIdLong()).loggingChannel);
	}

	public static void inLoggingChannel(final Guild guild, Consumer<TextChannel> consumer) {
		MatyBot.getInstance().getChannelIfPresent(MatyBot.getConfigForGuild(guild).loggingChannel, consumer);
	}

	public static void inLoggingChannel(final long guildId, Consumer<TextChannel> consumer) {
		MatyBot.getInstance().getChannelIfPresent(MatyBot.getConfigForGuild(guildId).loggingChannel, consumer);
	}

	public static void logException(final long guildId, final String message, final Throwable exception) {
		inLoggingChannel(guildId, channel -> {
			channel.sendMessageEmbeds(new EmbedBuilder().setTitle("Exception").setColor(Color.RED)
					.setDescription("There was an exception while %s".formatted(message))
					.addField("Exception", exception.getLocalizedMessage(), false).build()).queue();
		});
	}

}
