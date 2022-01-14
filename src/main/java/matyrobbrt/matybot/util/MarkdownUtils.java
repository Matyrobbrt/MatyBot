package matyrobbrt.matybot.util;

import net.dv8tion.jda.api.entities.Message;

public class MarkdownUtils {

	public static String createMessageLink(final Message message) {
		return "https://discord.com/channels/" + message.getGuild().getIdLong() + "/" + message.getChannel().getIdLong()
				+ "/" + message.getIdLong();
	}

}
