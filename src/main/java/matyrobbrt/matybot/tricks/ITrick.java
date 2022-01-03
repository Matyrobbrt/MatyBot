package matyrobbrt.matybot.tricks;

import java.util.List;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public interface ITrick {

	/**
	 * @return the trick names, including aliases
	 */
	List<String> getNames();

	/**
	 * @param args the args
	 * @return the message to send
	 */
	Message getMessage(String[] args);

	/**
	 * The TrickType interface. Every trick requires a trick type to be registered
	 *
	 * @param <T> the trick
	 */
	interface TrickType<T extends ITrick> {

		/**
		 * @return the trick class
		 */
		Class<T> getTrickClass();

		/**
		 * @return the argument argument names
		 * @deprecated use slash commands where possible
		 */
		@Deprecated(forRemoval = false)
		default List<String> getArgNames() { return getArgs().stream().map(OptionData::getName).toList(); }

		/**
		 * Create a trick from string arguments.
		 *
		 * @param args the args as a single string
		 * @return the trick
		 * @deprecated use slash commands when possible
		 */
		@Deprecated(forRemoval = false)
		T createFromArgs(String args);

		/**
		 * @return a list of arguments as OptionData
		 */
		List<OptionData> getArgs();

		/**
		 * Create from args t.
		 *
		 * @param event the command event
		 * @return the trick
		 */
		T createFromCommand(SlashCommandEvent event);
	}
}
