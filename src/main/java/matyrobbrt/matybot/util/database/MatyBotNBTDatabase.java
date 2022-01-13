package matyrobbrt.matybot.util.database;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import com.matyrobbrt.javanbt.db.NBTDatabase;
import com.matyrobbrt.javanbt.nbt.CompoundNBT;
import com.matyrobbrt.javanbt.serialization.NBTSerializable;
import com.matyrobbrt.javanbt.util.NBTManager;

public class MatyBotNBTDatabase extends NBTDatabase {

	public MatyBotNBTDatabase(File file) {
		super(file, 15 * 60 * 1000L);
		if (!file.exists()) {
			try {
				Files.createFile(file.toPath());
			} catch (IOException e) {}
		}
	}

	private final NBTManager nbtManager = new NBTManager();

	private List<Long> guilds = new ArrayList<>();

	public List<Long> getGuilds() { return guilds; }

	@Override
	public void load(CompoundNBT nbt) {
		guilds.clear();
		for (var guild : nbt.getLongArray("Guilds")) {
			guilds.add(guild);
		}

		nbtManager.deserializeNBT(nbt);
	}

	@Override
	public CompoundNBT save(CompoundNBT nbt) {
		nbt.entriesModifiable().clear();
		nbt = nbt.merge(nbtManager.serializeNBT());
		nbt.putLongArray("Guilds", guilds);
		return nbt;
	}

	private <T extends NBTSerializable<?>> T createAndTrack(String key, T value) {
		nbtManager.track(key, value);
		return value;
	}

}
