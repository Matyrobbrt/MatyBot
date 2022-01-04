package matyrobbrt.matybot.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Constants {

	public static final Path STORAGE_PATH;

	static {
		final Path storagePath = Path.of("storage");
		try {
			Files.createDirectory(storagePath);
		} catch (IOException e) {}
		STORAGE_PATH = storagePath;
	}

}
