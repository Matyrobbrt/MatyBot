package io.github.matyrobbrt.matybot.modules.commands.slash.moderation;

import java.awt.Color;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import com.jagrosh.jdautilities.command.SlashCommandEvent;

import io.github.matyrobbrt.matybot.MatyBot;
import io.github.matyrobbrt.matybot.api.annotation.RegisterSlashCommand;
import io.github.matyrobbrt.matybot.api.command.slash.GuildSpecificSlashCommand;
import io.github.matyrobbrt.matybot.modules.logging.LoggingModule;
import io.github.matyrobbrt.matybot.util.BotUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class BanSlashCommand extends GuildSpecificSlashCommand {

	@RegisterSlashCommand
	private static final BanSlashCommand CMD = new BanSlashCommand();

	public BanSlashCommand() {
		super("");
		name = "ban";
		enabledRolesGetter = guildId -> MatyBot.getConfigForGuild(guildId).moderatorRoles.stream().map(String::valueOf)
				.toArray(String[]::new);
		botPermissions = new Permission[] {
				Permission.BAN_MEMBERS
		};
		help = "Banns a member";
		OptionData time = new OptionData(OptionType.INTEGER, "time",
				"The amount of time to ban for. Indefinitely if not specified.").setRequired(false);
		OptionData unit = new OptionData(OptionType.STRING, "unit", "The unit of the time specifier.")
				.setRequired(false).addChoice("seconds", "seconds").addChoice("minutes", "minutes")
				.addChoice("hours", "hours").addChoice("days", "days");
		OptionData deleteCount = new OptionData(OptionType.INTEGER, "delete_count",
				"The amount of days for deleting message history. Leave empty in order to not delete any message.")
						.setRequired(false);
		options = List.of(new OptionData(OptionType.USER, "user", "User to ban", true),
				new OptionData(OptionType.STRING, "reason", "The reason to ban for").setRequired(true), deleteCount,
				time, unit);
	}

	public static MessageEmbed banMember(final Guild guild, final Member banner, final long memberID,
			final String reason, final int deleteDaysCount, final long time, final TimeUnit timeUnit) {
		final var loggingChannel = LoggingModule.getLoggingChannel(guild);

		final User member = MatyBot.getInstance().getJDA().retrieveUserById(memberID).complete();

		final var banEmbed = new EmbedBuilder().setColor(Color.DARK_GRAY)
				.setTitle(member.getName() + " has been banned!"
						+ (time != -1 ? " for " + time + " " + timeUnit.toString().toLowerCase() : "indefinitely"))
				.setDescription("**Reason**: " + reason + "\n**Banned By**: " + banner.getAsMention())
				.setTimestamp(Instant.now())
				.setFooter("Moderator ID: " + banner.getIdLong(), banner.getEffectiveAvatarUrl());
		loggingChannel.sendMessageEmbeds(banEmbed.build()).queue();

		member.openPrivateChannel().queue(dm -> {
			final var embed = new EmbedBuilder().setColor(Color.RED).setTitle("You have been banned!")
					.setDescription("You have been banned in **" + guild.getName() + "** by " + banner.getAsMention()
							+ (time != -1 ? " for " + time + " " + timeUnit.toString().toLowerCase() : "indefinitely"))
					.addField("Reason", reason, false).setTimestamp(Instant.now())
					.setFooter("Moderator ID: " + banner.getIdLong(), banner.getEffectiveAvatarUrl());
			dm.sendMessageEmbeds(embed.build()).queue();
		});

		guild.ban(member, deleteDaysCount, reason).queue();

		if (time != -1) {
			guild.unban(member).reason("Automatically unbanned").queueAfter(time, timeUnit, v -> {
				final var unbanEmbed = new EmbedBuilder().setColor(Color.MAGENTA)
						.setTitle(member.getName() + " has been automatically unbanned.").setTimestamp(Instant.now());
				loggingChannel.sendMessageEmbeds(unbanEmbed.build()).queue();
			});
		}
		return banEmbed.build();
	}

	public static String removeLastChar(final String str) {
		return removeLastChars(str, 1);
	}

	public static String removeLastChars(final String str, final int chars) {
		return str.substring(0, str.length() - chars);
	}

	public static String onlyInts(final String str) {
		return str.replaceAll("[^0-9]", "");
	}

	@Override
	public void execute(final SlashCommandEvent event) {
		final User toBan = event.getOption("user").getAsUser();
		final Member toBanMemeber = event.getGuild().getMember(toBan);
		if (toBan.getIdLong() == event.getMember().getIdLong()) {
			event.deferReply(true).setContent("You cannot ban yourself!").mentionRepliedUser(false).queue();
			return;
		}

		if (toBanMemeber != null && !event.getMember().canInteract(toBanMemeber)) {
			event.deferReply(true).setContent("You do not have permission to ban this user!").mentionRepliedUser(false)
					.queue();
			return;
		}

		final var botUser = event.getGuild().getMember(event.getJDA().getSelfUser());
		if (!botUser.canInteract(toBanMemeber)) {
			event.deferReply(true).setContent("I cannot ban this member!").mentionRepliedUser(false).queue();
			return;
		}

		long time = event.getOption("time") == null ? -1 : event.getOption("time").getAsLong();
		TimeUnit unit = event.getOption("unit") == null ? TimeUnit.DAYS
				: parseTimeUnit(event.getOption("unit").getAsString());

		final OptionMapping reasonOption = event.getOption("reason");
		final String reason = reasonOption == null ? "Unspecified" : reasonOption.getAsString();

		final int deleteMsgDays = BotUtils.getIntArgumentOr(event, reason, 0);

		event.deferReply(false).addEmbeds(
				banMember(event.getGuild(), event.getMember(), toBan.getIdLong(), reason, deleteMsgDays, time, unit))
				.mentionRepliedUser(false).queue();
	}

	/**
	 * Parse time unit.
	 *
	 * @param  timeUnitIn the time unit in
	 * @return            The {@code TimeUnit} formatted from a {@code String}.
	 */
	public static TimeUnit parseTimeUnit(final String timeUnitIn) {
		TimeUnit unit;
		try {
			unit = TimeUnit.valueOf(timeUnitIn.toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException e) {
			unit = TimeUnit.MINUTES;
		}
		return unit;
	}

}
