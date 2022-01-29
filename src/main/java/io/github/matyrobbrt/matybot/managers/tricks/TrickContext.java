package io.github.matyrobbrt.matybot.managers.tricks;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@Getter
@RequiredArgsConstructor
public class TrickContext {

	private final MessageChannel channel;
	private final Member member;
	private final Guild guild;
	private final String[] args;

	@Getter
	public static final class Slash extends TrickContext {

		private final SlashCommandInteractionEvent event;

		public Slash(SlashCommandInteractionEvent event, String[] args) {
			super(event.getChannel(), event.getMember(), event.getGuild(), args);
			this.event = event;
		}

	}

	@Getter
	public static final class Prefix extends TrickContext {

		private final Message message;

		public Prefix(Message message, String[] args) {
			super(message.getChannel(), message.getMember(), message.getGuild(), args);
			this.message = message;
		}

	}
}
