package matyrobbrt.matybot.quotes;

import com.google.gson.annotations.Expose;

import matyrobbrt.matybot.util.helper.MentionHelper;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

public class Quote {

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

}
