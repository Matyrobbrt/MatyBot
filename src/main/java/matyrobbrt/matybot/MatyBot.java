package matyrobbrt.matybot;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.util.Locale;

import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import matyrobbrt.matybot.api.annotation.EventSubscriber;
import matyrobbrt.matybot.api.modules.ModuleManager;
import matyrobbrt.matybot.modules.commands.CommandsModule;
import matyrobbrt.matybot.modules.levelling.LevellingModule;
import matyrobbrt.matybot.modules.logging.LoggingModule;
import matyrobbrt.matybot.modules.rolepanel.RolePanelsModule;
import matyrobbrt.matybot.util.BotConfig;
import matyrobbrt.matybot.util.BotUtils;
import matyrobbrt.matybot.util.Emotes;
import matyrobbrt.matybot.util.ReflectionUtils;
import matyrobbrt.matybot.util.database.DatabaseManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class MatyBot {

	public static final Logger LOGGER = LoggerFactory.getLogger("MatyBot");

	public static MatyBot instance;
	private static DatabaseManager database;
	private static BotConfig config;
	private static ModuleManager moduleManager;

	public static void main(String[] args) {
		config = new BotConfig(Paths.get("config.toml"));
		instance = create(BotUtils.getBotToken());
		Emotes.register();
		database = DatabaseManager.connectSQLite("jdbc:sqlite:" + config().getDatabaseName());

		final JDA bot = instance.getBot();

		moduleManager = new ModuleManager(bot);

		moduleManager.addModule(new LevellingModule(bot));
		moduleManager.addModule(CommandsModule.setUpInstance(bot));
		moduleManager.addModule(new LoggingModule(bot));
		moduleManager.addModule(new RolePanelsModule(bot));

		moduleManager.register();
	}

	public static Jdbi database() {
		return database.jdbi();
	}

	public static BotConfig config() {
		return config;
	}

	private final JDA bot;

	private MatyBot(final JDA bot) {
		this.bot = bot;

		if (config().isNewlyGenerated()) {
			LOGGER.warn("A new config file has been generated! Please configure it.");
			System.exit(0);
		}

		bot.setEventManager(new AnnotatedEventManager());
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
		LOGGER.info("I am ready to start. Logged in as " + bot.getSelfUser().getAsTag());
	}

	public JDA getBot() { return bot; }

	private static MatyBot create(final String token) {
		try {
			return new MatyBot(JDABuilder.createDefault(token)
					.enableIntents(GatewayIntent.DIRECT_MESSAGE_REACTIONS, GatewayIntent.values())
					.setMemberCachePolicy(MemberCachePolicy.ALL).enableCache(CacheFlag.ONLINE_STATUS, CacheFlag.EMOTE)
					.setChunkingFilter(ChunkingFilter.ALL)
					.setActivity(Activity.of(config().getActivityType(), config().activityName)).build().awaitReady());
		} catch (final Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

}
