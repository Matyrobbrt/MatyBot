package io.github.matyrobbrt.matybot.util.database;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.commons.io.FileUtils;

import com.jagrosh.jdautilities.command.SlashCommandEvent;

import io.github.matyrobbrt.javanbt.db.NBTDatabase;
import io.github.matyrobbrt.javanbt.db.NBTDatabaseManager;
import io.github.matyrobbrt.javanbt.nbt.CompoundNBT;
import io.github.matyrobbrt.javanbt.nbt.LongNBT;
import io.github.matyrobbrt.javanbt.serialization.NBTSerializable;
import io.github.matyrobbrt.javanbt.util.NBTManager;
import io.github.matyrobbrt.matybot.MatyBot;
import io.github.matyrobbrt.matybot.util.BotUtils.Markers;
import io.github.matyrobbrt.matybot.util.database.dao.nbt.GuildData;
import io.github.matyrobbrt.matybot.util.database.dao.nbt.UserSettings;
import io.github.matyrobbrt.matybot.util.nbt.NBTList;
import io.github.matyrobbrt.matybot.util.nbt.SnowflakeSpecifcData;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

public class MatyBotNBTDatabase extends NBTDatabase {

	public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("d-MM-uuuu_HH-mm-ss");
	public static final Timer TIMER = new Timer("DatabaseSaver");

	public MatyBotNBTDatabase(File file) {
		super(file, 0);
		if (!file.exists()) {
			try {
				Files.createFile(file.toPath());
			} catch (IOException e) {}
		}
	}

	private final NBTManager nbtManager = new NBTManager();

	@Getter
	private final List<Long> guildCache = createAndTrack("GuildCache",
			new NBTList<Long, LongNBT>(LongNBT::valueOf, LongNBT::getAsLong));

	private final SnowflakeSpecifcData<GuildData, CompoundNBT> guildData = createAndTrack("GuildData",
			new SnowflakeSpecifcData<>(GuildData::serializeNBT, GuildData.DESERIALIZER::fromNBT));

	public GuildData getDataForGuild(final long guildID) {
		return guildData.computeIfAbsent(guildID, k -> new GuildData());
	}

	public GuildData getDataForGuild(final Guild guild) {
		return getDataForGuild(guild.getIdLong());
	}

	public GuildData getDataForGuild(final SlashCommandEvent event) {
		return getDataForGuild(event.getGuild());
	}

	private final SnowflakeSpecifcData<UserSettings, CompoundNBT> userSettings = createAndTrack("UserSettings",
			new SnowflakeSpecifcData<>(UserSettings::serializeNBT, UserSettings.DESERIALIZER::fromNBT));

	public UserSettings getSettingsForUser(final long userId) {
		return userSettings.computeIfAbsent(userId, k -> new UserSettings());
	}

	public UserSettings getSettingsForUser(final User user) {
		return getSettingsForUser(user.getIdLong());
	}

	public UserSettings getSettingsForUser(final net.dv8tion.jda.api.entities.Member member) {
		return getSettingsForUser(member.getIdLong());
	}

	@Override
	public void load(CompoundNBT nbt) {
		nbtManager.deserializeNBT(nbt);
	}

	@Override
	public CompoundNBT save(CompoundNBT nbt) {
		return nbt.merge(nbtManager.serializeNBT());
	}

	private <T extends NBTSerializable<?>> T createAndTrack(String key, T value) {
		nbtManager.track(key, value);
		return value;
	}

	public void setDirtyAndSave() {
		setDirty();
		saveToDisk();
	}

	public static final TimerTask createBackupTask(final NBTDatabase database) {
		return createBackupTask(database,
				e -> MatyBot.LOGGER.error(Markers.DATABASE, "Exception while trying to backup the database {}! {}",
						database.getFile().getName(), e),
				() -> MatyBot.LOGGER.info(Markers.DATABASE, "The database {} has been automatically backed up!",
						database.getFile().getName()));
	}

	public static final TimerTask createBackupTask(final NBTDatabase database, final Consumer<Throwable> onException,
			Runnable finnaly) {
		return new TimerTask() {

			@Override
			public void run() {
				database.saveToDisk();
				final StringBuilder backupName = new StringBuilder();
				backupName.append(DATE_TIME_FORMATTER.format(LocalDateTime.now()));
				backupName.append(".dat");
				final var backupPath = Path.of("storage/backup/" + database.getFile().getName().replaceAll(".dat", ""))
						.resolve(backupName.toString());
				try {
					Files.createDirectories(
							Path.of("storage/backup/" + database.getFile().getName().replaceAll(".dat", "")));
				} catch (IOException e1) {}
				try {
					FileUtils.copyFile(database.getFile(), backupPath.toFile());
				} catch (IOException e) {
					onException.accept(e);
				} finally {

					finnaly.run();
				}
			}
		};
	}

	public static final class Manager extends NBTDatabaseManager {

		public Manager() {
			super(0);

			TIMER.scheduleAtFixedRate(new TimerTask() {

				@Override
				public void run() {
					Manager.super.save();
					MatyBot.LOGGER.info(Markers.DATABASE, "Databases have been automatically saved");
				}
			}, 0, 1000l * 60 * 10);
		}

		@Override
		public <T extends NBTDatabase> T computeIfAbsent(Function<File, T> creator, File storageFile) {
			T database = super.computeIfAbsent(creator, storageFile);
			TIMER.scheduleAtFixedRate(createBackupTask(database, e -> {
				MatyBot.LOGGER.error(Markers.DATABASE,
						"Exception while trying to backup the database {}. A new backup will be scheduled in 2 minutes!",
						database.getFile().getName(), e);
				TIMER.schedule(createBackupTask(database), 1000l * 60 * 2);
			}, () -> MatyBot.LOGGER.info(Markers.DATABASE, "The database {} has been automatically backed up!",
					database.getFile().getName())), 0, 1000l * 60 * 60);
			return database;
		}

		public void setDirtyAndSave() {
			databases.values().forEach(db -> {
				db.setDirty();
				db.saveToDisk();
			});
		}
	}

}
