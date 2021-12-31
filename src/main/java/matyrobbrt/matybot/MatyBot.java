package matyrobbrt.matybot;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.util.Locale;

import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import matyrobbrt.matybot.annotation.EventSubscriber;
import matyrobbrt.matybot.modules.commands.CommandsModule;
import matyrobbrt.matybot.modules.logging.LoggingModule;
import matyrobbrt.matybot.modules.rolepanel.RolePanelsModule;
import matyrobbrt.matybot.util.BotConfig;
import matyrobbrt.matybot.util.BotUtils;
import matyrobbrt.matybot.util.ReflectionUtils;
import matyrobbrt.matybot.util.database.DatabaseManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
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

	public static void main(String[] args) {
		config = new BotConfig(Paths.get("config.toml"));
		instance = create(BotUtils.getBotToken());
		database = DatabaseManager.connectSQLite("jdbc:sqlite:" + config().getDatabaseName());

		CommandsModule.setupCommandModule();
		LoggingModule.setupLoggingModule();
		RolePanelsModule.setupRolePanelsModule();
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
					.setMemberCachePolicy(MemberCachePolicy.ALL).enableCache(CacheFlag.ONLINE_STATUS)
					.setChunkingFilter(ChunkingFilter.ALL).build().awaitReady());
		} catch (final Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

}
