package io.github.matyrobbrt.matybot.api.command.slash;

import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.jagrosh.jdautilities.command.SlashCommandEvent;

import io.github.matyrobbrt.matybot.reimpl.MatyBotSlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

/**
 * A wrapper for Slash Commands which require a paginated embed. It handles the
 * buttons and interactions for you.
 * <p>
 * To use this, the developer needs to: - implement {@link #getEmbed(int)} as
 * per the javadoc. - implement {@link ButtonListener} in the child class, along
 * with the {@link ButtonListener#getButtonID()} method. - register their
 * implementation of {@link ButtonListener} to the
 * {@link com.jagrosh.jdautilities.command.CommandClient}. - call
 * {@link #updateMaximum(int)} as required - usually once per invocation - call
 * {@link #sendPaginatedMessage(SlashCommandEvent)} in the execute method when a
 * paginated embed is wanted.
 */
public abstract class PaginatedCommand extends MatyBotSlashCommand {

	// How many items should be sent per individual page. Defaults to the maximum
	// field count for an Embed, 25.
	protected int itemsPerPage = 25;
	// The maxmimum number of items in the list. Update with #updateMaximum
	protected int maximum = 0;

	protected PaginatedCommand(String name, String help, boolean guildOnly, List<OptionData> options, int items) {
		this.name = name;
		this.help = help;

		this.guildOnly = guildOnly;

		itemsPerPage = items;

		this.options = options;
	}

	/**
	 * Given the index of the start of the embed, get the next ITEMS_PER_PAGE items.
	 * This is where the implementation of the paginated command steps in.
	 *
	 * @param  startingIndex the index of the first item in the list.
	 * @param  guild         the guild the command is ran in
	 * @return               an unbuilt embed that can be sent.
	 */
	protected abstract EmbedBuilder getEmbed(int startingIndex, final Guild guild);

	/**
	 * Set a new maximum index into the paginated list. Updates the point at which
	 * buttons are created in new queries.
	 */
	protected void updateMaximum(int newMaxmimum) {
		maximum = newMaxmimum;
	}

	/**
	 * Create and queue a ReplyAction which, if the number of items requires, also
	 * contains buttons for scrolling.
	 *
	 * @param event the active SlashCommandEvent.
	 */
	protected void sendPaginatedMessage(SlashCommandEvent event) {
		var reply = event.replyEmbeds(getEmbed(0, event.getGuild()).build());
		Button[] buttons = createScrollButtons(0);
		if (buttons.length > 0) {
			reply.addActionRows(ActionRow.of(Arrays.asList(buttons)));
		}
		reply.queue();
	}

	/**
	 * Create the row of Component interaction buttons.
	 * <p>
	 * Currently, this just creates a left and right arrow. Left arrow scrolls back
	 * a page. Right arrow scrolls forward a page.
	 *
	 * @param  start The quote number at the start of the current page.
	 * @return       A row of buttons to go back and forth by one page.
	 */
	private Button[] createScrollButtons(int start) {
		Button backward = Button.primary(getName() + "-" + start + "-prev", Emoji.fromUnicode("U+25C0")).asDisabled();
		Button forward = Button.primary(getName() + "-" + start + "-next", Emoji.fromUnicode("U+25B6")).asDisabled();

		if (start != 0) {
			backward = backward.asEnabled();
		}

		if (start + itemsPerPage < maximum) {
			forward = forward.asEnabled();
		}

		return new Button[] {
				backward, forward
		};
	}

	/**
	 * Listens for interactions with the scroll buttons on the paginated message.
	 * Extend and implement as a child class of the Paginated Message.
	 * <p>
	 * Implement the {@link #getButtonID()} function in any way you like. Make sure
	 * that this listener is registered to the
	 * {@link com.jagrosh.jdautilities.command.CommandClient}.
	 */
	public abstract class ButtonListener extends ListenerAdapter {

		public abstract String getButtonID();

		@Override
		public void onButtonInteraction(@NotNull final ButtonInteractionEvent event) {
			var button = event.getButton();
			if (button.getId() == null) { return; }

			String[] idParts = button.getId().split("-");
			if (idParts.length != 3) { return; }

			if (!idParts[0].equals(getButtonID())) { return; }

			int current = Integer.parseInt(idParts[1]);

			if (idParts[2].equals("next")) {
				event.editMessageEmbeds(getEmbed(current + itemsPerPage, event.getGuild()).build())
						.setActionRow(createScrollButtons(current + itemsPerPage)).queue();
			} else {
				if (idParts[2].equals("prev")) {
					event.editMessageEmbeds(getEmbed(current - itemsPerPage, event.getGuild()).build())
							.setActionRow(createScrollButtons(current - itemsPerPage)).queue();
				}
			}
		}
	}
}
