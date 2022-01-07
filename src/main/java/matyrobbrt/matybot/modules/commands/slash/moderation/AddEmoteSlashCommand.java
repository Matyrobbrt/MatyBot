package matyrobbrt.matybot.modules.commands.slash.moderation;

import java.io.BufferedInputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;

import matyrobbrt.matybot.api.annotation.RegisterSlashCommand;
import matyrobbrt.matybot.util.BotUtils;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.Icon.IconType;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class AddEmoteSlashCommand extends SlashCommand {

	@RegisterSlashCommand
	private static final AddEmoteSlashCommand CMD = new AddEmoteSlashCommand();

	public AddEmoteSlashCommand() {
		name = "add-emote";
		help = "Adds an emote from the uuid of an already-exising emote ID";
		options = List.of(
				new OptionData(OptionType.STRING, "name", "The name of the emote to add", true),
				new OptionData(OptionType.STRING, "emote_id", "The ID of the emote to add", true),
				new OptionData(OptionType.STRING, "file_format",
						"The file format of the emote (usually png for normal emotes, and gifs for animated ones)")
								.addChoice("png", "png").addChoice("gif", "gif").addChoice("jpeg", "jpeg")
								.addChoice("webp", "webp"),
				new OptionData(OptionType.INTEGER, "size", "The size of the emote. (usually 80)")
				);
	}

	@Override
	protected void execute(SlashCommandEvent event) {
		final long emoteId = event.getOption("emote_id").getAsLong();
		final String name = event.getOption("name").getAsString();
		final String fileFormat = BotUtils.getOptionOr(event.getOption("file_format"), OptionMapping::getAsString,
				"png");
		final int size = BotUtils.getOptionOr(event.getOption("size"), m -> (int) m.getAsDouble(), 80);
		final String urlString = "https://cdn.discordapp.com/emojis/" + emoteId + "." + fileFormat + "?size=" + size;

		try {
			URL url = new URL(urlString);
			URLConnection uc = url.openConnection();
			uc.setRequestProperty("X-Requested-With", "Curl");
			try (final BufferedInputStream is = new BufferedInputStream(uc.getInputStream())) {
				event.getGuild().createEmote(name, Icon.from(is, IconType.PNG)).queue(e -> {
					event.getInteraction().reply("New Emote!")
							.queue(h -> h.retrieveOriginal().queue(m -> m.addReaction(e).queue()));
				});
			}
		} catch (Exception e) {}
	}

}
