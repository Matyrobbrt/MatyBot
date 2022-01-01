package matyrobbrt.matybot.modules.rolepanel;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import com.electronwill.nightconfig.core.file.FileWatcher;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import matyrobbrt.matybot.MatyBot;

public class RolePanelsModule {

	public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting()
			.excludeFieldsWithoutExposeAnnotation().create();

	private static final File PANELS_FILE = new File("storage/role_panels.json");

	static {
		if (!PANELS_FILE.exists()) {
			try {
				Files.createFile(PANELS_FILE.toPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try {
			FileWatcher.defaultInstance().addWatch(PANELS_FILE, () -> {
				MatyBot.LOGGER.info("Role panels file has been changed! Updating...");
				loadPanels();
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static final List<RolePanel> PANELS = new ArrayList<>();

	public static void setupRolePanelsModule() {
		loadPanels();

		MatyBot.instance.getBot().addEventListener(new RolePanelHandler());
		MatyBot.LOGGER.warn("Role Panels module enabled and loaded.");
	}

	public static RolePanel getPanelForChannelAndMessage(long channelId, long messageId) {
		for (var panel : PANELS) {
			if (panel.channelID == channelId && panel.messageID == messageId) { return panel; }
		}
		return null;
	}

	private static void loadPanels() {
		try (var reader = new FileReader(PANELS_FILE)) {
			PANELS.clear();
			JsonArray array = GSON.fromJson(reader, JsonArray.class);
			for (JsonElement element : array) {
				PANELS.add(GSON.fromJson(element, RolePanel.class));
			}
		} catch (IOException e) {
			MatyBot.LOGGER.error("Error while parsing in the role panels!");
		}
	}

}
