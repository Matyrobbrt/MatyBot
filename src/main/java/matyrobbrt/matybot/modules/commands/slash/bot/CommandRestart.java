package matyrobbrt.matybot.modules.commands.slash.bot;

import com.jagrosh.jdautilities.command.SlashCommand;

import matyrobbrt.matybot.MatyBot;
import matyrobbrt.matybot.annotation.RegisterSlashCommand;
import matyrobbrt.matybot.util.BotUtils;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

public class CommandRestart extends SlashCommand {

	@RegisterSlashCommand
	private static final CommandRestart CMD = new CommandRestart();

	public CommandRestart() {
		name = "restart";
		help = "Restarts the bot.";
		ownerCommand = true;
		guildOnly = false;
	}

	@Override
	protected void execute(SlashCommandEvent event) {
		event.reply("Restarting the bot!").queue();
		// Shutdown the JDA instace
		event.getJDA().shutdown();
		MatyBot.LOGGER.warn("Restarting the bot by request of {} via Discord!", event.getUser().getName());
		// Restart the JDA instance
		BotUtils.scheduleTask(() -> MatyBot.main(new String[] {}), 1000);
	}

}
