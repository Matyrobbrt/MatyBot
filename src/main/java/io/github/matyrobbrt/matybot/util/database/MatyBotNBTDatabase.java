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

import org.apache.commons.io.FileUtils;

import com.jagrosh.jdautilities.command.SlashCommandEvent;

import io.github.matyrobbrt.javanbt.db.NBTDatabase;
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
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

public class MatyBotNBTDatabase extends NBTDatabase {

	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("d-MM-uuuu_H-m-s");
	private static final Timer TIMER = new Timer("DatabaseSaver");

	public MatyBotNBTDatabase(File file) {
		super(file, 0);
		if (!file.exists()) {
			try {
				Files.createFile(file.toPath());
			} catch (IOException e) {}
		}

		// Don't make this very frequent since the DatabaseManager saves it once 15
		// minutes already.. We should call setDirtyAndSave() when we need immediate
		// saving!
		TIMER.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				saveToDisk();
				MatyBot.LOGGER.info(Markers.DATABASE, "Database has been automatically saved");
			}
		}, 0, 1000l * 60 * 10);

		// Backup the database every hour
		TIMER.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				saveToDisk();
				final StringBuilder backupName = new StringBuilder(getFile().getName().replaceAll(".dat", ""))
						.append("_");
				backupName.append(DATE_TIME_FORMATTER.format(LocalDateTime.now()));
				backupName.append(".dat");
				final var backupPath = Path.of("storage/backup").resolve(backupName.toString());
				try {
					FileUtils.copyFile(getFile(), backupPath.toFile());
				} catch (IOException e) {
					MatyBot.LOGGER.error(Markers.DATABASE, "Exception while trying to backup the database!", e);
				} finally {
					MatyBot.LOGGER.info(Markers.DATABASE, "Database has been automatically backed up!");
				}
			}
		}, 0, 1000l * 60 * 60);
	}

	private final NBTManager nbtManager = new NBTManager();

	private List<Long> guildCache = createAndTrack("GuildCache",
			new NBTList<Long, LongNBT>(LongNBT::valueOf, LongNBT::getAsLong));

	public List<Long> getGuildCache() {
		return guildCache;
	}

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

}
