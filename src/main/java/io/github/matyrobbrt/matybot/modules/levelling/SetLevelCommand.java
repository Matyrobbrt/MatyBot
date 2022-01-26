package io.github.matyrobbrt.matybot.modules.levelling;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import io.github.matyrobbrt.matybot.MatyBot;
import io.github.matyrobbrt.matybot.api.annotation.RegisterCommand;
import net.dv8tion.jda.api.Permission;

public class SetLevelCommand extends Command {

	@RegisterCommand
	private static final SetLevelCommand CMD = new SetLevelCommand();

	public SetLevelCommand() {
		name = "set-level";
		aliases = new String[] {
				"setlevel", "setlvl", "set-lvl"
		};
		userPermissions = new Permission[] {
				Permission.ADMINISTRATOR
		};
	}

	@Override
	protected void execute(CommandEvent event) {
		final var args = event.getArgs().split(" ");
		if (args.length < 1) {
			event.getMessage().reply("Please specify the user.").queue();
			return;
		}
		if (args.length < 2) {
			event.getMessage().reply("Please specify the new level.").queue();
			return;
		}
		try {
			final var memberId = Long.parseLong(args[0]);
			final var newLevel = Integer.parseInt(args[1]);
			final var newXp = LevellingModule.getXPForLevel(newLevel, event.getGuild());
			MatyBot.nbtDatabase().getDataForGuild(event.getGuild()).getLevelDataForUser(memberId)
					.setXp(newXp);
			MatyBot.nbtDatabase().setDirty();
			event.getMessage().reply("The user's level has been set!").queue();
		} catch (Exception e) {
			event.getMessage().reply("There was an exception while trying to run that command! **%s**"
					.formatted(e.getLocalizedMessage())).queue();
			;
		}
	}

}
