package io.github.matyrobbrt.matybot;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.matyrobbrt.jdautils.JDABot;
import io.github.matyrobbrt.jdautils.event.EventListenerWrapper;
import io.github.matyrobbrt.matybot.api.annotation.EventSubscriber;
import io.github.matyrobbrt.matybot.event.EmoteReactionEventHandler;
import io.github.matyrobbrt.matybot.event.MiscEvents;
import io.github.matyrobbrt.matybot.event.QuotingListener;
import io.github.matyrobbrt.matybot.modules.commands.CommandsModule;
import io.github.matyrobbrt.matybot.modules.levelling.LevellingModule;
import io.github.matyrobbrt.matybot.modules.logging.LoggingModule;
import io.github.matyrobbrt.matybot.modules.rolepanel.RolePanelsModule;
import io.github.matyrobbrt.matybot.modules.suggestions.SuggestionsModule;
import io.github.matyrobbrt.matybot.util.BotUtils;
import io.github.matyrobbrt.matybot.util.Constants;
import io.github.matyrobbrt.matybot.util.Emotes;
import io.github.matyrobbrt.matybot.util.ReflectionUtils;
import io.github.matyrobbrt.matybot.util.config.GeneralConfig;
import io.github.matyrobbrt.matybot.util.config.GuildConfig;
import io.github.matyrobbrt.matybot.util.database.DatabaseManager;
import io.github.matyrobbrt.matybot.util.database.MatyBotNBTDatabase;
import io.github.matyrobbrt.matybot.util.database.MatyBotNBTDatabase.Manager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.AllowedMentions;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class MatyBot extends JDABot {

	public static final Logger LOGGER = LoggerFactory.getLogger("MatyBot");

	private static MatyBot instance;
	private static DatabaseManager database;
	private static GeneralConfig generalConfig;
	private static final ConcurrentHashMap<Long, GuildConfig> GUILD_CONFIGS = new ConcurrentHashMap<>();
	public static final Manager NBT_DATABASE_MANAGER = new Manager();

	public static void main(String[] args) {
		if (instance == null) {
			LOGGER.warn(
					"""
							<--------------------------------------------------------------------------------------------------------------------------------------------->
							<-------------------------------------------------------- A new startup has happened! -------------------------------------------------------->
							<--------------------------------------------------------------------------------------------------------------------------------------------->""");
		}

		try {
			generateFolders();
		} catch (IOException e) {}

		AllowedMentions.setDefaultMentionRepliedUser(false);
		generalConfig = new GeneralConfig(Paths.get("configs/general.toml"));
		instance = create(BotUtils.getBotToken());
		Emotes.register();
		database = new DatabaseManager(
				DatabaseManager.connectSQLite("jdbc:sqlite:" + generalConfig().getDatabaseName()), NBT_DATABASE_MANAGER
						.computeIfAbsent(MatyBotNBTDatabase::new, new File(generalConfig().getNBTDatabaseName())));

		Runtime.getRuntime().addShutdownHook(new Thread(NBT_DATABASE_MANAGER::setDirtyAndSave, "DatabaseSaver"));

		// The D4J version is for logging

		try {
			final var clazz = Class.forName("io.github.matyrobbrt.matybot.d4j.D4JBridgeImpl");
			final var invokeMethod = clazz.getMethod("setupInstance");
			invokeMethod.invoke(null);
		} catch (Exception e) {}

		if (D4JBridge.instance == null) {
			LOGGER.warn("The D4J Bridge could not be found! Some features might not work as expected");
		}

		D4JBridge.executeOnInstance(bridge -> bridge.executeMain(args));

		final var bot = getInstance();
		bot.addEventListener(new EventListenerWrapper(Constants.EVENT_WAITER),
				new EventListenerWrapper(new QuotingListener()), wrap(new EmoteReactionEventHandler()));

		final var moduleManager = bot.getModuleManager();

		moduleManager.addModule(new LevellingModule(bot));
		moduleManager.addModule(CommandsModule.setUpInstance(bot));
		moduleManager.addModule(new LoggingModule(bot));
		moduleManager.addModule(new RolePanelsModule(bot));
		moduleManager.addModule(new SuggestionsModule(bot));

		moduleManager.register();

		nbtDatabase().getGuildCache().clear();
		nbtDatabase().getGuildCache().addAll(bot.getGuilds().stream().map(Guild::getIdLong).toList());
		nbtDatabase().setDirty(true);
		nbtDatabase().saveToDisk();
	}

	private static EventListenerWrapper wrap(EventListener listener) {
		return new EventListenerWrapper(listener);
	}

	public static Jdbi database() {
		return database.jdbi();
	}

	public static MatyBotNBTDatabase nbtDatabase() {
		return database.getNbtDatabase();
	}

	public static GeneralConfig generalConfig() {
		return generalConfig;
	}

	public static GuildConfig getConfigForGuild(final long guildId) {
		return GUILD_CONFIGS.computeIfAbsent(guildId,
				k -> new GuildConfig(Paths.get("configs/server/" + guildId + ".toml"), guildId));
	}

	public static GuildConfig getConfigForGuild(final Guild guild) {
		return getConfigForGuild(guild.getIdLong());
	}

	public static GuildConfig getConfigForGuild(final GenericGuildEvent guildEvent) {
		return getConfigForGuild(guildEvent.getGuild());
	}

	private static void generateFolders() throws IOException {
		Files.createDirectories(Paths.get("configs"));
		Files.createDirectories(Paths.get("configs/server"));
		Files.createDirectories(Paths.get("storage/backup"));
	}

	private MatyBot(final JDA bot) {
		super(bot, LOGGER);

		if (generalConfig().isNewlyGenerated()) {
			LOGGER.warn("A new config file has been generated! Please configure it.");
			System.exit(0);
		}

		ReflectionUtils.getClassesAnnotatedWith(EventSubscriber.class).forEach(clazz -> {
			EventSubscriber ann = clazz.getAnnotation(EventSubscriber.class);
			if (ann.createInstance()) {
				try {
					Constructor<?> constructor = clazz.getConstructor();
					constructor.setAccessible(true);
					bot.addEventListener(constructor.newInstance());
				} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
						| IllegalArgumentException | InvocationTargetException e) {
					LOGGER.warn(e.getLocalizedMessage());
				}
			} else {
				bot.addEventListener(clazz);
			}
		});
		Locale.setDefault(Locale.UK);
	}

	private static MatyBot create(final String token) {
		try {
			return new MatyBot(JDABuilder.createDefault(token)
					.enableIntents(GatewayIntent.DIRECT_MESSAGE_REACTIONS, GatewayIntent.values())
					.setMemberCachePolicy(MemberCachePolicy.ALL).enableCache(CacheFlag.ONLINE_STATUS, CacheFlag.EMOTE)
					.setChunkingFilter(ChunkingFilter.ALL).setEventManager(new AnnotatedEventManager())
					.addEventListeners(new EventListenerWrapper(new MiscEvents()))
					.setActivity(Activity.of(generalConfig().getActivityType(), generalConfig().activityName)).build()
					.awaitReady());
		} catch (final Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * @return the instance
	 */
	public static MatyBot getInstance() {
		return instance;
	}

}
