package io.github.matyrobbrt.matybot.modules.suggestions;

import java.awt.Color;
import java.time.Instant;
import java.util.List;

import com.jagrosh.jdautilities.command.CooldownScope;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;

import io.github.matyrobbrt.matybot.MatyBot;
import io.github.matyrobbrt.matybot.api.annotation.RegisterSlashCommand;
import io.github.matyrobbrt.matybot.util.BotUtils;
import io.github.matyrobbrt.matybot.util.DiscordUtils;
import io.github.matyrobbrt.matybot.util.database.dao.nbt.SuggestionData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.MarkdownUtil;

public class SuggestCommand extends SlashCommand {

	@RegisterSlashCommand
	private static final SuggestCommand CMD = new SuggestCommand();

	private SuggestCommand() {
		name = "suggest";
		cooldown = 1800;
		cooldownScope = CooldownScope.USER_GUILD;
		help = "Makes a suggestion.";
		guildOnly = true;
		options = List.of(new OptionData(OptionType.STRING, "suggestion", "The suggestion that you would like to make.")
				.setRequired(true), new OptionData(OptionType.STRING, "media_link", "An optional media link."));
	}

	@Override
	protected void execute(SlashCommandEvent event) {
		if (!SuggestionsModule.areSuggestionsEnabled(event.getGuild())) {
			event.reply("Suggestions are disabled in this guild!").setEphemeral(true).queue();
		}
		final var suggestion = BotUtils.getArgumentOrEmpty(event, "suggestion");
		final var mediaLink = BotUtils.getArgumentOrEmpty(event, "media_link");
		final var author = event.getMember();
		MatyBot.getInstance().getChannelIfPresent(MatyBot.getConfigForGuild(event.getGuild()).suggestionsChannel,
				suggestionsChannel -> {
					final var embed = new EmbedBuilder();
					embed.setTitle("Sugestion");
					embed.setDescription(suggestion);
					if (!mediaLink.isBlank()) {
						embed.setImage(mediaLink);
					}
					embed.setColor(Color.BLUE);
					embed.setTimestamp(Instant.now());
					embed.setFooter(author.getUser().getAsTag(), author.getEffectiveAvatarUrl());
					suggestionsChannel.sendMessageEmbeds(embed.build()).queue(m -> {
						m.editMessage("** **")
								.setActionRows(ActionRow.of(
										Button.success("approve_suggestion_" + m.getIdLong(), Emoji.fromMarkdown("☑")),
										Button.secondary("consider_suggestion_" + m.getIdLong(),
												Emoji.fromMarkdown("🤔")),
										Button.danger("deny_suggestion_" + m.getIdLong(), Emoji.fromMarkdown("✖"))))
								.queue();
						MatyBot.nbtDatabase().getDataForGuild(event).getSuggestions().computeIfAbsent(m.getIdLong(),
								k -> new SuggestionData(author.getIdLong()));
						MatyBot.nbtDatabase().setDirtyAndSave();
						final var responseEmbed = new EmbedBuilder();
						responseEmbed.setTitle("Your suggestion has been added!");
						responseEmbed.setDescription(
								MarkdownUtil.maskedLink("Jump to suggestion.", DiscordUtils.createMessageLink(m)));
						event.deferReply().addEmbeds(responseEmbed.build()).queue();
					});
				},
				() -> event.reply(
						"This guild does not have a suggestions channel set. You should report that to the server owner.")
						.setEphemeral(true).queue());
	}

}
