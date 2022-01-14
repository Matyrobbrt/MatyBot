package matyrobbrt.matybot.modules.commands.normal;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import matyrobbrt.matybot.MatyBot;
import matyrobbrt.matybot.api.annotation.RegisterCommand;

public class SaveDBCommand extends Command {

	@RegisterCommand
	private static final SaveDBCommand CMD = new SaveDBCommand();

	private SaveDBCommand() {
		name = "savedb";
		aliases = new String[] {
				"save-db", "savedatabase", "save-database"
		};
		ownerCommand = true;
		hidden = true;
	}

	@Override
	protected void execute(CommandEvent event) {
		MatyBot.nbtDatabase().setDirtyAndSave();
		event.getMessage().reply("Database has been saved!").queue();
	}

}
