package io.github.matyrobbrt.matybot.managers.tricks;

import java.util.List;

import com.jagrosh.jdautilities.command.SlashCommandEvent;

import io.github.matyrobbrt.javanbt.nbt.CompoundNBT;
import io.github.matyrobbrt.matybot.managers.tricks.TrickContext.Slash;
import lombok.Data;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@Data
public final class ScriptTrick implements ITrick {

	public static final Type TYPE = new Type();

	private final List<String> names;

	@Override
	public CompoundNBT serializeNBT() {
		return null;
	}

	@Override
	public List<String> getNames() {
		return names;
	}

	@Override
	public Message getMessage(String[] args) {
		return null;
	}

	@Override
	public Message getMessageSlash(Slash slashContext) {
		return null;
	}

	@Override
	public TrickType<?> getType() {
		return TYPE;
	}

	static class Type implements TrickType<ScriptTrick> {

		@Override
		public Class<ScriptTrick> getTrickClass() {
			return ScriptTrick.class;
		}

		@Override
		public ScriptTrick createFromArgs(String args) {
			return null;
		}

		@Override
		public List<OptionData> getArgs() {
			return null;
		}

		@Override
		public ScriptTrick createFromCommand(SlashCommandEvent event) {
			return null;
		}

		@Override
		public ScriptTrick fromNBT(CompoundNBT nbt) {
			return null;
		}

		@Override
		public CompoundNBT toNBT(ScriptTrick trick) {
			return null;
		}

	}

}
