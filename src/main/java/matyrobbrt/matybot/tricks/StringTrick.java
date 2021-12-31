package matyrobbrt.matybot.tricks;

import static matyrobbrt.matybot.util.BotUtils.getArgumentOrEmpty;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class StringTrick implements ITrick {

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
        return new MessageBuilder(String.format(getBody(), (Object[]) args)).setAllowedMentions(Set.of(Message.MentionType.CHANNEL, Message.MentionType.EMOTE)).build();
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
                new OptionData(OptionType.STRING, "names", "Name(s) for the trick. Separate with spaces.").setRequired(true),
                new OptionData(OptionType.STRING, "content", "The content of the trick.").setRequired(true)
            );
        }

        @Override
        public StringTrick createFromCommand(final SlashCommandEvent event) {
			return new StringTrick(Arrays.asList(getArgumentOrEmpty(event, "names").split(" ")),
					getArgumentOrEmpty(event, "content"));
        }
    }
}
