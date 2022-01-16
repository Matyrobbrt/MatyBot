package matyrobbrt.matybot.tricks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import matyrobbrt.matybot.MatyBot;
import matyrobbrt.matybot.tricks.ITrick.TrickType;

/**
 * TODO: Migrate to a the database
 */
public final class TrickManager {

	private static final Map<String, TrickType<?>> TRICK_TYPES = new HashMap<>();

	private static final List<ITrick> GLOBAL_TRICKS = new ArrayList<>();

	static {
		TrickManager.registerTrickType("string", StringTrick.TYPE);
		TrickManager.registerTrickType("embed", EmbedTrick.TYPE);
	}

	public static Optional<ITrick> getTrick(final long guildId, final String name) {
		// return getTricksForGuild(guildId).stream().filter(trick ->
		// trick.getNames().contains(name)).findAny();
		return MatyBot.nbtDatabase().getDataForGuild(guildId).getTricks().stream()
				.filter(trick -> trick.getNames().contains(name)).findAny()
				.or(() -> GLOBAL_TRICKS.stream().filter(trick -> trick.getNames().contains(name)).findAny());
	}

	public static List<ITrick> getTricksForGuild(long guildId) {
		return MatyBot.nbtDatabase().getDataForGuild(guildId).getTricks();
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
	public static Map<String, ITrick.TrickType<?>> getTrickTypes() {
		return new HashMap<>(TRICK_TYPES);
	}

	/**
	 * Gets a trick type by name.
	 *
	 * @param  name the name
	 * @return      the trick type, or null if no such type exists
	 */
	public static @Nullable ITrick.TrickType<?> getTrickType(final String name) {
		return TRICK_TYPES.get(name);
	}

	public static @Nullable String getTrickTypeName(final TrickType<?> type) {
		return TRICK_TYPES.entrySet().stream().filter(e -> e.getValue() == type).map(Entry::getKey).findFirst()
				.orElse(null);
	}

	/**
	 * Adds a trick.
	 *
	 * @param trick the trick to add.
	 */
	public static void addTrick(final long guildId, final ITrick trick) {
		getTricksForGuild(guildId).add(trick);
		MatyBot.nbtDatabase().setDirtyAndSave();
		// addOrRestoreCommand(trick, guildId);
	}

	/**
	 * Removes a trick.
	 *
	 * @param trick the trick
	 */
	public static void removeTrick(final long guildId, final ITrick trick) {
		getTricksForGuild(guildId).remove(trick);
		MatyBot.nbtDatabase().setDirtyAndSave();
		// CommandsModule.getInstance().getCommandClient().removeCommand(trick.getNames().get(0));
	}

	/*
	 * public static List<RunTrickCommand> createTrickCommands(final long guildId) {
	 * return getTricksForGuild(guildId).stream().map(trick ->
	 * RunTrickCommand.createGuild(trick, guildId)).toList(); }
	 */

	/**
	 * Adds or restores the command for a trick.
	 *
	 * @param trick the trick
	 */
	/*
	 * private static void addOrRestoreCommand(final ITrick trick, final long
	 * guildId) {
	 * CommandsModule.getInstance().getCommandClient().addCommand(RunTrickCommand.
	 * createGuild(trick, guildId)); }
	 */

}
