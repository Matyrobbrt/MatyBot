package matyrobbrt.matybot.modules.commands;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.command.SlashCommand;

import matyrobbrt.matybot.MatyBot;
import matyrobbrt.matybot.annotation.RegisterCommand;
import matyrobbrt.matybot.annotation.RegisterSlashCommand;
import matyrobbrt.matybot.api.event.EventListenerWrapper;
import matyrobbrt.matybot.quotes.QuoteCommand;
import matyrobbrt.matybot.tricks.TrickManager;
import matyrobbrt.matybot.util.ReflectionUtils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.hooks.EventListener;

public final class CommandsModule extends matyrobbrt.matybot.api.modules.Module {

	private static CommandsModule instance;

	public static CommandsModule getInstance() { return instance; }

	public static CommandsModule setUpInstance(final JDA bot) {
		if (instance == null) {
			instance = new CommandsModule(bot);
		}
		return instance;
	}

	public CommandsModule(final JDA bot) {
		super(MatyBot.config()::isCommandsModuleEnabled, bot);

		var builder = new CommandClientBuilder().setOwnerId(MatyBot.config().getBotOwner()).useHelpBuilder(false)
				.setPrefix(MatyBot.config().mainPrefix)
				.setPrefixes(MatyBot.config().alternativePrefixes.toArray(new String[] {}));

		collectSlashCommands().stream().filter(Objects::nonNull).forEach(c -> {
			builder.addSlashCommand(c);
			upsertCommand(c);
		});
		collectPrefixCommands().stream().filter(Objects::nonNull).forEach(builder::addCommand);

		builder.forceGuildOnly(MatyBot.config().getGuildID());

		TrickManager.createTrickCommands().forEach(builder::addCommand);

		this.commandClient = builder.build();
	}

	private final CommandClient commandClient;

	public CommandClient getCommandClient() { return commandClient; }

	@Override
	public void register() {
		super.register();

		if (isEnabled()) {
			bot.addEventListener(new EventListenerWrapper((EventListener) commandClient));
			bot.addEventListener(QuoteCommand.ListQuotes.getWrappedListener());
		}

	}

	private static Set<SlashCommand> collectSlashCommands() {
		return ReflectionUtils.getFieldsAnnotatedWith(RegisterSlashCommand.class).stream().filter(field -> {
			field.setAccessible(true);
			try {
				return field.get(null) instanceof SlashCommand && isEnabled(field);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
			return false;
		}).map(field -> {
			try {
				return (SlashCommand) field.get(null);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
			return null;
		}).collect(Collectors.toSet());
	}

	private static boolean isEnabled(Field field) {
		RegisterSlashCommand ann = field.getAnnotation(RegisterSlashCommand.class);
		if (ann.enabled().isEmpty()) { return true; }
		try {
			Object obj = field.getClass().getField(ann.enabled()).get(null);
			if (obj instanceof Boolean bool) { return bool; }
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {}
		return true;
	}

	private static Set<Command> collectPrefixCommands() {
		return ReflectionUtils.getFieldsAnnotatedWith(RegisterCommand.class).stream().filter(field -> {
			field.setAccessible(true);
			try {
				return field.get(null) instanceof Command;
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
			return false;
		}).map(field -> {
			try {
				return (Command) field.get(null);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
			return null;
		}).collect(Collectors.toSet());
	}

	/**
	 * Upserts a slash command.
	 *
	 * @param cmd the command
	 */
	public void upsertCommand(final SlashCommand cmd) {
		if (cmd.isGuildOnly()) {
			var guild = MatyBot.instance.getBot().getGuildById(MatyBot.config().getGuildID());
			if (guild == null) { throw new NullPointerException("No Guild found!"); }

			try {
				guild.upsertCommand(cmd.buildCommandData())
						.queue(cmd1 -> cmd1.updatePrivileges(guild, cmd.buildPrivileges(commandClient)).queue());
			} catch (Exception e) {
				MatyBot.LOGGER.error("Error while upserting guild command!", e);
			}
		} else {
			MatyBot.instance.getBot().upsertCommand(cmd.buildCommandData()).queue();
		}
	}

	public static void clearCommands(final @Nonnull Guild guild, final Runnable... after) {
		guild.retrieveCommands().queue(cmds -> {
			for (int i = 0; i < cmds.size(); i++) {
				try {
					guild.deleteCommandById(cmds.get(i).getIdLong()).submit().get();
				} catch (InterruptedException | ExecutionException e) {}
			}
			for (var toRun : after) {
				toRun.run();
			}
		});
	}

}
