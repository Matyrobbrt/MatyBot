package io.github.matyrobbrt.matybot.modules.logging.events;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import io.github.matyrobbrt.matybot.modules.logging.LoggingModule;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.role.RoleCreateEvent;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

public class RoleEvents extends ListenerAdapter {

	@SubscribeEvent
	@Override
	public void onGenericEvent(GenericEvent event) {
		super.onGenericEvent(event);
	}

	@Override
	public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {
		final var loggingChannel = LoggingModule.getLoggingChannel(event.getGuild());
		final var user = event.getUser();
		final var member = event.getMember();
		final var addedRoles = event.getRoles();
		final var userRoles = member.getRoles();

		//@formatter:off
		final var embed = new EmbedBuilder()
				.setColor(event.getRoles().get(0).getColor())
				.setTitle("Roles added to user")
				.setThumbnail(user.getEffectiveAvatarUrl())
				.addField("User: ", user.getAsMention(), true)
				.addField(new Field("Before:",
						userRoles.stream().filter(r -> !addedRoles.contains(r)).map(IMentionable::getAsMention)
								.collect(Collectors.joining()),
						false))
				.addField(new Field("Roles added:",
						addedRoles.stream().map(IMentionable::getAsMention).collect(Collectors.joining()), false))
				.addField(new Field("After:",
						userRoles.stream().map(IMentionable::getAsMention).collect(Collectors.joining()), false))
				.setFooter("User ID: " + user.getId()).setTimestamp(Instant.now());
		//@formatter:on

		loggingChannel.sendMessageEmbeds(embed.build()).queue();
	}

	@Override
	public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent event) {
		final var loggingChannel = LoggingModule.getLoggingChannel(event.getGuild());
		final var user = event.getUser();
		final var member = event.getMember();
		final var removedRoles = event.getRoles();
		final var userRoles = member.getRoles();

		final Set<Role> rolesBefore = new HashSet<>(userRoles);
		removedRoles.forEach(rolesBefore::add);

		//@formatter:off
		final var embed = new EmbedBuilder()
				.setColor(event.getRoles().get(0).getColor())
				.setTitle("Roles removed from user")
				.setThumbnail(user.getEffectiveAvatarUrl())
				.addField("User: ", user.getAsMention(), true)
				.addField(new Field("Before:",
						rolesBefore.stream().map(IMentionable::getAsMention)
								.collect(Collectors.joining()),
						false))
				.addField(new Field("Roles removed:",
						removedRoles.stream().map(IMentionable::getAsMention).collect(Collectors.joining()), false))
				.addField(new Field("After:",
						userRoles.stream().filter(r -> !removedRoles.contains(r)).map(IMentionable::getAsMention).collect(Collectors.joining()), false))
				.setFooter("User ID: " + user.getId()).setTimestamp(Instant.now());
		//@formatter:on

		loggingChannel.sendMessageEmbeds(embed.build()).queue();
	}

	@Override
	public void onRoleCreate(RoleCreateEvent event) {
		final var loggingChannel = LoggingModule.getLoggingChannel(event.getGuild());
		final var role = event.getRole();

		//@formatter:off
		final var embed = new EmbedBuilder()
				.setColor(role.getColor())
				.setTitle("Role Created")
				.addField("Role: ", role.getAsMention(), true)
				.setFooter("Role ID: " + role.getId()).setTimestamp(Instant.now());
		//@formatter:on

		loggingChannel.sendMessageEmbeds(embed.build()).queue();
	}

	@Override
	public void onRoleDelete(RoleDeleteEvent event) {
		final var loggingChannel = LoggingModule.getLoggingChannel(event.getGuild());
		final var role = event.getRole();

		//@formatter:off
		final var embed = new EmbedBuilder()
				.setColor(role.getColor())
				.setTitle("Role Deleted")
				.addField("Role ID: ", role.getId(), true)
				.setTimestamp(Instant.now());
		//@formatter:on

		loggingChannel.sendMessageEmbeds(embed.build()).queue();
	}
}
