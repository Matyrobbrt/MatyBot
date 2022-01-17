package io.github.matyrobbrt.matybot.util.database.dao.nbt;

import io.github.matyrobbrt.javanbt.nbt.CompoundNBT;
import io.github.matyrobbrt.javanbt.serialization.Deserializer;
import io.github.matyrobbrt.javanbt.serialization.NBTSerializable;
import io.github.matyrobbrt.javanbt.serialization.Serializers;
import io.github.matyrobbrt.matybot.util.nbt.NBTBuilder;

public class SuggestionData implements NBTSerializable<CompoundNBT> {

	public static final Deserializer<CompoundNBT, SuggestionData> DESERIALIZER = Serializers
			.registerDeserializer(SuggestionData.class, nbt -> {
				final SuggestionData data = new SuggestionData(nbt.getLong("OwnerID"));
				data.deserializeNBT(nbt);
				return data;
			});

	public SuggestionData(final long ownerId) {
		this.ownerId = ownerId;
	}

	private final long ownerId;
	private String denialReason = "";

	public String getDenialReason() {
		return denialReason;
	}

	public void setDenialReason(String denialReason) {
		this.denialReason = denialReason;
	}

	public long getOwnerId() {
		return ownerId;
	}

	private SuggestionStatus status = SuggestionStatus.NONE;

	public SuggestionStatus getStatus() {
		return status;
	}

	public void setStatus(SuggestionStatus status) {
		this.status = status;
	}

	@Override
	public CompoundNBT serializeNBT() {
		final var nbt = new CompoundNBT();
		if (!denialReason.isBlank() && status == SuggestionStatus.DENIED) {
			nbt.putString("DenialReason", denialReason);
		}
		return NBTBuilder.of(nbt).putLong("OwnerID", ownerId).putString("Status", status.toString()).build();
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		status = SuggestionStatus.valueOf(nbt.getString("Status"));
		if (nbt.contains("DenialReason")) {
			denialReason = nbt.getString("DenialReason");
		}
	}

	public enum SuggestionStatus {
		APPROVED, DENIED, CONSIDERED, NONE;
	}

}
