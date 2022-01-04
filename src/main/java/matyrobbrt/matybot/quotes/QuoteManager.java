package matyrobbrt.matybot.quotes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.electronwill.nightconfig.core.file.FileWatcher;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import matyrobbrt.matybot.MatyBot;
import matyrobbrt.matybot.util.Constants;
import net.dv8tion.jda.api.entities.Guild;

/**
 * TODO move to a database
 * 
 * @author matyrobbrt
 *
 */
public class QuoteManager {

	private static final File QUOTES_FILE = Constants.STORAGE_PATH.resolve("quotes.json").toFile();

	private static final Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

	private static @Nullable Map<Long, List<Quote>> quotes = null;

	public static List<Quote> getQuotesForGuild(long guidId) {
		if (quotes == null) {
			loadQuotes();
		}
		return quotes.computeIfAbsent(guidId, i -> new ArrayList<>());
	}

	public static List<Quote> getQuotesForGuild(final Guild guild) {
		return getQuotesForGuild(guild.getIdLong());
	}

	public static Map<Long, List<Quote>> getAllQuotes() {
		return quotes;
	}

	public static void addQuote(Quote quote, long guildId) {
		getQuotesForGuild(guildId).add(quote);
		write();
	}

	public static Quote removeQuote(int index, long guildId) {
		var quotesForGuild = getQuotesForGuild(guildId);
		if (index < quotesForGuild.size()) {
			var toRet = quotesForGuild.remove(index);
			write();
			return toRet;
		}
		return null;
	}

	public static void loadQuotes() {
		if (!QUOTES_FILE.exists()) {
			quotes = new HashMap<>();
		}
		try (InputStreamReader reader = new InputStreamReader(new FileInputStream(QUOTES_FILE),
				StandardCharsets.UTF_8)) {
			Type typeOfList = new TypeToken<Map<Long, List<Quote>>>() {}.getType();
			quotes = GSON.fromJson(reader, typeOfList);
		} catch (final IOException exception) {
			MatyBot.LOGGER.trace("Failed to read quotes file...", exception);
			quotes = new HashMap<>();
		}
	}

	static {
		try {
			FileWatcher.defaultInstance().addWatch(QUOTES_FILE.toPath(), QuoteManager::loadQuotes);
		} catch (IOException e) {
			MatyBot.LOGGER.error("Exception while setting up the quotes file watcher! The bot will be shut down.", e);
			System.exit(1);
		}
	}

	private static void write() {
		var quotes = getAllQuotes();
		try (var writer = new OutputStreamWriter(new FileOutputStream(QUOTES_FILE), StandardCharsets.UTF_8)) {
			GSON.toJson(quotes, writer);
		} catch (final FileNotFoundException exception) {
			MatyBot.LOGGER.error("An FileNotFoundException occurred saving quotes...", exception);
		} catch (final IOException exception) {
			MatyBot.LOGGER.error("An IOException occurred saving quotes...", exception);
		}
	}

}
