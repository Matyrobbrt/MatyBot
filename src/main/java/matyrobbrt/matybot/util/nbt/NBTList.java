package matyrobbrt.matybot.util.nbt;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.matyrobbrt.javanbt.nbt.ListNBT;
import com.matyrobbrt.javanbt.nbt.NBT;
import com.matyrobbrt.javanbt.serialization.NBTSerializable;

public class NBTList<O, ONBT extends NBT> extends ArrayList<O> implements NBTSerializable<ListNBT> {

	private static final long serialVersionUID = -8221947185139769286L;

	private final transient Function<O, ONBT> serializer;
	private final transient Function<ONBT, O> deserializer;

	public NBTList(Function<O, ONBT> serializer, Function<ONBT, O> deserializer) {
		super();
		this.serializer = serializer;
		this.deserializer = deserializer;
	}

	public NBTList(Function<O, ONBT> serializer, Function<ONBT, O> deserializer, List<O> other) {
		super(other);
		this.serializer = serializer;
		this.deserializer = deserializer;
	}

	@Override
	public ListNBT serializeNBT() {
		final ListNBT list = new ListNBT();
		forEach(obj -> list.add(serializer.apply(obj)));
		return list;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void deserializeNBT(ListNBT list) {
		list.forEach(nbt -> add(deserializer.apply((ONBT) nbt)));
	}

	@Override
	public boolean equals(Object o) {
		return super.equals(o) && o instanceof NBTList<?, ?>;
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

}
