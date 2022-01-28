package io.github.matyrobbrt.matybot.modules.rolepanel;

import static io.github.matyrobbrt.matybot.modules.rolepanel.RolePanelsModule.getPanelForChannelAndMessage;

import java.awt.Color;

import io.github.matyrobbrt.matybot.MatyBot;
import io.github.matyrobbrt.matybot.modules.logging.LoggingModule;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class RolePanelHandler extends ListenerAdapter {

	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent event) {
		Member member = event.retrieveMember().complete();
		if ((member == null) || member.getUser().isBot()) { return; }
		RolePanel panel = getPanelForChannelAndMessage(event.getChannel().getIdLong(), event.getMessageIdLong());
		if (panel == null) { return; }
		final var emote = event.getReactionEmote();
		Long roleId = panel.getRoleForEmoji(emote.isEmoji() ? emote.getAsCodepoints() : emote.getAsReactionCode());
		if (roleId == null) { return; }
		Role role = event.getGuild().getRoleById(roleId);
		if (role == null) { return; }

		if (!member.getRoles().contains(role)) {
			event.getGuild().addRoleToMember(member, role).queue(v -> {}, e -> {
				LoggingModule.inLoggingChannel(member.getGuild(), loggingChannel -> {
					loggingChannel.sendMessageEmbeds(new EmbedBuilder().setTitle("Reaction Role Exception")
							.setDescription("I could not give the reaction role %s to %s due to an exception."
									.formatted(role.getAsMention(), member.getAsMention()))
							.setColor(Color.RED).addField("Exception", e.getLocalizedMessage(), false).build()).queue();
				});
				MatyBot.LOGGER.error("I could not give the reaction role {} to {} in guild {} due to an exception.",
						role.getIdLong(), member.getUser().getAsTag(), member.getGuild().getName(), e);
			});
		}
	}

	@Override
	public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
		Member member = event.retrieveMember().complete();
		if ((member == null) || member.getUser().isBot()) { return; }
		RolePanel panel = getPanelForChannelAndMessage(event.getChannel().getIdLong(), event.getMessageIdLong());
		if (panel == null) { return; }
		final var emote = event.getReactionEmote();
		Long roleId = panel.getRoleForEmoji(emote.isEmoji() ? emote.getAsCodepoints() : emote.getAsReactionCode());
		if (roleId == null) { return; }
		Role role = event.getGuild().getRoleById(roleId);
		if (role == null) { return; }

		if (member.getRoles().contains(role)) {
			event.getGuild().removeRoleFromMember(member, role).queue(v -> {}, e -> {
				LoggingModule.inLoggingChannel(member.getGuild(), loggingChannel -> {
					loggingChannel.sendMessageEmbeds(new EmbedBuilder().setTitle("Reaction Role Exception")
							.setDescription("I could not remove the reaction role %s from %s due to an exception."
									.formatted(role.getAsMention(), member.getAsMention()))
							.setColor(Color.RED).addField("Exception", e.getLocalizedMessage(), false).build()).queue();
				});
				MatyBot.LOGGER.error("I could not remove the reaction role {} from {} in guild {} due to an exception.",
						role.getIdLong(), member.getUser().getAsTag(), member.getGuild().getName(), e);
			});
		}
	}

}
