package matyrobbrt.matybot.api.config;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;

import org.apache.commons.lang3.reflect.FieldUtils;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.conversion.Converter;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.file.FileNotFoundAction;
import com.electronwill.nightconfig.core.file.FileWatcher;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.google.common.io.Files;

import matyrobbrt.matybot.MatyBot;
import matyrobbrt.matybot.api.config.annotation.ConfigEntry;
import matyrobbrt.matybot.util.BotUtils;

public abstract class Config {

	/**
	 * The Config.
	 */
	protected final CommentedFileConfig config;

	/**
	 * If the config is newly generated
	 */
	protected boolean newlyGenerated;

	/**
	 * The default config
	 */
	protected final Object defaultConfig;

	/**
	 * @deprecated <b>THIS CONSTRUCTOR SHOULD NOT BE USED USUALLY</b> <br>
	 *             It is only used by default config objects as they do not need any
	 *             other data, nor do they need to actually exist
	 */
	@Deprecated(forRemoval = false)
	protected Config() {
		this.defaultConfig = null;
		this.config = null;
		this.newlyGenerated = false;
	}

	/**
	 * Instantiates a new config.
	 *
	 * @param configFile    the config file
	 * @param defaultConfig the default config
	 */
	protected Config(final Path configFile, final Object defaultConfig) {
		this(configFile, TomlFormat.instance(), defaultConfig);
	}

	/**
	 * Instantiates a new Bot config.
	 *
	 * @param configFile    the config file
	 * @param configFormat  the config format
	 * @param defaultConfig the default config
	 */
	protected Config(final Path configFile, final ConfigFormat<? extends CommentedConfig> configFormat,
			final Object defaultConfig) {
		this.newlyGenerated = false;
		this.defaultConfig = defaultConfig;
		this.config = CommentedFileConfig.builder(configFile, configFormat).onFileNotFound((file, format) -> {
			this.newlyGenerated = true;
			var toRet = FileNotFoundAction.CREATE_EMPTY.run(configFile, configFormat);
			Utils.createDefault(configFile, getClass(), defaultConfig, this::addExtraData);
			return toRet;
		}).preserveInsertionOrder().build();
		System.out.println(FieldUtils.getFieldsWithAnnotation(getClass(), ConfigEntry.class).length);
		loadData();
		Utils.addNotExistingEntries(config, getClass(), defaultConfig, this::addExtraData);
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

	public void addExtraData() {

	}

	private void loadData() {
		config.load();
		FieldUtils.getFieldsListWithAnnotation(getClass(), ConfigEntry.class).forEach(field -> {
			field.setAccessible(true);
			try {
				Object obj = config.get(Utils.getEntryName(field.getAnnotation(ConfigEntry.class)));
				System.out.println(Utils.convertObject(field, obj));
				field.set(this, Utils.convertObject(field, obj));
			} catch (IllegalArgumentException | IllegalAccessException | NoSuchMethodException | SecurityException
					| InstantiationException | InvocationTargetException e) {
				e.printStackTrace();
			}
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

	static final class Utils {

		private static void createDefault(final Path configFile, final Class<?> clazz, final Object defaultConfig,
				Runnable... beforeSave) {
			try {
				Files.createParentDirs(configFile.toFile());
				java.nio.file.Files.createFile(configFile);
			} catch (IOException e) {}
			final var config = CommentedFileConfig.builder(configFile).build();
			FieldUtils.getFieldsListWithAnnotation(clazz, ConfigEntry.class).forEach(field -> {
				field.setAccessible(true);
				ConfigEntry entry = field.getAnnotation(ConfigEntry.class);
				String entryName = getEntryName(entry);
				try {
					config.set(entryName, getObjectConverted(field, defaultConfig));
				} catch (IllegalArgumentException | IllegalAccessException | NoSuchMethodException | SecurityException
						| InstantiationException | InvocationTargetException e) {}
				String comment = getComment(field, defaultConfig);
				if (!comment.isEmpty()) {
					config.setComment(entryName, comment);
				}
			});
			for (var runnable : beforeSave) {
				runnable.run();
			}
			config.save();
		}

		@SuppressWarnings("unchecked")
		public static Object getObjectConverted(Field field, Object configInstance)
				throws IllegalArgumentException, IllegalAccessException, NoSuchMethodException, SecurityException,
				InstantiationException, InvocationTargetException {
			Object configVal = field.get(configInstance);
			if (field.getAnnotation(ConfigEntry.class).converter() != ConfigEntry.GenericConverter.class) {
				Class<? extends Converter<?, ?>> converterClazz = field.getAnnotation(ConfigEntry.class).converter();
				Constructor<? extends Converter<?, ?>> constr = converterClazz.getConstructor();
				Converter<Object, Object> converter = (Converter<Object, Object>) constr.newInstance();
				return converter.convertFromField(configVal);
			}
			return configVal;
		}

		@SuppressWarnings("unchecked")
		public static Object convertObject(Field field, Object toConvert)
				throws IllegalArgumentException, IllegalAccessException, NoSuchMethodException, SecurityException,
				InstantiationException, InvocationTargetException {
			if (field.getAnnotation(ConfigEntry.class).converter() != ConfigEntry.GenericConverter.class) {
				Class<? extends Converter<?, ?>> converterClazz = field.getAnnotation(ConfigEntry.class).converter();
				Constructor<? extends Converter<?, ?>> constr = converterClazz.getConstructor();
				Converter<Object, Object> converter = (Converter<Object, Object>) constr.newInstance();
				Object toRet = converter.convertToField(toConvert);
				System.out.println(toRet);
				return toRet;
			}
			return toConvert;
		}

		private static void addNotExistingEntries(final CommentedFileConfig config, final Class<?> clazz,
				final Object defaultConfig, Runnable... beforeSave) {
			FieldUtils.getFieldsListWithAnnotation(clazz, ConfigEntry.class).forEach(field -> {
				field.setAccessible(true);
				ConfigEntry entry = field.getAnnotation(ConfigEntry.class);
				String entryName = getEntryName(entry);
				if (!config.contains(entryName)) {
					try {
						config.set(entryName, getObjectConverted(field, defaultConfig));
					} catch (IllegalArgumentException | IllegalAccessException | NoSuchMethodException
							| SecurityException | InstantiationException | InvocationTargetException e) {}
					String comment = getComment(field, defaultConfig);
					if (!comment.isEmpty()) {
						config.setComment(entryName, comment);
					}
				}
			});
			for (var runnable : beforeSave) {
				runnable.run();
			}
			config.save();
		}

		private static String getEntryName(ConfigEntry entry) {
			String category = entry.category().isEmpty() ? "" : entry.category() + ".";
			return category + entry.name();
		}

		private static String getComment(Field field, final Object defaultConfig) {
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
					comment.append("default: " + getObjectConverted(field, defaultConfig));
				} catch (IllegalArgumentException | IllegalAccessException | NoSuchMethodException | SecurityException
						| InstantiationException | InvocationTargetException e) {}
			}
			return comment.toString();
		}
	}

}
