package io.github.matyrobbrt.matybot.modules.suggestions;

import java.awt.Color;
import java.time.Instant;
import java.util.List;
import java.util.Locale;

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
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
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
		guildOnly = false;
		cooldownScope = CooldownScope.USER_GUILD;
		help = "Makes a suggestion.";
		options = List.of(
				new OptionData(OptionType.STRING, "suggestion", "The suggestion that you would like to make.")
						.setRequired(true),
				new OptionData(OptionType.STRING, "type",
						"The type of the suggestion. This feature is guild-specific, please contact the owner for more info."),
				new OptionData(OptionType.STRING, "media_link", "An optional media link."));
	}

	@Override
	protected void execute(SlashCommandEvent event) {
		if (!event.isFromGuild()) {
			event.deferReply(true).setContent("This command only works in guilds!").queue();
			return;
		}
		if (!SuggestionsModule.areSuggestionsEnabled(event.getGuild())) {
			event.reply("Suggestions are disabled in this guild!").setEphemeral(true).queue();
			return;
		}
		final var suggestion = BotUtils.getArgumentOrEmpty(event, "suggestion");
		final var mediaLink = BotUtils.getArgumentOrEmpty(event, "media_link");
		final var suggestionType = BotUtils.getOptionOr(event.getOption("type"), OptionMapping::getAsString, "general")
				.toLowerCase(Locale.ROOT);
		final var author = event.getMember();
		MatyBot.getInstance().getChannelIfPresent(
				MatyBot.getConfigForGuild(event.getGuild()).getSuggestionChannel(suggestionType),
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
					if (!suggestionType.equalsIgnoreCase("general")
							&& MatyBot.getConfigForGuild(event.getGuild()).hasSuggestionType(suggestionType)) {
						embed.addField("Suggestion Type", suggestionType, true);
					}
					suggestionsChannel.sendMessageEmbeds(embed.build()).queue(m -> {
						m.addReaction("U+2B06").queue();
						m.addReaction("U+2B07").queue();
						m.createThreadChannel("Discussion of %s’s suggestion".formatted(author.getUser().getName()))
								.queue(thread -> thread.addThreadMember(author).queue());
						m.editMessage("** **")
								.setActionRows(ActionRow.of(
										Button.success("approve_suggestion_" + m.getIdLong(), Emoji.fromMarkdown("☑")),
										Button.secondary("consider_suggestion_" + m.getIdLong(),
												Emoji.fromMarkdown("🤔")),
										Button.danger("deny_suggestion_" + m.getIdLong(), Emoji.fromMarkdown("✖"))))
								.queue();
						MatyBot.nbtDatabase().getDataForGuild(event).getSuggestions().computeIfAbsent(m.getIdLong(),
								k -> new SuggestionData(author.getIdLong(), suggestionsChannel.getIdLong()));
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
