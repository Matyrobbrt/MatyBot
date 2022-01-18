package io.github.matyrobbrt.matybot.util.database.dao.nbt;

import io.github.matyrobbrt.javanbt.nbt.CompoundNBT;
import io.github.matyrobbrt.javanbt.serialization.Deserializer;
import io.github.matyrobbrt.javanbt.serialization.NBTSerializable;
import io.github.matyrobbrt.javanbt.serialization.Serializers;
import io.github.matyrobbrt.matybot.util.nbt.NBTBuilder;

public class UserSettings implements NBTSerializable<CompoundNBT> {

	public static final Deserializer<CompoundNBT, UserSettings> DESERIALIZER = Serializers
			.registerDeserializer(UserSettings.class, nbt -> {
				final var settings = new UserSettings();
				settings.deserializeNBT(nbt);
				return settings;
			});

	private boolean levelUpPing = true;

	/**
	 * @return the levelUpPing
	 */
	public boolean doesLevelUpPing() {
		return levelUpPing;
	}

	/**
	 * @param levelUpPing the levelUpPing to set
	 */
	public void setLevelUpPing(boolean levelUpPing) {
		this.levelUpPing = levelUpPing;
	}

	@Override
	public CompoundNBT serializeNBT() {
		return NBTBuilder.of().putBoolean("levelUpPing", doesLevelUpPing()).build();
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		setLevelUpPing(nbt.getBoolean("levelUpPing"));
	}

}
