package matyrobbrt.matybot.modules.commands.slash.moderation;

import java.awt.Color;
import java.time.Instant;
import java.util.List;

import com.jagrosh.jdautilities.command.SlashCommand;

import matyrobbrt.matybot.MatyBot;
import matyrobbrt.matybot.annotation.RegisterSlashCommand;
import matyrobbrt.matybot.modules.logging.LoggingModule;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class UnMuteSlashCommand extends SlashCommand {

	@RegisterSlashCommand
	private static final UnMuteSlashCommand CMD = new UnMuteSlashCommand();

	public UnMuteSlashCommand() {
		name = "unmute";
		defaultEnabled = false;
		enabledRoles = MatyBot.config().moderatorRoles.stream().map(String::valueOf).toArray(String[]::new);
		help = "Unmutes a member";
		options = List.of(new OptionData(OptionType.USER, "user", "User to unmute", true));
	}

	public static MessageEmbed unmuteMember(final Guild guild, final Member unmuter, final Member member) {
		final var loggingChannel = LoggingModule.getLoggingChannel(guild);
		final var mutedRole = guild.getRoleById(MatyBot.config().mutedRole);

		guild.removeRoleFromMember(member, mutedRole).queue();

		final var muteEmbed = new EmbedBuilder().setColor(Color.DARK_GRAY)
				.setTitle(member.getEffectiveName() + " has been unmuted!").setTimestamp(Instant.now())
				.setFooter("Moderator ID: " + unmuter.getIdLong(), unmuter.getEffectiveAvatarUrl());
		loggingChannel.sendMessageEmbeds(muteEmbed.build()).queue();

		member.getUser().openPrivateChannel().queue(dm -> {
			final var embed = new EmbedBuilder().setColor(Color.RED).setTitle("You have been unmuted!")
					.setDescription("You have been muted in **" + guild.getName() + "**").setTimestamp(Instant.now())
					.setFooter("Moderator ID: " + unmuter.getIdLong(), unmuter.getEffectiveAvatarUrl());
			dm.sendMessageEmbeds(embed.build()).queue();
		});
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
		if (toMute.getIdLong() == event.getMember().getIdLong()) {
			event.deferReply(true).setContent("You cannot unmute yourself!").mentionRepliedUser(false).queue();
			return;
		}

		if (!event.getMember().canInteract(toMute)) {
			event.deferReply(true).setContent("You do not have permission to unmute this user!")
					.mentionRepliedUser(false).queue();
			return;
		}

		if (!toMute.getRoles().contains(event.getGuild().getRoleById(MatyBot.config().mutedRole))) {
			event.deferReply(true).setContent("This user is not muted!").mentionRepliedUser(false).queue();
			return;
		}

		event.deferReply(false).addEmbeds(unmuteMember(event.getGuild(), event.getMember(), toMute))
				.mentionRepliedUser(false).queue();
	}

}
