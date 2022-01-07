package matyrobbrt.matybot.tricks;

import static matyrobbrt.matybot.util.BotUtils.getArgumentOrEmpty;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.jagrosh.jdautilities.command.SlashCommandEvent;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class EmbedTrick implements ITrick {

    private final List<String> names;

    private final String title;

    private final String description;

    private final int color;

    private final List<MessageEmbed.Field> fields;

    public EmbedTrick(final List<String> names, final String title, final String description, final int color,
                      final MessageEmbed.Field... fields) {
        this.names = names;
        this.title = title;
        this.description = description;
        this.color = color;
        this.fields = Arrays.asList(fields);
    }

    @Override
    public List<String> getNames() {
        return names;
    }

    @Override
    public Message getMessage(final String[] args) {
        EmbedBuilder builder = new EmbedBuilder()
            .setTitle(getTitle())
            .setDescription(getDescription())
            .setColor(color);
        for (MessageEmbed.Field field : getFields()) {
            builder.addField(field);
        }
        return new MessageBuilder(builder.build()).setAllowedMentions(Set.of(Message.MentionType.CHANNEL, Message.MentionType.EMOTE)).build();
    }

    public String getDescription() {
        return description;
    }

    public String getTitle() {
        return title;
    }

    public int getColor() {
        return color;
    }

    public List<MessageEmbed.Field> getFields() {
        return fields;
    }

    static class Type implements TrickType<EmbedTrick> {

        @Override
        public Class<EmbedTrick> getTrickClass() {
            return EmbedTrick.class;
        }

        @Override
        public EmbedTrick createFromArgs(final String args) {
            String[] argsArray = args.split(" \\| ");
            return new EmbedTrick(
                Arrays.asList(argsArray[0].split(" ")),
                argsArray[1],
                argsArray[2],
                Integer.parseInt(argsArray[3].replace("#", ""), 16)
            );
        }

        @Override
        public List<OptionData> getArgs() {
            return List.of(
                new OptionData(OptionType.STRING, "names", "Name(s) for the trick. Separate with spaces.").setRequired(true),
                new OptionData(OptionType.STRING, "title", "Title of the embed.").setRequired(true),
                new OptionData(OptionType.STRING, "description", "Description of the embed.").setRequired(true),
                new OptionData(OptionType.STRING, "color", "Hex color string in #AABBCC format, used for the embed.").setRequired(true)
            );
        }

        @Override
        public EmbedTrick createFromCommand(final SlashCommandEvent event) {
            return new EmbedTrick(
					Arrays.asList(getArgumentOrEmpty(event, "names").split(" ")), getArgumentOrEmpty(event, "title"),
					getArgumentOrEmpty(event, "description"),
					Integer.parseInt(getArgumentOrEmpty(event, "color").replaceAll("#", ""), 16)
            );
        }
    }
}
