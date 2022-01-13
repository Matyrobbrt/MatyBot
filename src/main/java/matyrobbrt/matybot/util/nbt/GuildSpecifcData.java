package matyrobbrt.matybot.util.nbt;

import java.util.HashMap;
import java.util.function.Function;

import com.matyrobbrt.javanbt.nbt.CompoundNBT;
import com.matyrobbrt.javanbt.nbt.NBT;
import com.matyrobbrt.javanbt.serialization.NBTSerializable;

public class GuildSpecifcData<O, ONBT extends NBT> extends HashMap<Long, O> implements NBTSerializable<CompoundNBT> {

	private static final long serialVersionUID = -4918669586732868195L;

	private final transient Function<O, ONBT> serializer;
	private final transient Function<ONBT, O> deserializer;

	public GuildSpecifcData(Function<O, ONBT> serializer, Function<ONBT, O> deserializer) {
		super();
		this.serializer = serializer;
		this.deserializer = deserializer;
	}

	@Override
	public CompoundNBT serializeNBT() {
		CompoundNBT nbt = new CompoundNBT();
		forEach((key, entry) -> nbt.put(String.valueOf(key), serializer.apply(entry)));
		return nbt;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		nbt.entriesModifiable()
				.forEach((key, entryNBT) -> put(Long.parseLong(key), deserializer.apply((ONBT) entryNBT)));
	}

}
