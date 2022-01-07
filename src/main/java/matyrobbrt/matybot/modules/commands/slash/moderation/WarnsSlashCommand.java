package matyrobbrt.matybot.modules.commands.slash.moderation;

import java.awt.Color;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;

import matyrobbrt.matybot.MatyBot;
import matyrobbrt.matybot.api.annotation.RegisterSlashCommand;
import matyrobbrt.matybot.modules.logging.LoggingModule;
import matyrobbrt.matybot.util.database.dao.Warnings;
import matyrobbrt.matybot.util.database.dao.Warnings.WarningDocument;
import matyrobbrt.matybot.util.database.dao.Warnings.WarningsDocument;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.TimeFormat;
import net.dv8tion.jda.api.utils.Timestamp;

public class WarnsSlashCommand extends SlashCommand {

	@RegisterSlashCommand
	private static final WarnsSlashCommand COMMAND = new WarnsSlashCommand();

	public WarnsSlashCommand() {
		this.name = "warning";
		defaultEnabled = false;
		enabledRoles = MatyBot.config().moderatorRoles.stream().map(String::valueOf).toList().toArray(new String[] {});
		guildOnly = true;

		help = "Things regarding warns";

		children = new SlashCommand[] {
				new AddWarn(), new ClearWarn(), new ListWarns()
		};
	}

	@Override
	protected void execute(SlashCommandEvent event) {
	}

	public static final class AddWarn extends SlashCommand {

		public AddWarn() {
			this.name = "add";
			help = "Adds a new warning to the user";
			options = List.of(new OptionData(OptionType.USER, "user", "The user to warn").setRequired(true),
					new OptionData(OptionType.STRING, "reason", "The reason of the warning").setRequired(true),
					new OptionData(OptionType.BOOLEAN, "public", "If the punishment is public"));
		}

		@Override
		protected void execute(SlashCommandEvent event) {
			String reason = event.getOption("reason").getAsString();
			boolean publicPunishment = event.getOption("public") == null ? true
					: event.getOption("public").getAsBoolean();
			User userToWarn = event.getOption("user").getAsUser();
			Member member = event.getMember();

			final var warn = new Warnings.WarningDocument(reason, event.getUser().getIdLong(),
					Date.from(Instant.now()).getTime());

			MatyBot.database().useExtension(Warnings.class,
					data -> data.addWarn(userToWarn.getIdLong(), event.getGuild().getIdLong(), warn));

			userToWarn.openPrivateChannel().queue(channel -> {
				//@formatter:off
				final var dmEmbed = new EmbedBuilder()
						.setColor(Color.RED)
						.setTitle("New Warning")
						.setDescription("You have been warned in **" + event.getGuild().getName() + "**!")
						.addField("Warner:", member.getAsMention() + " (" + member.getId() + ")", false)
						.addField("Reason:", reason, false)
						.setFooter("Warner ID: " + member.getId(), member.getEffectiveAvatarUrl())
						.setTimestamp(Instant.now());
				channel.sendMessageEmbeds(dmEmbed.build()).queue();
			});
			
			final var embed = new EmbedBuilder()
					.setColor(Color.RED)
					.setTitle("New Warning")
					.setDescription(member.getAsMention() + " warned " + userToWarn.getAsMention() + " ("
						+ userToWarn.getId() + ")!")
					.setThumbnail(userToWarn.getEffectiveAvatarUrl())
					.addField("Reason:", reason, false)
					.setTimestamp(Instant.now())
					.setFooter("Warner ID: " + member.getId(), member.getEffectiveAvatarUrl());
			if (publicPunishment) {
				event.getChannel().sendMessageEmbeds(embed.build()).queue();
			}
			final var loggingChannel = LoggingModule.getLoggingChannel(event.getGuild());
			loggingChannel.sendMessageEmbeds(embed.build()).queue();
			event.getInteraction().reply(new MessageBuilder().append("Warn succesful!").build()).setEphemeral(true)
					.queue();
		}
		//@formatter:on
	}

	public static final class ClearWarn extends SlashCommand {

		public ClearWarn() {
			this.name = "clear";
			help = "Clears a warning from the user";
			options = List.of(
					new OptionData(OptionType.USER, "user", "The user to remove the warn from").setRequired(true),
					new OptionData(OptionType.INTEGER, "index",
							"The index of the warn to remove. Do not provide it if you want to clean all warnings of that user."),
					new OptionData(OptionType.BOOLEAN, "public", "If the un-punishment is public"));
		}

