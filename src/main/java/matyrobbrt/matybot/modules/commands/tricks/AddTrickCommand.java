package matyrobbrt.matybot.modules.commands.tricks;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;

import matyrobbrt.matybot.MatyBot;
import matyrobbrt.matybot.annotation.RegisterCommand;
import matyrobbrt.matybot.annotation.RegisterSlashCommand;
import matyrobbrt.matybot.tricks.ITrick.TrickType;
import matyrobbrt.matybot.tricks.TrickManager;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

/**
 * Double command (both slash and normal)
 * 
 * @author matyrobbrt
 *
 */
public final class AddTrickCommand extends SlashCommand {

	@RegisterCommand
	@RegisterSlashCommand
	private static final AddTrickCommand CMD = new AddTrickCommand();

	public AddTrickCommand() {
		name = "addtrick";
		help = "Adds a new trick, either a string or an embed, if a string you only need the <names> and <body>.";
		category = new Category("Info");
		arguments = "(<string> <trick content body> (or) <embed> <title> " + "<description> <colour-as-hex-code>";
		aliases = new String[] {
				"add-trick"
		};
		enabledRoles = MatyBot.config().trickManagerRoles.stream().map(String::valueOf).toArray(String[]::new);
		guildOnly = true;

		// used by the non-slash version
		// guildId = Long.toString(MatyBot.config().getGuildID());

		children = TrickManager.getTrickTypes().entrySet().stream()
				.map(entry -> new TypeSubCmd(entry.getKey(), entry.getValue())).toArray(SlashCommand[]::new);
	}

	private static class TypeSubCmd extends SlashCommand {

		private final TrickType<?> trickType;

		public TypeSubCmd(String name, TrickType<?> trickType) {
			this.trickType = trickType;
			this.name = name;
			this.help = "Create a trick of the type " + name;
			this.guildOnly = true;
			this.options = trickType.getArgs();
		}

		@Override
		protected void execute(final SlashCommandEvent event) {
			try {
				TrickManager.addTrick(trickType.createFromCommand(event));
				event.reply("Added trick!").mentionRepliedUser(false).setEphemeral(true).queue();
			} catch (IllegalArgumentException e) {
				event.reply("A command with that name already exists!").mentionRepliedUser(false).setEphemeral(true)
						.queue();
				MatyBot.LOGGER.warn("Failure adding trick: {}", e.getMessage());
			}
		}
	}

	@Override
	protected void execute(SlashCommandEvent event) {
		// Use the children types
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void execute(CommandEvent event) {
		final var channel = event.getMessage();
		var args = event.getArgs();
		var firstSpace = args.indexOf(" ");

		try {
			TrickManager.addTrick(TrickManager.getTrickType(args.substring(0, firstSpace))
					.createFromArgs(args.substring(firstSpace + 1)));
			channel.reply("Added trick!").mentionRepliedUser(false).queue();
		} catch (IllegalArgumentException e) {
			channel.reply("A command with that name already exists!").mentionRepliedUser(false).queue();
			MatyBot.LOGGER.warn("Failure adding trick: {}", e.getMessage());
		}
	}

}
