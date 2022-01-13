package matyrobbrt.matybot.quotes;

import com.google.gson.annotations.Expose;
import com.matyrobbrt.javanbt.nbt.CompoundNBT;
import com.matyrobbrt.javanbt.serialization.Deserializer;
import com.matyrobbrt.javanbt.serialization.NBTSerializable;
import com.matyrobbrt.javanbt.serialization.Serializers;
import com.matyrobbrt.javanbt.util.NBTBuilder;

import matyrobbrt.matybot.util.helper.MentionHelper;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

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

	public String getAuthorFormatter(final Guild guild, boolean asTag) {
		if (asTag) {
			final Member member = guild.getMemberById(getAuthor());
			return member == null ? String.format("Author ID: %s", getAuthor()) : member.getUser().getAsTag();
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
