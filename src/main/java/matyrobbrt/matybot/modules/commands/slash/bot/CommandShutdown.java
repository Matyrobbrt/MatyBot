package matyrobbrt.matybot.modules.commands.slash.bot;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;

import matyrobbrt.matybot.MatyBot;
import matyrobbrt.matybot.api.annotation.RegisterCommand;
import matyrobbrt.matybot.api.annotation.RegisterSlashCommand;
import matyrobbrt.matybot.util.BotUtils;
import matyrobbrt.matybot.util.Constants;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

public final class CommandShutdown extends SlashCommand {

	@RegisterSlashCommand
	private static final CommandShutdown CMD = new CommandShutdown();

	@RegisterCommand
	private static final Normal CMD_NORMAL = new Normal();

	private CommandShutdown() {
		name = "shutdown";
		help = "Shuts the bot down without restarting it. (Only usable by the bot owner)";
		ownerCommand = true;
		guildOnly = false;
	}

	@Override
	protected void execute(final SlashCommandEvent event) {
		event.reply("Shutting down the bot!").queue();
		event.getJDA().shutdown();
		MatyBot.nbtDatabase().setDirtyAndSave();
		MatyBot.LOGGER.warn("Shutting down the bot by request of {} via Discord!", event.getUser().getName());
		BotUtils.scheduleTask(() -> System.exit(0), 2000);
	}

	private static final class Normal extends Command {

		private Normal() {
			name = "shutdown";
			ownerCommand = true;
			hidden = true;
		}

		@Override
		protected void execute(CommandEvent event) {
			event.getMessage().reply("Are you sure? React with :warning: below.").queue(m -> {
				m.addReaction("U+26A0").queue();
				Constants.EVENT_WAITER.waitForEvent(MessageReactionAddEvent.class,
						e -> e.getMessageIdLong() == m.getIdLong() && e.getUserIdLong() == event.getMember().getIdLong()
								&& e.getReactionEmote().getAsCodepoints().equalsIgnoreCase("U+26A0"),
						confirm -> {
							m.editMessage("Shutting down the bot!").queue();
							event.getJDA().shutdown();
							MatyBot.nbtDatabase().setDirtyAndSave();
							MatyBot.LOGGER.warn("Shutting down the bot by request of {} via Discord!",
									event.getMessage().getMember().getUser().getName());
							BotUtils.scheduleTask(() -> System.exit(0), 2000);
						});
			});
		}

	}

}
