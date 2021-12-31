package matyrobbrt.matybot.modules.commands.tricks;

import java.util.List;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;

import matyrobbrt.matybot.annotation.RegisterSlashCommand;
import matyrobbrt.matybot.tricks.ITrick;
import matyrobbrt.matybot.tricks.TrickManager;
import matyrobbrt.matybot.util.BotUtils;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public final class RunTrickCommand extends Command {

	private final ITrick trick;

	public RunTrickCommand(final ITrick trick) {
		this.trick = trick;
		List<String> trickNames = trick.getNames();
		name = trickNames.get(0);
		aliases = trickNames.size() > 1 ? trickNames.subList(1, trickNames.size()).toArray(new String[0])
				: new String[0];
		guildOnly = true;
	}

	@Override
	protected void execute(CommandEvent event) {
		final var channel = event.getTextChannel();
		channel.sendMessage(trick.getMessage(event.getArgs().split(" "))).queue();
	}

	public static final class Slash extends SlashCommand {

		@RegisterSlashCommand
		private static final Slash SLASH_CMD = new Slash();

		public Slash() {
			name = "trick";
			help = "Run a specific trick by name.";
			guildOnly = true;

			options = List.of(
					new OptionData(OptionType.STRING, "name", "The name of the trick to run").setRequired(true),
					new OptionData(OptionType.STRING, "args",
							"The arguments of the trick to run. Separate them by spaces"));
		}

		@Override
		protected void execute(SlashCommandEvent event) {
			final String args = BotUtils.getArgumentOrEmpty(event, "args");
			TrickManager.getTrick(event.getOption("name").getAsString()).ifPresentOrElse(
					trick -> event.reply(trick.getMessage(args.split(" "))).setEphemeral(false).queue(),
					() -> event.reply("A trick with that name could not be found.").setEphemeral(true).queue());
		}

	}

}
