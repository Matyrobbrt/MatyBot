package matyrobbrt.matybot.modules.commands.common.info;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;

import matyrobbrt.matybot.MatyBot;
import matyrobbrt.matybot.api.annotation.RegisterCommand;
import matyrobbrt.matybot.api.annotation.RegisterSlashCommand;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public final class SearchCommand extends SlashCommand {

	@RegisterCommand
	@RegisterSlashCommand
	private static final SearchCommand LMGTFY = new SearchCommand("lmgtfy", "https://lmgtfy.com/?q=",
			"let-me-google-that-for-you");

	@RegisterCommand
	@RegisterSlashCommand
	private static final SearchCommand GOOGLE = new SearchCommand("google", "https://www.google.com/search?q=", "goog");

	@RegisterCommand
	@RegisterSlashCommand
	private static final SearchCommand BING = new SearchCommand("bing", "https://www.bing.com/search?q=");

	@RegisterCommand
	@RegisterSlashCommand
	private static final SearchCommand DUCK_DUCK_GO = new SearchCommand("duckduckgo", "https://duckduckgo.com/?q=",
			"ddg");

	private final String baseUrl;

	public SearchCommand(final String name, final String baseUrl, final String... aliases) {
		this.name = name.toLowerCase(Locale.ROOT);
		this.aliases = aliases;
		this.help = "Search for something using " + name + ".";
		this.baseUrl = baseUrl;

		OptionData data = new OptionData(OptionType.STRING, "text", "The text to search").setRequired(true);
		List<OptionData> dataList = new ArrayList<>();
		dataList.add(data);
		this.options = dataList;
		guildOnly = false;
	}

	@Override
	protected void execute(CommandEvent event) {
		final var args = event.getArgs();
		try {
			final String query = URLEncoder.encode(args, StandardCharsets.UTF_8.toString());
			event.getMessage().reply(baseUrl + query).mentionRepliedUser(false).queue();
		} catch (UnsupportedEncodingException ex) {
			MatyBot.LOGGER.error("Error processing search query {}: {}", args, ex);
			event.getMessage().reply("There was an error processing your command.").mentionRepliedUser(false).queue();
		}
	}

	@Override
	protected void execute(final SlashCommandEvent event) {
		try {
			final String query = URLEncoder.encode(event.getOption("text").getAsString(),
					StandardCharsets.UTF_8.toString());
			event.reply(baseUrl + query).mentionRepliedUser(false).queue();
		} catch (UnsupportedEncodingException ex) {
			MatyBot.LOGGER.error("Error processing search query {}: {}", event.getOption("text").getAsString(), ex);
			event.reply("There was an error processing your command.").mentionRepliedUser(false).queue();
		}

	}

}
