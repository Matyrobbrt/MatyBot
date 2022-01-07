package matyrobbrt.matybot.modules.commands.slash.bot;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;

import matyrobbrt.matybot.MatyBot;
import matyrobbrt.matybot.api.annotation.RegisterSlashCommand;
import matyrobbrt.matybot.util.BotUtils;

public final class CommandShutdown extends SlashCommand {

	@RegisterSlashCommand
	private static final CommandShutdown CMD = new CommandShutdown();

	public CommandShutdown() {
		name = "shutdown";
		help = "Shuts the bot down without restarting it. (Only usable by the bot owner)";
		ownerCommand = true;
		guildOnly = false;
	}

	@Override
	protected void execute(final SlashCommandEvent event) {
		event.reply("Shutting down the bot!").queue();
		event.getJDA().shutdown();
		MatyBot.LOGGER.warn("Shutting down the bot by request of {} via Discord!", event.getUser().getName());
		BotUtils.scheduleTask(() -> System.exit(0), 1000);
	}

}
