package matyrobbrt.matybot.util.nbt;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import io.github.matyrobbrt.javanbt.nbt.CompoundNBT;
import io.github.matyrobbrt.javanbt.nbt.NBT;
import io.github.matyrobbrt.javanbt.serialization.NBTSerializable;

public class OrderedNBTList<O, ONBT extends NBT> extends ArrayList<O> implements NBTSerializable<CompoundNBT> {

	private static final long serialVersionUID = -8221947185139769286L;

	private final transient Function<O, ONBT> serializer;
	private final transient Function<ONBT, O> deserializer;

	public OrderedNBTList(Function<O, ONBT> serializer, Function<ONBT, O> deserializer) {
		super();
		this.serializer = serializer;
		this.deserializer = deserializer;
	}

	public OrderedNBTList(Function<O, ONBT> serializer, Function<ONBT, O> deserializer, List<O> other) {
		super(other);
		this.serializer = serializer;
		this.deserializer = deserializer;
	}

	@Override
	public CompoundNBT serializeNBT() {
		CompoundNBT tag = new CompoundNBT();
		tag.putInt("size", size());
		for (int i = 0; i < size(); i++) {
			tag.put(String.valueOf(i), serializer.apply(get(i)));
		}
		return tag;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		int size = nbt.getInt("size");
		for (int i = 0; i < size; i++) {
			O element = deserializer.apply((ONBT) nbt.get(String.valueOf(i)));
			if (i < size()) {
				set(i, element);
			} else {
				add(i, element);
			}
		}
	}

	@Override
	public boolean equals(Object o) {
		return super.equals(o) && o instanceof OrderedNBTList<?, ?> list && list == o;
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

}
