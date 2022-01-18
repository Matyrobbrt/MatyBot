package io.github.matyrobbrt.matybot.modules.levelling;

import java.time.Instant;
import java.util.ArrayList;

import com.jagrosh.jdautilities.command.CooldownScope;
import com.jagrosh.jdautilities.command.SlashCommandEvent;

import io.github.matyrobbrt.jdautils.event.EventListenerWrapper;
import io.github.matyrobbrt.matybot.MatyBot;
import io.github.matyrobbrt.matybot.api.annotation.RegisterSlashCommand;
import io.github.matyrobbrt.matybot.api.command.slash.PaginatedCommand;
import io.github.matyrobbrt.matybot.util.BotUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;

public class LeaderboardCommand extends PaginatedCommand {

	private static ButtonListener listener;

	public static EventListenerWrapper getWrappedListener() { return new EventListenerWrapper(listener); }

	@RegisterSlashCommand
	private static final LeaderboardCommand CMD = new LeaderboardCommand();

	private LeaderboardCommand() {
		super("leaderboard", "Gets the top users (in levels) from this guild.", true, new ArrayList<>(), 10);
		cooldown = 30;
		cooldownScope = CooldownScope.GUILD;
		listener = new ButtonListener();
	}

	@Override
	protected void execute(SlashCommandEvent event) {
		if (!event.isFromGuild()) {
			event.deferReply(true).setContent("This command only works in guilds!").queue();
			return;
		}
		if (!LevellingModule.isLevellingEnabled(event.getGuild())) {
			event.deferReply().setContent("Levelling is not enabled on this server!").setEphemeral(true).queue();
			return;
		}
		updateMaximum(MatyBot.nbtDatabase().getDataForGuild(event).getLeaderboardSorted().size() - 1);
		sendPaginatedMessage(event);
	}

	public class ButtonListener extends PaginatedCommand.ButtonListener {

		@Override
		public String getButtonID() { return "leaderboard"; }
	}

	@Override
	protected EmbedBuilder getEmbed(int start, Guild guild) {
		EmbedBuilder embed;
		final var randomColour = BotUtils.generateRandomColor();
		final var guildData = MatyBot.nbtDatabase().getDataForGuild(guild);
		final var leaderboard = guildData.getLeaderboardSorted();
		if (leaderboard.isEmpty()) {
			embed = new EmbedBuilder().setColor(randomColour).setAuthor(guild.getName(), null, guild.getIconUrl())
					.setDescription("The leaderboard is empty currently.").setTimestamp(Instant.now());
		} else {
			embed = new EmbedBuilder().setColor(randomColour)
					.setTitle("Leaderboard Page " + ((start / itemsPerPage) + 1))
					.setAuthor(guild.getName(), null, guild.getIconUrl()).setTimestamp(Instant.now());
		}

		for (int x = start; x < start + itemsPerPage; x++) {
			if (x >= leaderboard.size()) {
				break;
			}

			final var user = leaderboard.get(x);
			final var levelData = guildData.getLevelDataForUser(user);
			embed.addField("Rank: " + (x + 1),
					"User: <@" + user + "> Level: " + levelData.getXpInGuild(guild) + " | XP: " + levelData.getXp(),
					false);
		}

		return embed;
	}

}
