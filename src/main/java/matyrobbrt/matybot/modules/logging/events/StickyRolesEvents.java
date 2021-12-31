package matyrobbrt.matybot.modules.logging.events;

import static matyrobbrt.matybot.MatyBot.LOGGER;

import java.awt.Color;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import matyrobbrt.matybot.MatyBot;
import matyrobbrt.matybot.event.api.AnnotationEventListener;
import matyrobbrt.matybot.modules.logging.LoggingModule;
import matyrobbrt.matybot.util.BotUtils;
import matyrobbrt.matybot.util.database.dao.StickyRoles;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import net.dv8tion.jda.api.utils.TimeFormat;

public class StickyRolesEvents extends AnnotationEventListener {

	public static final StickyRolesEvents INSTANCE = new StickyRolesEvents();

	@Override
	@SubscribeEvent
	public void onEventHandleAnnotation(GenericEvent event) {
		super.onEventHandleAnnotation(event);
	}

	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		final var loggingChannel = LoggingModule.getLoggingChannel(event.getGuild());

		Member member = event.getMember();
		final var roles = BotUtils.getOldUserRoles(event.getGuild(), member.getIdLong());
		if (!roles.isEmpty()) {
			new Thread(() -> roles.forEach(role -> event.getGuild().addRoleToMember(member, role).queue()),
					"Adding roles for " + member.getUser().getAsTag()).start();
		}

		final var user = event.getUser();
		final var embed = new EmbedBuilder();
		final var dateJoinedDiscord = member.getTimeCreated().toInstant();
		embed.setColor(Color.GREEN);
		embed.setTitle("User Joined");
		embed.setThumbnail(user.getEffectiveAvatarUrl());
		embed.addField("User:", user.getAsTag(), true);
		if (!roles.isEmpty()) {
			embed.addField("Roles:", roles.stream().map(IMentionable::getAsMention).collect(Collectors.joining()),
					true);
		}

		embed.addField("Joined Discord:", TimeFormat.RELATIVE.format(dateJoinedDiscord), true);
		embed.setFooter("User ID: " + user.getId());
		embed.setTimestamp(Instant.now());

		loggingChannel.sendMessageEmbeds(embed.build()).queue();
	}

	@Override
	public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
		final var loggingChannel = LoggingModule.getLoggingChannel(event.getGuild());

		Member member = event.getMember();
		List<Role> roles = null;
		if (member != null) {
			roles = member.getRoles();
			final List<Long> roleIds = roles.stream().map(ISnowflake::getIdLong).toList();
			BotUtils.clearOldUserRoles(member.getIdLong());
			MatyBot.database().useExtension(StickyRoles.class, data -> data.insert(member.getIdLong(), roleIds));
		}

		User user = event.getUser();
		final var embed = new EmbedBuilder();
		embed.setColor(java.awt.Color.RED);
		embed.setTitle("User Left");
		embed.setThumbnail(user.getEffectiveAvatarUrl());
		embed.addField("User:", user.getAsTag(), true);
		if (roles != null && !roles.isEmpty()) {
			embed.addField("Roles:", roles.stream().map(IMentionable::getAsMention).collect(Collectors.joining()),
					true);
			LOGGER.info(BotUtils.Markers.EVENTS, "User {} had the following roles before leaving: {}", user, roles);
		} else if (roles == null) {
			embed.addField("Roles:", "_Could not obtain user's roles._", true);
		}
		embed.setFooter("User ID: " + user.getId());
		embed.setTimestamp(Instant.now());

		loggingChannel.sendMessageEmbeds(embed.build()).queue();
	}

}
