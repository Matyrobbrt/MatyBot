package io.github.matyrobbrt.matybot.util.database.dao.nbt;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

import io.github.matyrobbrt.javanbt.nbt.CompoundNBT;
import io.github.matyrobbrt.javanbt.serialization.Deserializer;
import io.github.matyrobbrt.javanbt.serialization.NBTSerializable;
import io.github.matyrobbrt.javanbt.serialization.Serializers;

public class MessageData implements NBTSerializable<CompoundNBT> {

	public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

	public static final Deserializer<CompoundNBT, MessageData> DESERIALIZER = Serializers
			.registerDeserializer(MessageData.class, nbt -> {
				final MessageData data = new MessageData(nbt.getLong("AuthorID"),
						DATE_TIME_FORMATTER.parse(nbt.getString("TimeCreated")));
				data.deserializeNBT(nbt);
				return data;
			});

	private final long authorId;
	private TemporalAccessor timeCreated;
	private String content = "";

	public MessageData(long authorId, final TemporalAccessor timeCreated) {
		this.authorId = authorId;
		this.timeCreated = timeCreated;
	}

	@Override
	public CompoundNBT serializeNBT() {
		return io.github.matyrobbrt.matybot.util.nbt.NBTBuilder.of().putLong("AuthorID", authorId)
				.putString("Content", content).putString("TimeCreated", DATE_TIME_FORMATTER.format(timeCreated))
				.build();
	}

	public String getContent() {
		return content;
	}

	public MessageData setContent(String content) {
		this.content = content;
		return this;
	}

	public long getAuthorId() {
		return authorId;
	}

	public OffsetDateTime getTimeCreated() {
		return OffsetDateTime.from(timeCreated);
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		content = nbt.getString("Content");
	}

}