		@Override
		protected void execute(SlashCommandEvent event) {
			boolean publicPunishment = event.getOption("public") == null || event.getOption("public").getAsBoolean();
			User userToWarn = event.getOption("user").getAsUser();
			int warnIndex = (event.getOption("index") == null ? -1 : (int) event.getOption("index").getAsDouble());
			Member member = event.getMember();

			//@formatter:off
			if (warnIndex == -1) {
				MatyBot.database().useExtension(Warnings.class, data -> data.clear(userToWarn.getIdLong(), event.getGuild().getIdLong()));
				
				userToWarn.openPrivateChannel().queue(channel -> {
					final var dmEmbed = new EmbedBuilder()
							.setColor(Color.GREEN)
							.setTitle("Warnings Cleared")
							.setDescription("All of your warnings from **" + event.getGuild().getName() + "** have been cleared!")
							.setTimestamp(Instant.now())
							.setFooter("Un-Warner ID: " + member.getId(), member.getEffectiveAvatarUrl());;
					channel.sendMessageEmbeds(dmEmbed.build()).queue();
				});
				
				final var embed = new EmbedBuilder()
						.setColor(Color.GREEN)
						.setTitle("Warnings Cleared")
						.setDescription("All of the warnings of " + userToWarn.getAsMention() + " (" + userToWarn.getId() + ") have been cleared!")
						.setTimestamp(Instant.now())
						.setFooter("Un-Warner ID: " + member.getId(), member.getEffectiveAvatarUrl());
				
				if (publicPunishment) {
					event.getChannel().sendMessageEmbeds(embed.build()).queue();
				}
				final var loggingChannel = LoggingModule.getLoggingChannel(event.getGuild());
				loggingChannel.sendMessageEmbeds(embed.build()).queue();
				
				event.getInteraction().reply(new MessageBuilder().append("Warnings cleared!").build()).setEphemeral(true).queue();
			} else {
				int currentWarns = MatyBot.database().withExtension(Warnings.class, data -> {
					WarningsDocument warns = data.getWarnings(userToWarn.getIdLong(), event.getGuild().getIdLong());
					return warns == null ? 0 : warns.getWarns().size();
				});
				if (warnIndex >= currentWarns) {
					event.getInteraction().reply(new MessageBuilder().append("The user has " + currentWarns + " warnings!").build()).setEphemeral(true).queue();
					return;
				}
				
				WarningDocument warnDoc = MatyBot.database().withExtension(Warnings.class, data -> 
					data.removeWarn(userToWarn.getIdLong(), event.getGuild().getIdLong(), warnIndex));
				
				userToWarn.openPrivateChannel().queue(channel -> {
					final var dmEmbed = new EmbedBuilder()
							.setColor(Color.GREEN)
							.setTitle("Warning Cleared")
							.setDescription("One of your warnings from **" + event.getGuild().getName() + "** has been cleared!")
							.addField("Old warning reason:", warnDoc.reason(), false)
							.addField("Old warner:", "<@" + warnDoc.warner() + "> (" + warnDoc.warner() + ")", false)
							.setTimestamp(Instant.now())
							.setFooter("Un-Warner ID: " + member.getId(), member.getEffectiveAvatarUrl());
					channel.sendMessageEmbeds(dmEmbed.build()).queue();
				});
				
				final var embed = new EmbedBuilder()
						.setColor(Color.GREEN)
						.setTitle("Warning Cleared")
						.setDescription("One of the warnings of " + userToWarn.getAsMention() + " (" + userToWarn.getId() + ") has been removed!")
						.addField("Old warning reason:", warnDoc.reason(), false)
						.addField("Old warner:", "<@" + warnDoc.warner() + "> (" + warnDoc.warner() + ")", false)
						.setTimestamp(Instant.now())
						.setFooter("Un-Warner ID: " + member.getId(), member.getEffectiveAvatarUrl());
				
				if (publicPunishment) {
					event.getChannel().sendMessageEmbeds(embed.build()).queue();
				}
				final var loggingChannel = LoggingModule.getLoggingChannel(event.getGuild());
				loggingChannel.sendMessageEmbeds(embed.build()).queue();
				
				event.getInteraction().reply(new MessageBuilder().append("Warning cleared!").build()).setEphemeral(true).queue();
			}
			
		}
		//@formatter:on
	}

	public static final class ListWarns extends SlashCommand {

		public ListWarns() {
			this.name = "list";
			help = "Lists the warnings of a user.";
			options = List
					.of(new OptionData(OptionType.USER, "user", "The user to remove the warn from").setRequired(true));
		}

		@Override
		protected void execute(SlashCommandEvent event) {
			User userToSee = event.getOption("user").getAsUser();
			final long userID = userToSee.getIdLong();

			final WarningsDocument warns = MatyBot.database().withExtension(Warnings.class, data -> {
				WarningsDocument doc = data.getWarnings(userID, event.getGuild().getIdLong());
				return doc == null ? new WarningsDocument() : doc;
			});

			final EmbedBuilder embed = new EmbedBuilder()
					.setDescription("The warnings of " + userToSee.getAsMention() + " (" + userID + "): ")
					.setTimestamp(Instant.now()).setColor(Color.MAGENTA);
			for (int i = 0; i < warns.getWarns().size(); i++) {
				final WarningDocument doc = warns.getWarns().get(i);
				embed.addField("Warning " + i + ":",
						"Reason: **" + doc.reason() + "**; Warner: " + mentionAndID(doc.warner()) + "; Timestamp: "
								+ new Timestamp(TimeFormat.DATE_TIME_LONG, doc.timeStamp()) {}.toString(),
						false);
			}

			event.getInteraction().replyEmbeds(embed.build()).queue();
		}

	}

	private static String mentionAndID(final long id) {
		return "<@" + id + "> (" + id + ")";
	}
}
