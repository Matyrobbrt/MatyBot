package io.github.matyrobbrt.matybot.modules.commands.slash.moderation;

import java.awt.Color;
import java.time.Instant;
import java.util.concurrent.ExecutionException;

import com.jagrosh.jdautilities.command.SlashCommandEvent;

import io.github.matyrobbrt.matybot.MatyBot;
import io.github.matyrobbrt.matybot.api.annotation.RegisterSlashCommand;
import io.github.matyrobbrt.matybot.api.command.slash.GuildSpecificSlashCommand;
import io.github.matyrobbrt.matybot.modules.logging.LoggingModule;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class ClearSlashCommand extends GuildSpecificSlashCommand {

	@RegisterSlashCommand
	private static final ClearSlashCommand CMD = new ClearSlashCommand();

	public ClearSlashCommand() {
		super("");
		this.name = "clear";
		help = "Clears x amount of messages!";
		enabledRolesGetter = guildId -> MatyBot.getConfigForGuild(guildId).moderatorRoles.stream().map(String::valueOf)
				.toArray(String[]::new);
		options.add(new OptionData(OptionType.NUMBER, "amount", "The amount of messages that should be deleted!")
				.setRequired(true).setRequiredRange(1, 100));
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
