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
import matyrobbrt.matybot.api.annotation.RegisterCommand;
import matyrobbrt.matybot.api.annotation.RegisterContextMenu;
import matyrobbrt.matybot.api.annotation.RegisterSlashCommand;
import matyrobbrt.matybot.api.command.slash.ContextMenu;
import matyrobbrt.matybot.api.command.slash.GuildSpecificSlashCommand;
import matyrobbrt.matybot.api.event.EventListenerWrapper;
import matyrobbrt.matybot.modules.commands.menu.CreateGistContextMenu;
import matyrobbrt.matybot.modules.levelling.LeaderboardCommand;
import matyrobbrt.matybot.quotes.QuoteCommand;
import matyrobbrt.matybot.tricks.TrickListener;
import matyrobbrt.matybot.util.ReflectionUtils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.hooks.EventListener;
import ssynx.gist.GistUtils;

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
		super(MatyBot.generalConfig()::isCommandsModuleEnabled, bot);

		var builder = new CommandClientBuilder().setOwnerId(MatyBot.generalConfig().getBotOwner()).useHelpBuilder(false)
				.setManualUpsert(true).setPrefix(MatyBot.generalConfig().mainPrefix)
				.setPrefixes(MatyBot.generalConfig().alternativePrefixes.toArray(new String[] {}));

		builder.setPrefixFunction(event -> {
			if (!event.isFromGuild()) { return MatyBot.generalConfig().mainPrefix; }
			return MatyBot.getConfigForGuild(event.getGuild().getIdLong()).prefix;
		});

		collectSlashCommands().stream().filter(Objects::nonNull).forEach(c -> {
			builder.addSlashCommand(c);
			if (c instanceof GuildSpecificSlashCommand guildCmd) {
				guildCmd.buildAndUpsert(bot);
			} else {
				upsertCommand(c);
			}
		});
		collectPrefixCommands().stream().filter(Objects::nonNull).forEach(builder::addCommand);

		collectContextMenus().forEach(menu -> {
			upsertContextMenu(menu);
			bot.addEventListener(new EventListenerWrapper(menu));
		});

		if (GistUtils.hasToken()) {
			final var gistMenu = new CreateGistContextMenu();
			upsertContextMenu(gistMenu);
			bot.addEventListener(new EventListenerWrapper(gistMenu));
		} else {
			MatyBot.LOGGER.warn("A Github token has not been configured! I will not be able to create gists.");
		}

		for (var guild : bot.getGuilds()) {
			bot.addEventListener(new EventListenerWrapper(new TrickListener(guild.getIdLong())));
		}

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
			bot.addEventListener(LeaderboardCommand.getWrappedListener());
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

	private static Set<ContextMenu> collectContextMenus() {
		return ReflectionUtils.getFieldsAnnotatedWith(RegisterContextMenu.class).stream().filter(field -> {
			field.setAccessible(true);
			try {
				return field.get(null) instanceof ContextMenu;
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
			return false;
		}).map(field -> {
			try {
				return (ContextMenu) field.get(null);
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
			for (var guild : bot.getGuilds()) {
				try {
					guild.upsertCommand(cmd.buildCommandData())
							.queue(cmd1 -> cmd1.updatePrivileges(guild, cmd.buildPrivileges(commandClient)).queue());
				} catch (Exception e) {
					MatyBot.LOGGER.error("Error while upserting guild command!", e);
				}
			}
		} else {
			MatyBot.instance.getBot().upsertCommand(cmd.buildCommandData()).queue();
		}
	}

	/**
	 * Upserts a context menu.
	 *
	 * @param menu the menu
	 */
	public static void upsertContextMenu(final ContextMenu menu) {
		if (menu.isGuildOnly()) {
			for (var guild : MatyBot.instance.getBot().getGuilds()) {
				try {
					guild.upsertCommand(menu.buildCommandData())
							.queue(cmd1 -> cmd1.updatePrivileges(guild, menu.buildPrivileges()).queue());
				} catch (Exception e) {
					MatyBot.LOGGER.error("Error while upserting guild context menu!", e);
				}
			}
		} else {
			MatyBot.instance.getBot().upsertCommand(menu.buildCommandData()).queue();
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
