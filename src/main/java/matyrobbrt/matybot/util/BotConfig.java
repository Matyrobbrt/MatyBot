package matyrobbrt.matybot.util;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.io.IOException;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.lang3.reflect.FieldUtils;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.file.FileNotFoundAction;
import com.electronwill.nightconfig.core.file.FileWatcher;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

import matyrobbrt.matybot.MatyBot;
import net.dv8tion.jda.api.entities.Activity.ActivityType;

public final class BotConfig {

	/**
	 * The Config.
	 */
	private final CommentedFileConfig config;

	/**
	 * The Newly generated.
	 */
	private boolean newlyGenerated;

	private static final BotConfig DEFAULT = new BotConfig();

	private BotConfig() {
		this.config = null;
		this.newlyGenerated = false;
	}

	/**
	 * Instantiates a new Bot config.
	 *
	 * @param configFile the config file
	 */
	public BotConfig(final Path configFile) {
		this(configFile, TomlFormat.instance());
	}

	/**
	 * Instantiates a new Bot config.
	 *
	 * @param configFile   the config file
	 * @param configFormat the config format
	 */
	public BotConfig(final Path configFile, final ConfigFormat<? extends CommentedConfig> configFormat) {
		this.newlyGenerated = false;
		this.config = CommentedFileConfig.builder(configFile, configFormat).onFileNotFound((file, format) -> {
			this.newlyGenerated = true;
			var toRet = FileNotFoundAction.CREATE_EMPTY.run(configFile, configFormat);
			Utils.createDefault(configFile);
			return toRet;
		}).preserveInsertionOrder().build();
		loadData();
		Utils.addNotExistingEntries(config);
		try {
			FileWatcher.defaultInstance().addWatch(configFile, () -> {
				MatyBot.LOGGER.info("Config file changed! Updating values...");
				loadData();
			});
		} catch (IOException e) {
			MatyBot.LOGGER.error("Config file cannot be watched! The bot will be stopped!", e);
			System.exit(1);
		}

	}

	private void loadData() {
		config.load();
		FieldUtils.getFieldsListWithAnnotation(getClass(), ConfigEntry.class).forEach(field -> {
			field.setAccessible(true);
			try {
				field.set(this, config.get(Utils.getEntryName(field.getAnnotation(ConfigEntry.class))));
			} catch (IllegalArgumentException | IllegalAccessException e) {}
		});
	}

	/**
	 * Returns whether the configuration file was newly generated (e.g. the bot was
	 * run for the first time).
	 *
	 * @return If the config file was newly generated
	 */
	public boolean isNewlyGenerated() { return newlyGenerated; }

	/**
	 * Returns the raw {@link CommentedFileConfig} object.
	 *
	 * @return The raw config object
	 */
	public CommentedFileConfig getConfig() { return config; }

	/// UTIL STUFF ///

	private static final class Utils {

		private static void createDefault(final Path configFile) {
			try {
				Files.createParentDirs(configFile.toFile());
				java.nio.file.Files.createFile(configFile);
			} catch (IOException e) {}
			final var config = CommentedFileConfig.builder(configFile).build();
			FieldUtils.getFieldsListWithAnnotation(BotConfig.class, ConfigEntry.class).forEach(field -> {
				field.setAccessible(true);
				ConfigEntry entry = field.getAnnotation(ConfigEntry.class);
				String entryName = getEntryName(entry);
				try {
					config.set(entryName, field.get(DEFAULT));
				} catch (IllegalArgumentException | IllegalAccessException e) {}
				String comment = getComment(field);
				if (!comment.isEmpty()) {
					config.setComment(entryName, comment);
				}
			});
			config.setComment("modules.logging",
					"This module controls logging in the logging channel, as well as sticky roles.");
			config.save();
		}

		private static void addNotExistingEntries(final CommentedFileConfig config) {
			FieldUtils.getFieldsListWithAnnotation(BotConfig.class, ConfigEntry.class).forEach(field -> {
				field.setAccessible(true);
				ConfigEntry entry = field.getAnnotation(ConfigEntry.class);
				String entryName = getEntryName(entry);
				if (!config.contains(entryName)) {
					try {
						config.set(entryName, field.get(DEFAULT));
					} catch (IllegalArgumentException | IllegalAccessException e) {}
					String comment = getComment(field);
					if (!comment.isEmpty()) {
						config.setComment(entryName, comment);
					}
				}
			});
			config.save();
		}

