package io.github.matyrobbrt.matybot.managers.quotes;

import java.util.concurrent.ExecutionException;

import com.google.gson.annotations.Expose;

import io.github.matyrobbrt.javanbt.nbt.CompoundNBT;
import io.github.matyrobbrt.javanbt.serialization.Deserializer;
import io.github.matyrobbrt.javanbt.serialization.NBTSerializable;
import io.github.matyrobbrt.javanbt.serialization.Serializers;
import io.github.matyrobbrt.javanbt.util.NBTBuilder;
import io.github.matyrobbrt.matybot.util.helper.MentionHelper;
import net.dv8tion.jda.api.entities.Guild;

public class Quote implements NBTSerializable<CompoundNBT> {

	@Expose
	private String quote;
	@Expose
	private long author;
	@Expose
	private long quoter;

	public Quote(String quote, long author, long quoter) {
		this.quote = quote;
		this.author = author;
		this.quoter = quoter;
	}

	public String getQuote() { return quote; }

	public long getAuthor() { return author; }

	public long getQuoter() { return quoter; }

	public String getAuthorFormatted(final Guild guild, boolean asTag) {
		if (asTag) {
			try {
				final var member = guild.retrieveMemberById(getAuthor()).submit().get();
				return member == null ? "Author ID: %s".formatted(getAuthor()) : member.getUser().getAsTag();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
			return "Author ID: %s".formatted(getAuthor());
		} else {
			return guild.getMemberById(getAuthor()) == null ? String.format("Author ID: %s", getAuthor())
					: MentionHelper.user(getAuthor()).getAsMention();
		}
	}

	@Override
	public CompoundNBT serializeNBT() {
		return new NBTBuilder().putString("quote", quote).putLong("author", author).putLong("quoter", quoter).build();
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		quote = nbt.getString("quote");
		author = nbt.getLong("author");
		quoter = nbt.getLong("quoter");
	}

	private Quote() {

	}

	public static final Deserializer<CompoundNBT, Quote> DESERIALIZER = Serializers.registerDeserializer(Quote.class,
			nbt -> {
				final Quote quote = new Quote();
				quote.deserializeNBT(nbt);
				return quote;
			});

}
