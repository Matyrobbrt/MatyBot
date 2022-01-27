package io.github.matyrobbrt.matybot.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import java.util.Timer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.github.matyrobbrt.jdautils.event.BetterEventWaiter;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Constants {

	public static final Path STORAGE_PATH;

	static {
		final Path storagePath = Path.of("storage");
		try {
			Files.createDirectory(storagePath);
		} catch (IOException e) {}
		STORAGE_PATH = storagePath;
	}

	public static final long SECOND_TO_MILLI = 1000L;
	public static final long MINUTE_TO_MILLI = 60000L;
	public static final long HOUR_TO_MILLI = (long) 3.6e+6;
	public static final long DAY_TO_MILLI = (long) 8.64e+7;
	public static final long WEEK_TO_MILLI = (long) 6.048e+8;
	public static final long MONTH_TO_MILLI = (long) 2.628e+9;
	public static final long YEAR_TO_MILLI = (long) 3.154e+10;

	public static final BetterEventWaiter EVENT_WAITER = new BetterEventWaiter();
	public static final Random RANDOM = new Random();
	public static final Timer TIMER = new Timer();
	public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

}
