package matyrobbrt.matybot;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.matyrobbrt.javanbt.db.NBTDatabaseManager;
import matyrobbrt.matybot.api.annotation.EventSubscriber;
import matyrobbrt.matybot.api.event.EventListenerWrapper;
import matyrobbrt.matybot.api.modules.ModuleManager;
import matyrobbrt.matybot.event.MiscEvents;
import matyrobbrt.matybot.event.QuotingListener;
import matyrobbrt.matybot.modules.commands.CommandsModule;
import matyrobbrt.matybot.modules.levelling.LevellingModule;
import matyrobbrt.matybot.modules.logging.LoggingModule;
import matyrobbrt.matybot.modules.rolepanel.RolePanelsModule;
import matyrobbrt.matybot.util.BotUtils;
import matyrobbrt.matybot.util.Constants;
import matyrobbrt.matybot.util.Emotes;
import matyrobbrt.matybot.util.ReflectionUtils;
import matyrobbrt.matybot.util.config.GeneralConfig;
import matyrobbrt.matybot.util.config.GuildConfig;
import matyrobbrt.matybot.util.database.DatabaseManager;
import matyrobbrt.matybot.util.database.MatyBotNBTDatabase;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.AllowedMentions;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class MatyBot {

	public static final Logger LOGGER = LoggerFactory.getLogger("MatyBot");

	public static MatyBot instance;
	private static DatabaseManager database;
	private static GeneralConfig generalConfig;
	private static ModuleManager moduleManager;
	private static final Map<Long, GuildConfig> GUILD_CONFIGS = Collections.synchronizedMap(new HashMap<>());

	public static void main(String[] args) {
		try {
			generateFolders();
		} catch (IOException e) {}

		AllowedMentions.setDefaultMentionRepliedUser(false);
		generalConfig = new GeneralConfig(Paths.get("configs/general.toml"));
		instance = create(BotUtils.getBotToken());
		Emotes.register();
		database = new DatabaseManager(
				DatabaseManager.connectSQLite("jdbc:sqlite:" + generalConfig().getDatabaseName()),
				NBTDatabaseManager.DEFAULT.computeIfAbsent(MatyBotNBTDatabase::new,
						new File(generalConfig().getNBTDatabaseName())));

		final JDA bot = instance.getBot();
		bot.addEventListener(new EventListenerWrapper(Constants.EVENT_WAITER),
				new EventListenerWrapper(new QuotingListener()));

		moduleManager = new ModuleManager(bot);

		moduleManager.addModule(new LevellingModule(bot));
		moduleManager.addModule(CommandsModule.setUpInstance(bot));
		moduleManager.addModule(new LoggingModule(bot));
		moduleManager.addModule(new RolePanelsModule(bot));

		moduleManager.register();

		nbtDatabase().getGuildCache().clear();
		nbtDatabase().getGuildCache().addAll(bot.getGuilds().stream().map(Guild::getIdLong).toList());
		nbtDatabase().setDirty(true);
		nbtDatabase().saveToDisk();
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

	public static JDA getJDA() { return instance.getBot(); }

	private static void generateFolders() throws IOException {
		Files.createDirectories(Paths.get("configs"));
		Files.createDirectories(Paths.get("configs/server"));
	}

	private final JDA bot;

	private MatyBot(final JDA bot) {
		this.bot = bot;

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

	public JDA getBot() { return bot; }

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

}
