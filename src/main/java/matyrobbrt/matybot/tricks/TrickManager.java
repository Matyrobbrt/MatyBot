package matyrobbrt.matybot.tricks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.electronwill.nightconfig.core.file.FileWatcher;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import matyrobbrt.matybot.MatyBot;
import matyrobbrt.matybot.modules.commands.CommandsModule;
import matyrobbrt.matybot.tricks.ITrick.TrickType;
import matyrobbrt.matybot.tricks.commands.RunTrickCommand;

/**
 * TODO: Migrate to a the database
 */
public final class TrickManager {

	private static final String TRICK_STORAGE_PATH = "storage/tricks.json";

	private static final Gson GSON;

	private static final Map<String, ITrick.TrickType<?>> TRICK_TYPES = new HashMap<>();

	private static @Nullable Map<Long, List<ITrick>> tricks = null;

	public static Map<Long, List<ITrick>> getAllTricks() {
		if (tricks == null) {
			loadTricks();
		}
		return tricks;
	}

	public static Optional<ITrick> getTrick(final long guildId, final String name) {
		return getTricksForGuild(guildId).stream().filter(trick -> trick.getNames().contains(name)).findAny();
	}

	public static List<ITrick> getTricksForGuild(long guildId) {
		if (tricks == null) {
			loadTricks();
		}
		return tricks.computeIfAbsent(guildId, g -> new ArrayList<>());
	}

	public static void loadTricks() {
		final File file = new File(TRICK_STORAGE_PATH);
		if (!file.exists()) {
			tricks = new HashMap<>();
		}
		try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
			Type typeOfList = new TypeToken<List<ITrick>>() {}.getType();
			JsonObject json = GSON.fromJson(reader, JsonObject.class);
			if (json == null) {
				json = new JsonObject();
			}
			tricks = new HashMap<>();
			json.entrySet().forEach(entry -> {
				tricks.put(Long.parseLong(entry.getKey()), GSON.fromJson(entry.getValue(), typeOfList));
			});
		} catch (final IOException exception) {
			MatyBot.LOGGER.trace("Failed to read tricks file...", exception);
			tricks = new HashMap<>();
		}
	}

	static {
		try {
			FileWatcher.defaultInstance().addWatch(Paths.get(TRICK_STORAGE_PATH), TrickManager::loadTricks);
		} catch (IOException e) {
			MatyBot.LOGGER.error("Exception while setting up the tricks file watcher! The bot will be shut down.", e);
			System.exit(1);
		}

		registerTrickType("string", new StringTrick.Type());
		registerTrickType("embed", new EmbedTrick.Type());
	}

	/**
	 * Register a new {@link TrickType}.
	 *
	 * @param name the name to register the type under
	 * @param type the type
	 */
	public static void registerTrickType(final String name, final ITrick.TrickType<?> type) {
		TRICK_TYPES.put(name, type);
	}

	/**
	 * Gets all trick types.
	 *
	 * @return a map where the values are the trick types and the keys are their
	 *         names
	 */
	public static Map<String, ITrick.TrickType<?>> getTrickTypes() { return new HashMap<>(TRICK_TYPES); }

	/**
	 * Gets a trick type by name.
	 *
	 * @param name the name
	 * @return the trick type, or null if no such type exists
	 */
	public static @Nullable ITrick.TrickType<?> getTrickType(final String name) {
		return TRICK_TYPES.get(name);
	}

	/**
	 * Adds a trick.
	 *
	 * @param iTrick the trick to add.
	 */
	public static void addTrick(final long guildId, final ITrick iTrick) {
		getTricksForGuild(guildId).add(iTrick);
		write();
		addOrRestoreCommand(iTrick);
	}

	/**
	 * Removes a trick.
	 *
	 * @param trick the trick
	 */
	public static void removeTrick(final long guildId, final ITrick trick) {
		getTricksForGuild(guildId).remove(trick);
		write();
		CommandsModule.getInstance().getCommandClient().removeCommand(trick.getNames().get(0));
	}

	/**
	 * Write tricks to disk.
	 */
	private static void write() {
		final var tricksFile = new File(TRICK_STORAGE_PATH);
		var tricks = getAllTricks();
		try (var writer = new OutputStreamWriter(new FileOutputStream(tricksFile), StandardCharsets.UTF_8)) {
			JsonObject json = new JsonObject();
			for (var trick : tricks.entrySet()) {
				json.add(String.valueOf(trick.getValue()),
						GSON.fromJson(GSON.toJson(trick.getValue()), JsonArray.class));
			}
			GSON.toJson(tricks, writer);
		} catch (final FileNotFoundException exception) {
			MatyBot.LOGGER.error("An FileNotFoundException occurred saving tricks...", exception);
		} catch (final IOException exception) {
			MatyBot.LOGGER.error("An IOException occurred saving tricks...", exception);
		}
	}

	public static List<RunTrickCommand> createTrickCommands(final long guildId) {
		return getTricksForGuild(guildId).stream().map(RunTrickCommand::new).toList();
	}

	/**
	 * Adds or restores the command for a trick.
	 *
	 * @param trick the trick
	 */
	private static void addOrRestoreCommand(final ITrick trick) {
		CommandsModule.getInstance().getCommandClient().addCommand(new RunTrickCommand(trick));
	}

	static {
		TrickManager.registerTrickType("string", new StringTrick.Type());
		TrickManager.registerTrickType("embed", new EmbedTrick.Type());

		GSON = new GsonBuilder().registerTypeAdapterFactory(new TrickSerializer()).create();
	}

	/**
	 * Handles serializing tricks to JSON.
	 */
	static final class TrickSerializer implements TypeAdapterFactory {

		/**
		 * Create type adapter.
		 *
		 * @param <T>  the type parameter
		 * @param gson the gson
		 * @param type the type
		 * @return the type adapter
		 */
		@Override
		public <T> TypeAdapter<T> create(final Gson gson, final TypeToken<T> type) {
			if (!ITrick.class.isAssignableFrom(type.getRawType())) { return null; }
			final TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);
			return new TypeAdapter<>() {

				@Override
				public void write(final JsonWriter out, final T value) throws IOException {
					out.beginObject().name("$type").value(type.toString()).name("value");
					delegate.write(out, value);
					out.endObject();
				}

				@Override
				public T read(final JsonReader in) throws IOException {
					in.beginObject();
					if (!"$type".equals(in.nextName())) { return null; }
					try {
						@SuppressWarnings("unchecked")
						Class<T> clazz = (Class<T>) Class.forName(in.nextString());
						TypeToken<T> readType = TypeToken.get(clazz);
						in.nextName();
						var result = gson.getDelegateAdapter(TrickSerializer.this, readType).read(in);
						in.endObject();
						return result;
					} catch (ClassNotFoundException e) {
						throw new RuntimeException(e);
					}
				}
			};
		}
	}
}
