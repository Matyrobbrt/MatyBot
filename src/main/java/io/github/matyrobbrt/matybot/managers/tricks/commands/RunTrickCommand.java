package io.github.matyrobbrt.matybot.managers.tricks.commands;

import java.util.List;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;

import io.github.matyrobbrt.matybot.api.annotation.RegisterSlashCommand;
import io.github.matyrobbrt.matybot.managers.tricks.ITrick;
import io.github.matyrobbrt.matybot.managers.tricks.TrickManager;
import io.github.matyrobbrt.matybot.util.BotUtils;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public final class RunTrickCommand extends Command {

	private final ITrick trick;
	private final long guild;
	private final boolean isGlobal;

	public static RunTrickCommand createGlobal(final ITrick trick) {
		return new RunTrickCommand(trick, true, 0);
	}

	public static RunTrickCommand createGuild(final ITrick trick, final long guild) {
		return new RunTrickCommand(trick, false, guild);
	}

	private RunTrickCommand(final ITrick trick, final boolean isGlobal, final long guild) {
		this.trick = trick;
		this.isGlobal = isGlobal;
		this.guild = guild;
		List<String> trickNames = trick.getNames();
		name = trickNames.get(0);
		aliases = trickNames.size() > 1 ? trickNames.subList(1, trickNames.size()).toArray(new String[0])
				: new String[0];
		guildOnly = true;
	}

	@Override
	protected void execute(CommandEvent event) {
		if (isGlobal || event.getGuild().getIdLong() == guild) {
			final var channel = event.getTextChannel();
			channel.sendMessage(trick.getMessage(event.getArgs().split(" "))).queue();
		}
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
			TrickManager.getTrick(event.getGuild().getIdLong(), event.getOption("name").getAsString()).ifPresentOrElse(
					trick -> event.reply(trick.getMessage(args.split(" "))).setEphemeral(false).queue(),
					() -> event.reply("A trick with that name could not be found.").setEphemeral(true).queue());
		}

	}

}
