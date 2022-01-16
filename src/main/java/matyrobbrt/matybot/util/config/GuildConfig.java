package matyrobbrt.matybot.util.config;

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
import matyrobbrt.matybot.util.BotUtils;

public final class GuildConfig {

	/**
	 * The Config.
	 */
	private final CommentedFileConfig config;

	/**
	 * The Newly generated.
	 */
	private boolean newlyGenerated;

	private final long guildId;

	private static final GuildConfig DEFAULT = new GuildConfig();

	private GuildConfig() {
		this.config = null;
		this.newlyGenerated = false;
		this.guildId = 0;
	}

	/**
	 * Instantiates a new Bot config.
	 *
	 * @param configFile the config file
	 */
	public GuildConfig(final Path configFile, final long guildId) {
		this(configFile, TomlFormat.instance(), guildId);
	}

	/**
	 * Instantiates a new Bot config.
	 *
	 * @param configFile   the config file
	 * @param configFormat the config format
	 */
	public GuildConfig(final Path configFile, final ConfigFormat<? extends CommentedConfig> configFormat,
			final long guildId) {
		this.newlyGenerated = false;
		this.guildId = guildId;
		this.config = CommentedFileConfig.builder(configFile, configFormat).onFileNotFound((file, format) -> {
			this.newlyGenerated = true;
			var toRet = FileNotFoundAction.CREATE_EMPTY.run(configFile, configFormat);
			Utils.createDefault(configFile, guildId);
			return toRet;
		}).preserveInsertionOrder().build();
		loadData();
		Utils.addNotExistingEntries(config, guildId);
		try {
			FileWatcher.defaultInstance().addWatch(configFile, () -> {
				MatyBot.LOGGER.info("Config file for guild {} changed! Updating values...", guildId);
				loadData();
			});
		} catch (IOException e) {
			MatyBot.LOGGER.error("Config file for guild {} cannot be watched! The bot will be stopped! {}", guildId, e);
			System.exit(1);
		}

	}

	private void loadData() {
		config.load();
		FieldUtils.getFieldsListWithAnnotation(GuildConfig.class, ConfigEntry.class).forEach(field -> {
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

	public long getGuildID() { return guildId; }

	/// UTIL STUFF ///

	private static final class Utils {

		private static void createDefault(final Path configFile, final long guildId) {
			try {
				Files.createParentDirs(configFile.toFile());
				java.nio.file.Files.createFile(configFile);
			} catch (IOException e) {}
			final var config = CommentedFileConfig.builder(configFile).build();
			FieldUtils.getFieldsListWithAnnotation(GuildConfig.class, ConfigEntry.class).forEach(field -> {
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
			addExtraData(config, guildId);
			config.save();
		}

		private static void addNotExistingEntries(final CommentedFileConfig config, final long guildId) {
			FieldUtils.getFieldsListWithAnnotation(GuildConfig.class, ConfigEntry.class).forEach(field -> {
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
			addExtraData(config, guildId);
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

	public static void addExtraData(final CommentedFileConfig config, final long longGuildId) {
		config.add("guild_name", MatyBot.getInstance().getJDA().getGuildById(longGuildId).getName());
		config.setComment("guild_name",
				"""
						This is modified automatically in order to indicate the name of the guild, so it is easier to differentiate between multiple guild configs.
						This value does NOTHING if changed, it is purely visual!""");
	}

	// GENERAL STUFF //

	@ConfigEntry(name = "main", category = "general.prefixes", comments = "The prefix of the bot in the guild.")
	public String prefix = "!";

	/// CHANNELS ///

	@ConfigEntry(name = "logging", category = "channels", comments = "The channel in which logs will be sent", commentDefaultValue = false)
	public long loggingChannel;

	@ConfigEntry(name = "welcome", category = "channels", comments = {
			"The channel in which welcome messages will be sent.", "Supply 0 in order to disable welcome messages."
	}, commentDefaultValue = false)
	public long welcomeChannel;

	/// ROLES ///

	@ConfigEntry(name = "moderator", category = "roles", comments = "The moderator roles", commentDefaultValue = false)
	public List<Long> moderatorRoles = Lists.newArrayList();

	@ConfigEntry(name = "trick_manager", category = "roles", comments = "The roles that can manage tricks.", commentDefaultValue = false)
	public List<Long> trickManagerRoles = Lists.newArrayList();

	@ConfigEntry(name = "muted", category = "roles", comments = {
			"The role which will be added to muted people.",
			"NOTE: We will switch to using timing out for handling mutes after JDA implements that and discord makes the timing out better.",
			"NOTE 2: Timeing out is now an option when muting, which is turned on by default."
	}, commentDefaultValue = false)
	public long mutedRole;

	@ConfigEntry(name = "join", category = "roles", comments = "The roles which will to members when they join.", commentDefaultValue = false)
	public List<Long> joinRoles = Lists.newArrayList();

	@ConfigEntry(name = "enabled", category = "levels", comments = "If the levelling should be enabled in this guild.")
	private boolean levellingEnabled = true;

	public boolean isLevellingEnabled() { return levellingEnabled; }

	@ConfigEntry(name = "multiplier", category = "levels", comments = {
			"The multiplier applied to the amount of XP needed per level.",
			"The formula for calculating the multiplier is:", "xpForLevel = (multiplier * level ^ 2 + 15 * multiplier)"
	})
	public int xpMultiplier = 8;

	public long getRoleForLevel(final int level) {
		return config.getLongOrElse("levels.level_roles." + level, () -> 0);
	}

}
