package io.github.matyrobbrt.matybot.util.database;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

import io.github.matyrobbrt.javanbt.db.NBTDatabase;
import io.github.matyrobbrt.javanbt.nbt.CompoundNBT;
import io.github.matyrobbrt.matybot.util.database.dao.nbt.MessageData;
import io.github.matyrobbrt.matybot.util.nbt.SnowflakeSpecifcData;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;

public class MessageCache extends NBTDatabase {

	public MessageCache(File file) {
		super(file, 0);
		if (!file.exists()) {
			try {
				Files.createFile(file.toPath());
			} catch (IOException e) {}
		}
	}

	private final SnowflakeSpecifcData<SnowflakeSpecifcData<SnowflakeSpecifcData<MessageData, CompoundNBT>, CompoundNBT>, CompoundNBT> data = createGuildsData();

	private static SnowflakeSpecifcData<SnowflakeSpecifcData<SnowflakeSpecifcData<MessageData, CompoundNBT>, CompoundNBT>, CompoundNBT> createGuildsData() {
		return new SnowflakeSpecifcData<>(SnowflakeSpecifcData::serializeNBT, compound -> {
			final var channelData = createChannelsData();
			channelData.deserializeNBT(compound);
			return channelData;
		});
	}

	private static SnowflakeSpecifcData<SnowflakeSpecifcData<MessageData, CompoundNBT>, CompoundNBT> createChannelsData() {
		return new SnowflakeSpecifcData<>(SnowflakeSpecifcData::serializeNBT, compound -> {
			final SnowflakeSpecifcData<MessageData, CompoundNBT> msgCache = new SnowflakeSpecifcData<>(
					MessageData::serializeNBT, MessageData.DESERIALIZER::fromNBT);
			msgCache.deserializeNBT(compound);
			return msgCache;
		});
	}

	private static SnowflakeSpecifcData<MessageData, CompoundNBT> createChannelData() {
		return new SnowflakeSpecifcData<>(MessageData::serializeNBT, MessageData.DESERIALIZER::fromNBT);
	}

	public Map<Long, MessageData> getDataForChannel(final long guildId, final long channelId) {
		return data.computeIfAbsent(guildId, k -> createChannelsData()).computeIfAbsent(channelId,
				k -> createChannelData());
	}

	public void putMessageData(final long guildId, final long channelId, final long messageId, final MessageData data) {
		getDataForChannel(guildId, channelId).put(messageId, data);
		setDirty();
	}

	public MessageData computeMessageData(final Message message) {
		final var toRet = getDataForChannel(message.getGuild().getIdLong(), message.getChannel().getIdLong())
				.computeIfAbsent(message.getIdLong(),
						k -> new MessageData(message.getAuthor().getIdLong(), message.getTimeCreated())
								.setContent(message.getContentRaw()));
		setDirty();
		return toRet;
	}

	public MessageData getMessageData(final GenericMessageEvent event) {
		return getDataForChannel(event.getGuild().getIdLong(), event.getChannel().getIdLong())
				.get(event.getMessageIdLong());
	}

	@Override
	public void load(CompoundNBT nbt) {
		data.deserializeNBT(nbt);
	}

	@Override
	public CompoundNBT save(CompoundNBT nbt) {
		return nbt.merge(data.serializeNBT());
	}

}
