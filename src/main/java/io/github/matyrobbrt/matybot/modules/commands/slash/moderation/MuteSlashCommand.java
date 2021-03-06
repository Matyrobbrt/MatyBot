package io.github.matyrobbrt.matybot.modules.commands.slash.moderation;

import java.awt.Color;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

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
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class MuteSlashCommand extends GuildSpecificSlashCommand {

	@RegisterSlashCommand
	private static final MuteSlashCommand CMD = new MuteSlashCommand();

	public MuteSlashCommand() {
		super("");
		name = "mute";
		enabledRolesGetter = guildId -> MatyBot.getConfigForGuild(guildId).moderatorRoles.stream().map(String::valueOf)
				.toArray(String[]::new);
		botPermissions = new Permission[] {
				Permission.MODERATE_MEMBERS
		};
		help = "Mutes a member";
		OptionData time = new OptionData(OptionType.INTEGER, "time",
				"The amount of time to mute for. Indefinitely if not specified.").setRequired(false);
		OptionData unit = new OptionData(OptionType.STRING, "unit", "The unit of the time specifier.")
				.setRequired(false).addChoice("seconds", "seconds").addChoice("minutes", "minutes")
				.addChoice("hours", "hours").addChoice("days", "days");
		options = List.of(new OptionData(OptionType.USER, "user", "User to mute", true),
				new OptionData(OptionType.STRING, "reason", "The reason to mute for").setRequired(true), time, unit,
				new OptionData(OptionType.BOOLEAN, "use_timeouts",
						"If true, the user will be timed out. Defaults to true"));
	}

	public static MessageEmbed muteMember(final Guild guild, final Member muter, final Member member,
			final String reason, final long time, final TimeUnit timeUnit, final boolean isTimeout) {
		final var loggingChannel = LoggingModule.getLoggingChannel(guild);
		final var mutedRole = guild.getRoleById(MatyBot.getConfigForGuild(guild.getIdLong()).mutedRole);

		AtomicReference<String> muteTimeStr = new AtomicReference<>(
				time != -1 ? " for " + time + " " + timeUnit.toString().toLowerCase() : " indefinitely");

		if (isTimeout) {
			final long actualTime = time == -1 ? 28 : time;
			if (time == -1) {
				muteTimeStr.set(" for 28 days");
			}
			guild.timeoutFor(member, actualTime, timeUnit).reason(reason).queue();
		} else {
			guild.addRoleToMember(member, mutedRole).reason("Muted with the reason: " + reason).queue();
		}

		final var muteEmbed = new EmbedBuilder().setColor(Color.DARK_GRAY)
				.setTitle(member.getEffectiveName() + " has been muted" + muteTimeStr.get())
				.setDescription("**Reason**: " + reason + "\n**Muted By**: " + muter.getAsMention())
				.setTimestamp(Instant.now()).setFooter("Muter ID: " + muter.getIdLong(), muter.getEffectiveAvatarUrl());

		member.getUser().openPrivateChannel().queue(dm -> {
			final var embed = new EmbedBuilder().setColor(Color.RED).setTitle("You have been muted!")
					.setDescription("You have been muted in **" + guild.getName() + "** by " + muter.getAsMention()
							+ muteTimeStr.get())
					.addField("Reason", reason, false).setTimestamp(Instant.now())
					.setFooter("Muter ID: " + muter.getIdLong(), muter.getEffectiveAvatarUrl());
			dm.sendMessageEmbeds(embed.build()).queue(m -> {},
					e -> muteEmbed.appendDescription(" *The user could not be DM'd.*"));
		});

		loggingChannel.sendMessageEmbeds(muteEmbed.build()).queue();

		if (time != -1) {
			if (!isTimeout) {
				guild.removeRoleFromMember(member, mutedRole).reason("Automatically unmuted!").queueAfter(time,
						timeUnit, v -> {
							final var unmuteEmbed = new EmbedBuilder().setColor(member.getColorRaw())
									.setTitle(member.getEffectiveName() + " has been automatically unmuted.")
									.setTimestamp(Instant.now());
							loggingChannel.sendMessageEmbeds(unmuteEmbed.build()).queue();
						});
			}
		}
		return muteEmbed.build();
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
		final Member toMute = event.getOption("user").getAsMember();
		final boolean isTimeout = BotUtils.getOptionOr(event.getOption("use_timeouts"), OptionMapping::getAsBoolean,
				true);
		if (toMute.getIdLong() == event.getMember().getIdLong()) {
			event.deferReply(true).setContent("You cannot mute yourself!").mentionRepliedUser(false).queue();
			return;
		}

		if (!event.getMember().canInteract(toMute)) {
			event.deferReply(true).setContent("You do not have permission to mute this user!").mentionRepliedUser(false)
					.queue();
			return;
		}

		final var botUser = event.getGuild().getMember(event.getJDA().getSelfUser());
		if (!botUser.canInteract(toMute)) {
			event.deferReply(true).setContent("I cannot unmute this member!").mentionRepliedUser(false).queue();
			return;
		}

		final boolean isMuted = isTimeout ? toMute.isTimedOut()
				: toMute.getRoles()
						.contains(event.getGuild().getRoleById(MatyBot.getConfigForGuild(event.getGuild()).mutedRole));

		if (isMuted) {
			event.deferReply(true).setContent("This user is already muted!").mentionRepliedUser(false).queue();
			return;
		}

		long time = event.getOption("time") == null ? -1 : event.getOption("time").getAsLong();
		TimeUnit unit = event.getOption("unit") == null ? TimeUnit.DAYS
				: parseTimeUnit(event.getOption("unit").getAsString());

		final String reason = event.getOption("reason").getAsString();

		event.deferReply(false)
				.addEmbeds(muteMember(event.getGuild(), event.getMember(), toMute, reason, time, unit, isTimeout))
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
