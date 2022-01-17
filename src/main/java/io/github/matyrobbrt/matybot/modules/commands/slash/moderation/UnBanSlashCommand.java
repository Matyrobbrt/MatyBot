package io.github.matyrobbrt.matybot.modules.commands.slash.moderation;

import java.awt.Color;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.jagrosh.jdautilities.command.SlashCommandEvent;

import io.github.matyrobbrt.matybot.MatyBot;
import io.github.matyrobbrt.matybot.api.annotation.RegisterSlashCommand;
import io.github.matyrobbrt.matybot.api.command.slash.GuildSpecificSlashCommand;
import io.github.matyrobbrt.matybot.modules.logging.LoggingModule;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class UnBanSlashCommand extends GuildSpecificSlashCommand {

	@RegisterSlashCommand
	private static final UnBanSlashCommand CMD = new UnBanSlashCommand();

	public UnBanSlashCommand() {
		super("");
		name = "unban";
		help = "Unbans a member";
		enabledRolesGetter = guildId -> MatyBot.getConfigForGuild(guildId).moderatorRoles.stream().map(String::valueOf)
				.toArray(String[]::new);
		options = List.of(new OptionData(OptionType.USER, "user", "User to unban", true));
	}

	public static MessageEmbed unmuteMember(final Guild guild, final Member unmuter, final long memberId)
			throws Throwable {
		final var member = MatyBot.getInstance().getJDA().retrieveUserById(memberId).complete();
		final var loggingChannel = LoggingModule.getLoggingChannel(guild);

		final var error = new AtomicReference<Throwable>(null);
		guild.unban(member).onErrorMap(throwable -> {
			error.set(throwable);
			return null;
		}).complete();

		if (error.get() != null) { throw error.get(); }

		final var banEmbed = new EmbedBuilder().setColor(Color.DARK_GRAY)
				.setTitle(member.getName() + " has been unbanned!").setTimestamp(Instant.now())
				.setFooter("Moderator ID: " + unmuter.getIdLong(), unmuter.getEffectiveAvatarUrl());
		loggingChannel.sendMessageEmbeds(banEmbed.build()).queue();
		return banEmbed.build();
	}

	public static String removeLastChar(final String str) {
		return removeLastChars(str, 1);
	}

	public static String removeLastChars(final String str, final int chars) {
		return str.substring(0, str.length() - chars);
	}

	@Override
	public void execute(final SlashCommandEvent event) {
		final User toUnban = event.getOption("user").getAsUser();
		try {
			event.deferReply(false).addEmbeds(unmuteMember(event.getGuild(), event.getMember(), toUnban.getIdLong()))
					.mentionRepliedUser(false).queue();
		} catch (Throwable e) {
			event.deferReply(false).setEphemeral(true)
					.setContent("Error while un-banning user: **" + e.getLocalizedMessage() + "**")
					.mentionRepliedUser(true).queue();
		}
	}

}
