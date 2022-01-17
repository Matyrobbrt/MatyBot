package io.github.matyrobbrt.matybot.util.database.dao.nbt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.github.matyrobbrt.javanbt.nbt.CompoundNBT;
import io.github.matyrobbrt.javanbt.serialization.Deserializer;
import io.github.matyrobbrt.javanbt.serialization.NBTSerializable;
import io.github.matyrobbrt.javanbt.serialization.Serializers;
import io.github.matyrobbrt.javanbt.util.NBTBuilder;
import io.github.matyrobbrt.javanbt.util.NBTManager;
import io.github.matyrobbrt.matybot.managers.quotes.Quote;
import io.github.matyrobbrt.matybot.managers.tricks.ITrick;
import io.github.matyrobbrt.matybot.managers.tricks.TrickManager;
import io.github.matyrobbrt.matybot.util.nbt.NBTList;
import io.github.matyrobbrt.matybot.util.nbt.OrderedNBTList;
import io.github.matyrobbrt.matybot.util.nbt.SnowflakeSpecifcData;
import net.dv8tion.jda.api.entities.Member;

public class GuildData implements NBTSerializable<CompoundNBT> {

	public static final Deserializer<CompoundNBT, GuildData> DESERIALIZER = Serializers
			.registerDeserializer(GuildData.class, nbt -> {
				GuildData data = new GuildData();
				data.deserializeNBT(nbt);
				return data;
			});

	private final NBTManager nbtManager = new NBTManager();

	private final List<Quote> quotes = createAndTrack("Quotes",
			new OrderedNBTList<>(Quote::serializeNBT, Quote.DESERIALIZER::fromNBT));

	public List<Quote> getQuotes() { return quotes; }

	private final List<ITrick> tricks = createAndTrack("Tricks", new NBTList<ITrick, CompoundNBT>(trick -> {
		return new NBTBuilder().putString("type", TrickManager.getTrickTypeName(trick.getType()))
				.put("value", trick.serializeNBT()).build();
	}, nbt -> TrickManager.getTrickType(nbt.getString("type")).fromNBT(nbt.getCompound("value"))));

	public List<ITrick> getTricks() { return tricks; }

	private final Map<Long, LevelData> levels = createAndTrack("Levels",
			new SnowflakeSpecifcData<>(LevelData::serializeNBT, LevelData.DESERIALIZER::fromNBT));

	private final Map<Long, SuggestionData> suggestions = createAndTrack("Suggestions",
			new SnowflakeSpecifcData<>(SuggestionData::serializeNBT, SuggestionData.DESERIALIZER::fromNBT));

	public Map<Long, LevelData> getLevels() { return levels; }

	public Map<Long, SuggestionData> getSuggestions() {
		return suggestions;
	}

	public List<Long> getLeaderboardSorted() {
		final List<Long> data = new ArrayList<>();
		levels.keySet().stream().sorted(
				(lvl1, lvl2) -> Integer.compare(getLevelDataForUser(lvl2).getXp(), getLevelDataForUser(lvl1).getXp()))
				.forEach(data::add);
		return data;
	}

	public LevelData getLevelDataForUser(final long userId) {
		return levels.computeIfAbsent(userId, k -> new LevelData());
	}

	public LevelData getLevelDataForUser(final Member member) {
		return getLevelDataForUser(member.getIdLong());
	}

	@Override
	public CompoundNBT serializeNBT() {
		return nbtManager.serializeNBT();
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		nbtManager.deserializeNBT(nbt);
	}

	private <T extends NBTSerializable<?>> T createAndTrack(String key, T value) {
		nbtManager.track(key, value);
		return value;
	}

}
