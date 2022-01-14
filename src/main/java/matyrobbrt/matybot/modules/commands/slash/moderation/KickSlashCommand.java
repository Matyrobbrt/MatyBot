package matyrobbrt.matybot.modules.commands.slash.moderation;

import java.awt.Color;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import com.jagrosh.jdautilities.command.SlashCommandEvent;

import matyrobbrt.matybot.MatyBot;
import matyrobbrt.matybot.api.annotation.RegisterSlashCommand;
import matyrobbrt.matybot.api.command.slash.GuildSpecificSlashCommand;
import matyrobbrt.matybot.modules.logging.LoggingModule;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class KickSlashCommand extends GuildSpecificSlashCommand {

	@RegisterSlashCommand
	private static final KickSlashCommand CMD = new KickSlashCommand();

	public KickSlashCommand() {
		super("");
		name = "kick";
		enabledRolesGetter = guildId -> MatyBot.getConfigForGuild(guildId).moderatorRoles.stream().map(String::valueOf)
				.toArray(String[]::new);
		help = "Kicks a member";
		options = List.of(new OptionData(OptionType.USER, "user", "User to kick", true),
				new OptionData(OptionType.STRING, "reason", "The reason to kick for").setRequired(true));
	}

	public static MessageEmbed kickMember(final Guild guild, final Member kicker, final Member member,
			final String reason) {
		final var loggingChannel = LoggingModule.getLoggingChannel(guild);

		final var user = member.getUser();

		final var banEmbed = new EmbedBuilder().setColor(Color.DARK_GRAY).setTitle(user.getName() + " has been kicked!")
				.setDescription("**Reason**: " + reason + "\n**Kicked By**: " + kicker.getAsMention())
				.setTimestamp(Instant.now())
				.setFooter("Moderator ID: " + kicker.getIdLong(), kicker.getEffectiveAvatarUrl());
		loggingChannel.sendMessageEmbeds(banEmbed.build()).queue();

		user.openPrivateChannel().queue(dm -> {
			final var embed = new EmbedBuilder().setColor(Color.RED).setTitle("You have been kicked!")
					.setDescription("You have been kicked in **" + guild.getName() + "** by " + kicker.getAsMention())
					.addField("Reason", reason, false).setTimestamp(Instant.now())
					.setFooter("Moderator ID: " + kicker.getIdLong(), kicker.getEffectiveAvatarUrl());
			dm.sendMessageEmbeds(embed.build()).queue();
		});
		guild.kick(member, reason).queue();
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
		final User toKick = event.getOption("user").getAsUser();
		final Member toKickMember = event.getGuild().getMember(toKick);
		if (toKick.getIdLong() == event.getMember().getIdLong()) {
			event.deferReply(true).setContent("You cannot kick yourself!").mentionRepliedUser(false).queue();
			return;
		}

		if (toKickMember != null && !event.getMember().canInteract(toKickMember)) {
			event.deferReply(true).setContent("You do not have permission to kick this user!").mentionRepliedUser(false)
					.queue();
			return;
		}

		final var botUser = event.getGuild().getMember(event.getJDA().getSelfUser());
		if (!botUser.canInteract(toKickMember)) {
			event.deferReply(true).setContent("I cannot kick this member!").mentionRepliedUser(false).queue();
			return;
		}

		final OptionMapping reasonOption = event.getOption("reason");
		final String reason = reasonOption == null ? "Unspecified" : reasonOption.getAsString();

		event.deferReply(false).addEmbeds(kickMember(event.getGuild(), event.getMember(), toKickMember, reason))
				.queue();
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
