package io.github.matyrobbrt.matybot.managers.tricks;

import static io.github.matyrobbrt.matybot.util.BotUtils.getArgumentOrEmpty;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.text.StrBuilder;

import com.jagrosh.jdautilities.command.SlashCommandEvent;

import io.github.matyrobbrt.javanbt.nbt.CompoundNBT;
import io.github.matyrobbrt.javanbt.util.NBTBuilder;
import io.github.matyrobbrt.matybot.util.helper.NBTHelper;
import lombok.Builder;
import lombok.Data;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@Data
@Builder
@SuppressWarnings("deprecation")
public class StringTrick implements ITrick {

	public static final Type TYPE = new Type();

	private final List<String> names;
	private final String body;

	@Override
	public Message getMessage(final String[] args) {
		return new MessageBuilder(String.format(getBody(), (Object[]) args))
				.setAllowedMentions(Set.of(Message.MentionType.CHANNEL, Message.MentionType.EMOTE)).build();
	}

	static class Type implements TrickType<StringTrick> {

		@Override
		public Class<StringTrick> getTrickClass() {
			return StringTrick.class;
		}

		@Override
		public StringTrick createFromArgs(final String args) {
			String[] argsArray = args.split(" \\| ");
			return new StringTrick(Arrays.asList(argsArray[0].split(" ")), argsArray[1]);
		}

		@Override
		public List<OptionData> getArgs() {
			return List.of(
					new OptionData(OptionType.STRING, "names", "Name(s) for the trick. Separate with spaces.")
							.setRequired(true),
					new OptionData(OptionType.STRING, "content", "The content of the trick.").setRequired(true));
		}

		@Override
		public StringTrick createFromCommand(final SlashCommandEvent event) {
			final var stringBuilder = new StrBuilder();
			final var description = getArgumentOrEmpty(event, "content").split("\n");
			for (var line : description) {
				stringBuilder.appendln(line);
			}
			return StringTrick.builder().names(Arrays.asList(getArgumentOrEmpty(event, "names").split(" ")))
					.body(stringBuilder.toString()).build();
		}

		@Override
		public StringTrick fromNBT(CompoundNBT nbt) {
			final List<String> names = NBTHelper.deserializeStringList(nbt.getList("names", 8));
			final var stringBuilder = new StrBuilder();
			final var body = nbt.getString("body").split("\n");
			for (var line : body) {
				stringBuilder.appendln(line);
			}
			return builder().names(names).body(stringBuilder.toString()).build();
		}

		@Override
		public CompoundNBT toNBT(StringTrick trick) {
			return new NBTBuilder().put("names", NBTHelper.serializeStringList(trick.getNames()))
					.putString("body", trick.getBody()).build();
		}
	}

	@Override
	public CompoundNBT serializeNBT() {
		return TYPE.toNBT(this);
	}

	@Override
	public TrickType<?> getType() {
		return TYPE;
	}
}
