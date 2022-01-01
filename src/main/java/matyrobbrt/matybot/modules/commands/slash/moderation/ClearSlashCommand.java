package matyrobbrt.matybot.modules.commands.slash.moderation;

import java.awt.Color;
import java.time.Instant;
import java.util.concurrent.ExecutionException;

import com.jagrosh.jdautilities.command.SlashCommand;

import matyrobbrt.matybot.MatyBot;
import matyrobbrt.matybot.annotation.RegisterSlashCommand;
import matyrobbrt.matybot.modules.logging.LoggingModule;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class ClearSlashCommand extends SlashCommand {

	@RegisterSlashCommand
	private static final ClearSlashCommand CMD = new ClearSlashCommand();

	public ClearSlashCommand() {
		this.name = "clear";
		help = "Clears x amount of messages!";
		defaultEnabled = false;
		enabledRoles = MatyBot.config().moderatorRoles.stream().map(String::valueOf).toArray(String[]::new);
		options.add(new OptionData(OptionType.NUMBER, "amount", "The amount of messages that should be deleted!")
				.setRequired(true).setRequiredRange(1, 100));
		guildOnly = true;
	}

	@Override
	protected void execute(SlashCommandEvent event) {
		final int deletionCount = (int) event.getOption("amount").getAsDouble();
		final var channel = (TextChannel) event.getChannel();
		new Thread(() -> {
			try {
				event.getChannel().getIterableHistory().takeAsync(deletionCount).thenAccept(channel::purgeMessages)
						.get();
				event.getInteraction().reply("Bulk deletion successful!").setEphemeral(true).queue();
				final TextChannel loggingChannel = LoggingModule.getLoggingChannel(event.getGuild());
				if (loggingChannel != null) {
					final MessageEmbed embed = new EmbedBuilder().setTitle("Bulk Deletion!")
							.setDescription(event.getUser().getAsMention() + " bulk deleted " + deletionCount
									+ " messages in " + channel.getAsMention())
							.setFooter("Moderator ID: " + event.getUser().getIdLong(),
									event.getMember().getEffectiveAvatarUrl())
							.setColor(Color.RED).setTimestamp(Instant.now()).build();
					loggingChannel.sendMessageEmbeds(embed).queue();
				}
			} catch (InterruptedException | ExecutionException e) {
				event.getInteraction().reply("There was an error while trying to delete the messages!")
						.setEphemeral(true).queue();
			}
		}, "Bulk deletion").start();
	}

}
