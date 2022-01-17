package io.github.matyrobbrt.matybot.managers.tricks;

import static io.github.matyrobbrt.matybot.util.BotUtils.getArgumentOrEmpty;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.jagrosh.jdautilities.command.SlashCommandEvent;

import io.github.matyrobbrt.javanbt.nbt.CompoundNBT;
import io.github.matyrobbrt.javanbt.util.NBTBuilder;
import io.github.matyrobbrt.matybot.util.helper.NBTHelper;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class StringTrick implements ITrick {

	public static final Type TYPE = new Type();

	private final List<String> names;

	private final String body;

	public StringTrick(final List<String> names, final String body) {
		this.names = names;
		this.body = body;
	}

	@Override
	public List<String> getNames() {
		return names;
	}

	@Override
	public Message getMessage(final String[] args) {
		return new MessageBuilder(String.format(getBody(), (Object[]) args))
				.setAllowedMentions(Set.of(Message.MentionType.CHANNEL, Message.MentionType.EMOTE)).build();
	}

	public String getBody() {
		return body;
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
			return new StringTrick(Arrays.asList(getArgumentOrEmpty(event, "names").split(" ")),
					getArgumentOrEmpty(event, "content"));
		}

		@Override
		public StringTrick fromNBT(CompoundNBT nbt) {
			final List<String> names = NBTHelper.deserializeStringList(nbt.getList("names", 8));
			return new StringTrick(names, nbt.getString("body"));
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
