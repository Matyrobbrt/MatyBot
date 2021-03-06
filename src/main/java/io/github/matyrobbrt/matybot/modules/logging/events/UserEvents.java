package io.github.matyrobbrt.matybot.modules.logging.events;

import java.awt.Color;
import java.time.Instant;

import io.github.matyrobbrt.matybot.MatyBot;
import io.github.matyrobbrt.matybot.modules.logging.LoggingModule;
import io.github.matyrobbrt.matybot.util.console.Markers;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class UserEvents extends ListenerAdapter {

	@Override
	public void onGuildMemberUpdateNickname(GuildMemberUpdateNicknameEvent event) {
		final var loggingChannel = LoggingModule.getLoggingChannel(event.getGuild());

		if (loggingChannel == null) { return; }

		event.getGuild().retrieveAuditLogs().type(ActionType.MEMBER_UPDATE).limit(1).cache(false)
				.map(list -> list.get(0)).flatMap(entry -> {
					final var embed = new EmbedBuilder();
					final var target = event.getUser();

					embed.setColor(Color.YELLOW);
					embed.setTitle("Nickname Changed");
					embed.setThumbnail(target.getEffectiveAvatarUrl());
					embed.addField("User:", target.getAsMention() + " (" + target.getId() + ")", true);
					embed.setTimestamp(Instant.now());
					if (entry.getTargetIdLong() != target.getIdLong()) {
						MatyBot.LOGGER.warn(Markers.EVENTS,
								"Inconsistency between target of retrieved audit log "
										+ "entry and actual nickname event target: retrieved is {}, but target is {}",
								entry.getUser(), target);
					} else if (entry.getUser() != null) {
						final var editor = entry.getUser();
						embed.addField("Nickname Editor:", editor.getAsMention() + " (" + editor.getId() + ")", true);
						embed.addBlankField(true);
					}
					final String oldNick = event.getOldNickname() == null ? target.getName() : event.getOldNickname();
					final String newNick = event.getNewNickname() == null ? target.getName() : event.getNewNickname();
					embed.addField("Old Nickname:", oldNick, true);
					embed.addField("New Nickname:", newNick, true);

					return loggingChannel.sendMessageEmbeds(embed.build());
				}).queue();

	}

}