		private static String getEntryName(ConfigEntry entry) {
			String category = entry.category().isEmpty() ? "" : entry.category() + ".";
			return category + entry.name();
		}

		private static String getComment(Field field) {
			ConfigEntry entry = field.getAnnotation(ConfigEntry.class);
			StringBuilder comment = new StringBuilder();
			if (entry.comments().length > 0 && !entry.comments()[0].isEmpty()) {
				comment.append(BotUtils.LINE_JOINER.join(entry.comments()));
			}
			if (entry.commentDefaultValue()) {
				if (!comment.isEmpty()) {
					comment.append(System.getProperty("line.separator"));
				}
				try {
					comment.append("default: " + field.get(DEFAULT));
				} catch (IllegalArgumentException | IllegalAccessException e) {}
			}
			return comment.toString();
		}
	}

	@Documented
	@Retention(RUNTIME)
	@Target(ElementType.FIELD)
	private @interface ConfigEntry {

		String name();

		String category() default "";

		String[] comments() default {};

		boolean commentDefaultValue() default true;
	}

	/// GENERAL CONFIG ///

	@ConfigEntry(name = "botOwner", category = "general", comments = "The owner of the bot", commentDefaultValue = false)
	private long botOwner;

	public long getBotOwner() { return botOwner; }

	@ConfigEntry(name = "guildID", category = "general", comments = "The main guild of the bot", commentDefaultValue = false)
	private long guildID;

	public long getGuildID() { return guildID; }

	@ConfigEntry(name = "databaseName", category = "general", comments = "The name of the database")
	private String databaseName = "storage/database.db";

	public String getDatabaseName() { return databaseName; }

	@ConfigEntry(name = "main", category = "general.prefixes", comments = {
			"The main prefix of the bot", "Any other prefix should be an alternative one"
	})
	public String mainPrefix = "!";

	@ConfigEntry(name = "type", category = "general.activity", comments = "The type of activity the bot has.")
	private String activityType = "WATCHING";

	public ActivityType getActivityType() { return ActivityType.valueOf(activityType); }

	@ConfigEntry(name = "name", category = "general.activity", comments = "The name of the activity the bot has.")
	public String activityName = "naughty people!";

	@ConfigEntry(name = "alternative", category = "general.prefixes", comments = "The alternative prefixes of the bot")
	public List<String> alternativePrefixes = Lists.newArrayList();

	/// MODULES ///

	@ConfigEntry(name = "enabled", category = "modules.commands", comments = "If the commands module should be enabled.")
	private boolean commandsModuleEnabled = true;

	public boolean isCommandsModuleEnabled() { return commandsModuleEnabled; }

	@ConfigEntry(name = "enabled", category = "modules.logging", comments = "If the logging module should be enabled.")
	private boolean loggingModuleEnabled = true;

	public boolean isLoggingModuleEnabled() { return loggingModuleEnabled; }

	@ConfigEntry(name = "enabled", category = "modules.levelling", comments = "If the levelling module should be enabled.")
	private boolean levellingModuleEnabled = true;

	public boolean isLevellingModuleEnabled() { return levellingModuleEnabled; }

	/// CHANNELS ///

	@ConfigEntry(name = "logging", category = "channels", comments = "The channel in which logs will be sent", commentDefaultValue = false)
	public long loggingChannel;

	/// ROLES ///

	@ConfigEntry(name = "moderator", category = "roles", comments = "The moderator roles", commentDefaultValue = false)
	public List<Long> moderatorRoles = Lists.newArrayList();

	@ConfigEntry(name = "trick_manager", category = "roles", comments = "The roles that can manage tricks.", commentDefaultValue = false)
	public List<Long> trickManagerRoles = Lists.newArrayList();

	@ConfigEntry(name = "muted", category = "roles", comments = {
			"The role which will be added to muted people.",
			"NOTE: We will switch to using timing out for handling mutes after JDA implements that and discord makes the timing out better."
	}, commentDefaultValue = false)
	public long mutedRole;

}
