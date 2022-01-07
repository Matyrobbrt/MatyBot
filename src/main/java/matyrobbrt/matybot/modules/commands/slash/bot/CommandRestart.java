package matyrobbrt.matybot.modules.commands.slash.bot;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;

import matyrobbrt.matybot.MatyBot;
import matyrobbrt.matybot.api.annotation.RegisterSlashCommand;
import matyrobbrt.matybot.modules.commands.CommandsModule;
import matyrobbrt.matybot.util.BotUtils;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class CommandRestart extends SlashCommand {

	@RegisterSlashCommand
	private static final CommandRestart CMD = new CommandRestart();

	public CommandRestart() {
		name = "restart";
		help = "Restarts the bot.";
		ownerCommand = true;
		guildOnly = false;
		options.add(new OptionData(OptionType.BOOLEAN, "clear_guild_commands",
				"If the slash commands of this guild will be cleared before restarting the bot!"));
	}

	@Override
	protected void execute(SlashCommandEvent event) {
		if (Boolean.TRUE.equals(
				BotUtils.getOptionOr(event.getOption("clear_guild_commands"), OptionMapping::getAsBoolean, false))) {
			try {
				AtomicReference<InteractionHook> msg = new AtomicReference<>(
						event.getInteraction().reply("Waiting for command deletion...").submit().get());
				MatyBot.LOGGER.warn(
						"Deleting the guild commands of the guild with the id {} at the request of {} via Discord!",
						event.getGuild().getIdLong(), event.getUser().getName());
				new Thread(() -> {
					CommandsModule.clearCommands(event.getGuild(), () -> {
						msg.get().editOriginal("Restarting the bot!").queue();
						event.getJDA().shutdown();
						MatyBot.LOGGER.warn("Restarting the bot by request of {} via Discord!",
								event.getUser().getName());
						// Restart the JDA instance
						BotUtils.scheduleTask(() -> MatyBot.main(new String[] {}), 1000);
					});
				}, "Guild command clearing").start();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
				event.getInteraction().reply("Error! Restart cancelled!");
			}
		} else {
			event.reply("Restarting the bot!").queue();
			// Shutdown the JDA instace
			event.getJDA().shutdown();
			MatyBot.LOGGER.warn("Restarting the bot by request of {} via Discord!", event.getUser().getName());
			// Restart the JDA instance
			BotUtils.scheduleTask(() -> MatyBot.main(new String[] {}), 1000);
		}
	}

}
