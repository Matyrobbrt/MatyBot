package io.github.matyrobbrt.matybot.managers.quotes;

import java.awt.Color;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;

import io.github.matyrobbrt.jdautils.event.EventListenerWrapper;
import io.github.matyrobbrt.matybot.MatyBot;
import io.github.matyrobbrt.matybot.api.annotation.RegisterSlashCommand;
import io.github.matyrobbrt.matybot.api.command.slash.PaginatedCommand;
import io.github.matyrobbrt.matybot.util.BotUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class QuoteCommand extends SlashCommand {

	@RegisterSlashCommand
	private static final QuoteCommand CMD = new QuoteCommand();

	public QuoteCommand() {
		name = "quote";
		children = new SlashCommand[] {
				new AddQuote(), new RemoveQuote(), new GetQuote(), new ListQuotes()
		};
		guildOnly = true;
	}

	@Override
	protected void execute(SlashCommandEvent event) {
	}

	public static final class RemoveQuote extends SlashCommand {

		public RemoveQuote() {
			name = "remove";
			help = "Removes a quote";
			options = List.of(
					new OptionData(OptionType.INTEGER, "index", "The index of the quote to remove.").setRequired(true));
		}

		@Override
		protected void execute(SlashCommandEvent event) {
			final int index = (int) event.getOption("index").getAsDouble() - 1;
			final long guildId = event.getGuild().getIdLong();
			if (index >= QuoteManager.getQuotesForGuild(guildId).size()) {
				event.getInteraction().reply("This quote does not exist!").queue();
				return;
			}
			final Quote toRemove = QuoteManager.getQuotesForGuild(guildId).get(index);
			if (!event.getMember().hasPermission(Permission.MESSAGE_MANAGE)
					&& event.getMember().getIdLong() != toRemove.getQuoter()) {
				event.getInteraction().reply("You do not have the permissions required to remove this quote!").queue();
				return;
			}
			QuoteManager.removeQuote(index, guildId);
			event.getInteraction().reply("Quote succesfully removed!").queue();
		}
	}

	public static final class AddQuote extends SlashCommand {

		public AddQuote() {
			name = "add";
			help = "Adds a new quote";
			options = List.of(new OptionData(OptionType.STRING, "quote", "The content of the quote").setRequired(true),
					new OptionData(OptionType.USER, "author", "The author of the quote").setRequired(true));
		}

		@Override
		protected void execute(SlashCommandEvent event) {
			final String quote = event.getOption("quote").getAsString();
			final User author = event.getOption("author").getAsUser();
			final Member quoter = event.getMember();

			QuoteManager.addQuote(new Quote(quote, author.getIdLong(), quoter.getIdLong()),
					event.getGuild().getIdLong());
			event.getInteraction().reply("Quote with the index "
					+ QuoteManager.getQuotesForGuild(event.getGuild().getIdLong()).size() + " added!").queue();
		}
	}

	public static final class GetQuote extends SlashCommand {

		private static final Random RAND = new Random();

		public GetQuote() {
			name = "get";
			help = "Gets a quote";
			options = List.of(new OptionData(OptionType.INTEGER, "index",
					"The index of the quote to fetch. Do not provide it in order to get a random quote."));
		}

		@Override
		protected void execute(SlashCommandEvent event) {
			final long guildId = event.getGuild().getIdLong();

			final int quoteIndex = BotUtils.getOptionOr(event.getOption("index"), m -> (int) m.getAsDouble(),
					RAND.nextInt(QuoteManager.getQuotesForGuild(guildId).size()) + 1);
			final int actualIndex = quoteIndex - 1;

			final Quote quote = QuoteManager.getQuotesForGuild(guildId).get(actualIndex);

			final User quoter = MatyBot.getInstance().getJDA().getUserById(quote.getQuoter());

			final String footer = quoter == null ? "Quoter ID: %s".formatted(quote.getAuthor())
					: "Quoted by %s".formatted(quoter.getAsTag());

			final String author = quote.getAuthorFormatted(event.getGuild(), false);

			final EmbedBuilder embed = new EmbedBuilder().setColor(new Color((int) (Math.random() * 0x1000000)))
					.setTitle("Quote #" + quoteIndex).addField("Content", "> " + quote.getQuote(), false)
					.addField("Author", author, false).setFooter(footer).setTimestamp(Instant.now());

			event.getInteraction().replyEmbeds(embed.build()).queue();
		}
	}

	public static final class ListQuotes extends PaginatedCommand {

		private static ButtonListener listener;

		public ListQuotes() {
			super("list", "Get all quotes.", true, new ArrayList<>(), 10);
			guildOnly = true;

			listener = new ButtonListener();
		}

		public static EventListenerWrapper getWrappedListener() {
			return new EventListenerWrapper(listener);
		}

		@Override
		protected EmbedBuilder getEmbed(int start, final Guild guild) {
			EmbedBuilder embed;
			final var randomColour = BotUtils.generateRandomColor();
			final var quotes = QuoteManager.getQuotesForGuild(guild);
			if (quotes.isEmpty()) {
				embed = new EmbedBuilder().setColor(randomColour)
						.setDescription("There are no quotes loaded currently.")
						.setTimestamp(Instant.now());
			} else {
				embed = new EmbedBuilder().setColor(randomColour).setTitle("Quote Page " + ((start / itemsPerPage) + 1))
						.setTimestamp(Instant.now());
			}

			for (int x = start; x < start + itemsPerPage; x++) {
				if (x >= quotes.size()) {
					break;
				}

				Quote fetchedQuote = quotes.get(x);

				embed.addField(String.valueOf(x + 1),
						fetchedQuote == null ? "Quote does not exist."
								: fetchedQuote.getQuote() + " - " + fetchedQuote.getAuthorFormatted(guild, true),
						false);
			}

			return embed;
		}

		@Override
		protected void execute(io.github.matyrobbrt.matybot.reimpl.SlashCommandEvent event) {
			updateMaximum(event.getGuild().getQuotes().size() - 1);
			sendPaginatedMessage(event);
		}

		public class ButtonListener extends PaginatedCommand.ButtonListener {

			@Override
			public String getButtonID() { return "quote_list"; }
		}

	}
}
