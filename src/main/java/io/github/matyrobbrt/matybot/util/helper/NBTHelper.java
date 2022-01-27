package io.github.matyrobbrt.matybot.util.helper;

import java.util.ArrayList;
import java.util.List;

import io.github.matyrobbrt.javanbt.nbt.CompoundNBT;
import io.github.matyrobbrt.javanbt.nbt.ListNBT;
import io.github.matyrobbrt.javanbt.nbt.StringNBT;
import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.entities.MessageEmbed;

@UtilityClass
public class NBTHelper {

	public static ListNBT serializeStringList(final List<String> list) {
		ListNBT listNBT = new ListNBT();
		for (int i = 0; i < list.size(); i++) {
			listNBT.add(i, StringNBT.valueOf(list.get(i)));
		}
		return listNBT;
	}

	public static List<String> deserializeStringList(final ListNBT listNBT) {
		final List<String> list = new ArrayList<>();
		for (int i = 0; i < listNBT.size(); i++) {
			list.add(i, listNBT.getString(i));
		}
		return list;
	}

	public static CompoundNBT serializeEmbedField(final MessageEmbed.Field field) {
		CompoundNBT nbt = new CompoundNBT();
		nbt.putString("name", field.getName());
		nbt.putString("value", field.getValue());
		nbt.putBoolean("inline", field.isInline());
		return nbt;
	}

	public static MessageEmbed.Field deserializeEmbedField(final CompoundNBT nbt) {
		final var name = nbt.getString("name");
		final var value = nbt.getString("value");
		final var inline = nbt.getBoolean("inline");
		return new MessageEmbed.Field(name, value, inline);
	}

}
