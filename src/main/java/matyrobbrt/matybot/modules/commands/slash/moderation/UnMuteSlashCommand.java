package matyrobbrt.matybot.modules.commands.slash.moderation;

import java.awt.Color;
import java.time.Instant;
import java.util.List;

import com.jagrosh.jdautilities.command.SlashCommandEvent;

import matyrobbrt.matybot.MatyBot;
import matyrobbrt.matybot.api.annotation.RegisterSlashCommand;
import matyrobbrt.matybot.api.command.slash.GuildSpecificSlashCommand;
import matyrobbrt.matybot.modules.logging.LoggingModule;
import matyrobbrt.matybot.util.BotUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class UnMuteSlashCommand extends GuildSpecificSlashCommand {

	@RegisterSlashCommand
	private static final UnMuteSlashCommand CMD = new UnMuteSlashCommand();

	public UnMuteSlashCommand() {
		super("");
		name = "unmute";
		help = "Unmutes a member";
		enabledRolesGetter = guildId -> MatyBot.getConfigForGuild(guildId).moderatorRoles.stream().map(String::valueOf)
				.toArray(String[]::new);
		options = List.of(new OptionData(OptionType.USER, "user", "User to unmute", true), new OptionData(
				OptionType.BOOLEAN, "use_timeouts", "If true, the user's timeout will be removed. Defaults to true"));
	}

	public static MessageEmbed unmuteMember(final Guild guild, final Member unmuter, final Member member,
			final boolean isTimeout) {
		final var loggingChannel = LoggingModule.getLoggingChannel(guild);
		final var mutedRole = guild.getRoleById(MatyBot.getConfigForGuild(guild.getIdLong()).mutedRole);

		if (isTimeout) {
			guild.removeTimeout(member).reason("Timeout removed by " + unmuter.getUser().getAsTag()).queue();
		} else {
			guild.removeRoleFromMember(member, mutedRole).queue();
		}

		final var muteEmbed = new EmbedBuilder().setColor(Color.DARK_GRAY)
				.setTitle(member.getEffectiveName() + " has been unmuted!").setTimestamp(Instant.now())
				.setFooter("Moderator ID: " + unmuter.getIdLong(), unmuter.getEffectiveAvatarUrl());

		member.getUser().openPrivateChannel().queue(dm -> {
			final var embed = new EmbedBuilder().setColor(Color.RED).setTitle("You have been unmuted!")
					.setDescription("You have been unmuted in **" + guild.getName() + "**").setTimestamp(Instant.now())
					.setFooter("Moderator ID: " + unmuter.getIdLong(), unmuter.getEffectiveAvatarUrl());
			dm.sendMessageEmbeds(embed.build()).queue(m -> {},
					e -> muteEmbed.setDescription("*The user could not be DM'd.*"));
		});
		loggingChannel.sendMessageEmbeds(muteEmbed.build()).queue();
		return muteEmbed.build();
	}

	public static String removeLastChar(final String str) {
		return removeLastChars(str, 1);
	}

	public static String removeLastChars(final String str, final int chars) {
		return str.substring(0, str.length() - chars);
	}

	@Override
	public void execute(final SlashCommandEvent event) {
		final Member toMute = event.getOption("user").getAsMember();
		final boolean isTimeout = BotUtils.getOptionOr(event.getOption("use_timeouts"), OptionMapping::getAsBoolean,
				true);
		if (toMute.getIdLong() == event.getMember().getIdLong()) {
			event.deferReply(true).setContent("You cannot unmute yourself!").mentionRepliedUser(false).queue();
			return;
		}

		final var botUser = event.getGuild().getMember(event.getJDA().getSelfUser());
		if (!botUser.canInteract(toMute)) {
			event.deferReply(true).setContent("I cannot unmute this member!").mentionRepliedUser(false).queue();
			return;
		}

		if (!event.getMember().canInteract(toMute)) {
			event.deferReply(true).setContent("You do not have permission to unmute this user!")
					.mentionRepliedUser(false).queue();
			return;
		}

		final boolean isMuted = isTimeout ? toMute.isTimedOut()
				: toMute.getRoles().contains(event.getGuild()
						.getRoleById(MatyBot.getConfigForGuild(event.getGuild()).mutedRole));
		if (!isMuted) {
			event.deferReply(true).setContent("This user is not muted!").mentionRepliedUser(false).queue();
			return;
		}

		event.deferReply(false).addEmbeds(unmuteMember(event.getGuild(), event.getMember(), toMute, isTimeout))
				.mentionRepliedUser(false).queue();
	}

}
