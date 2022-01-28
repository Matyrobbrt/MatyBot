package io.github.matyrobbrt.matybot.util.console;

import java.util.Collections;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Layout;
import io.github.matyrobbrt.matybot.MatyBot;
import net.dv8tion.jda.api.MessageBuilder;

public final class ConsoleChannelAppender extends AppenderBase<ILoggingEvent> {

	private boolean allowMentions;

	private Layout<ILoggingEvent> layout;

	public void setAllowMentions(final boolean allowMentionsIn) {
		this.allowMentions = allowMentionsIn;
	}

	public void setLayout(final Layout<ILoggingEvent> layoutIn) {
		this.layout = layoutIn;
	}

	@Override
	protected void append(final ILoggingEvent event) {
		if (MatyBot.getInstance() != null) {
			MatyBot.getInstance().getChannelIfPresent(MatyBot.generalConfig().consoleChannelId, channel -> {
				final var builder = new MessageBuilder();
				builder.append(layout != null ? layout.doLayout(event) : event.getFormattedMessage());
				if (!allowMentions) {
					builder.setAllowedMentions(Collections.emptyList());
				}
				channel.sendMessage(builder.build()).queue();
			});
		}
	}
}
