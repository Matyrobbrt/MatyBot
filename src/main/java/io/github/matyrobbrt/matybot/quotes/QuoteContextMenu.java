package io.github.matyrobbrt.matybot.quotes;

import io.github.matyrobbrt.matybot.api.annotation.RegisterContextMenu;
import io.github.matyrobbrt.matybot.api.command.slash.ContextMenu;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command.Type;

public class QuoteContextMenu extends ContextMenu {

	@RegisterContextMenu
	private static final QuoteContextMenu MENU = new QuoteContextMenu();

	public QuoteContextMenu() {
		this.type = Type.MESSAGE;
		name = "Quote";
		guildOnly = true;
	}

	@Override
	public void onMessageContextInteraction(MessageContextInteractionEvent event) {
		final String quote = event.getTarget().getContentRaw();
		final User author = event.getTarget().getAuthor();
		final Member quoter = event.getMember();

		QuoteManager.addQuote(new Quote(quote, author.getIdLong(), quoter.getIdLong()), event.getGuild().getIdLong());
		event.getInteraction().reply("Quote with the index "
				+ QuoteManager.getQuotesForGuild(event.getGuild().getIdLong()).size() + " added!").queue();
	}

}
