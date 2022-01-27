package io.github.matyrobbrt.matybot.managers.tricks;

import static io.github.matyrobbrt.matybot.util.BotUtils.getArgumentOrEmpty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.text.StrBuilder;

import com.jagrosh.jdautilities.command.SlashCommandEvent;

import io.github.matyrobbrt.javanbt.nbt.CompoundNBT;
import io.github.matyrobbrt.matybot.util.helper.NBTHelper;
import io.github.matyrobbrt.matybot.util.nbt.OrderedNBTList;
import lombok.Data;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@Data
@SuppressWarnings("deprecation")
public class EmbedTrick implements ITrick {

	public static final Type TYPE = new Type();

	private final List<String> names;

	private final String title;

	private final String description;

	private final int colour;

	private final OrderedNBTList<MessageEmbed.Field, CompoundNBT> fields;

	public EmbedTrick(final List<String> names, final String title, final String description, final int color,
			final List<MessageEmbed.Field> fields) {
		this.names = names;
		this.title = title;
		this.description = description;
		this.colour = color;
		this.fields = new OrderedNBTList<>(NBTHelper::serializeEmbedField, NBTHelper::deserializeEmbedField, fields);
	}

	public EmbedTrick(final List<String> names, final String title, final String description, final int color,
			final MessageEmbed.Field... fields) {
		this(names, title, description, color, Arrays.asList(fields));
	}

	@Override
	public Message getMessage(final String[] args) {
		EmbedBuilder builder = new EmbedBuilder().setTitle(getTitle()).setDescription(getDescription())
				.setColor(colour);
		for (MessageEmbed.Field field : getFields()) {
			builder.addField(field);
		}
		return new MessageBuilder(builder.build())
				.setAllowedMentions(Set.of(Message.MentionType.CHANNEL, Message.MentionType.EMOTE)).build();
	}

	static class Type implements TrickType<EmbedTrick> {

		@Override
		public Class<EmbedTrick> getTrickClass() {
			return EmbedTrick.class;
		}

		@Override
		public EmbedTrick createFromArgs(final String args) {
			String[] argsArray = args.split(" \\| ");
			if (argsArray.length == 3) {
				return new EmbedTrick(Arrays.asList(argsArray[0].split(" ")), argsArray[1], argsArray[2],
						Integer.parseInt(argsArray[3].replace("#", ""), 16));
			} else {
				final String[] fieldArgsArray = argsArray[3].split(" \\~ ");
				final List<Field> fields = new ArrayList<>();
				for (var field : fieldArgsArray) {
					fields.add(fieldFromArgs(field));
				}
				return new EmbedTrick(Arrays.asList(argsArray[0].split(" ")), argsArray[1], argsArray[2],
						Integer.parseInt(argsArray[4].replace("#", ""), 16), fields);
			}
		}

		private static Field fieldFromArgs(final String args) {
			final String[] argsArray = args.split(" \\¬ ");
			final var title = argsArray[0];
			final var name = argsArray[1];
			final boolean inline = Boolean.parseBoolean(argsArray.length == 2 ? "false" : argsArray[2]);
			return new Field(title, name, inline);
		}

		@Override
		public List<OptionData> getArgs() {
			return List.of(
					new OptionData(OptionType.STRING, "names", "Name(s) for the trick. Separate with spaces.")
							.setRequired(true),
					new OptionData(OptionType.STRING, "title", "Title of the embed.").setRequired(true),
					new OptionData(OptionType.STRING, "description", "Description of the embed.").setRequired(true),
					new OptionData(OptionType.STRING, "color",
							"Hex color string in #AABBCC format, used for the embed.").setRequired(true));
		}

		@Override
		public EmbedTrick createFromCommand(final SlashCommandEvent event) {
			final var stringBuilder = new StrBuilder(0);
			final var description = getArgumentOrEmpty(event, "description").split("\n");
			for (var line : description) {
				stringBuilder.appendln(line);
			}
			return new EmbedTrick(Arrays.asList(getArgumentOrEmpty(event, "names").split(" ")),
					getArgumentOrEmpty(event, "title"), stringBuilder.toString(),
					Integer.parseInt(getArgumentOrEmpty(event, "color").replaceAll("#", "").replaceAll("0x", ""), 16));
		}

		@Override
		public EmbedTrick fromNBT(CompoundNBT nbt) {
			final var names = NBTHelper.deserializeStringList(nbt.getList("names", 8));
			final String title = nbt.getString("title");
			final String description = nbt.getString("description");
			final int colour = nbt.getInt("color");
			final EmbedTrick trick = new EmbedTrick(names, title, description, colour);
			trick.fields.deserializeNBT(nbt.getCompound("fields"));
			return trick;
		}

		@Override
		public CompoundNBT toNBT(EmbedTrick trick) {
			CompoundNBT nbt = new CompoundNBT();
			nbt.put("names", NBTHelper.serializeStringList(trick.getNames()));
			nbt.putString("title", trick.getTitle());
			nbt.putString("description", trick.getDescription());
			nbt.putInt("color", trick.getColour());
			nbt.put("fields", trick.fields);
			return nbt;
		}
	}

	@Override
	public CompoundNBT serializeNBT() {
		return TYPE.toNBT(this);
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		fields.deserializeNBT(nbt.getCompound("fields"));
	}

	@Override
	public TrickType<?> getType() {
		return TYPE;
	}
}
