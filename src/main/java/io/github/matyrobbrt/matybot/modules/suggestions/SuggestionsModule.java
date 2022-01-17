package io.github.matyrobbrt.matybot.modules.suggestions;

import java.util.function.Consumer;

import io.github.matyrobbrt.jdautils.event.EventListenerWrapper;
import io.github.matyrobbrt.matybot.MatyBot;
import io.github.matyrobbrt.matybot.util.BotUtils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

public class SuggestionsModule extends io.github.matyrobbrt.jdautils.modules.Module {

	public SuggestionsModule(JDA bot) {
		super(() -> true, bot);
	}

	public static boolean areSuggestionsEnabled(final Guild guild) {
		return MatyBot.getConfigForGuild(guild).areSuggestionsEnabled();
	}

	public static boolean memberCanApproveSuggestion(final Member member) {
		return BotUtils.memberHasRole(member, MatyBot.getConfigForGuild(member.getGuild()).suggestionApproverRoles);
	}

	public static void getSuggestionsChannel(final Guild guild, final Consumer<TextChannel> channel) {
		MatyBot.getInstance().getChannelIfPresent(MatyBot.getConfigForGuild(guild).suggestionsChannel, channel);
	}

	@Override
	public void register() {
		super.register();
		bot.addEventListener(new EventListenerWrapper(new SuggestionEventListener()));
	}

}
