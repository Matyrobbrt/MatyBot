package io.github.matyrobbrt.matybot.managers.tricks.commands;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;

import io.github.matyrobbrt.matybot.MatyBot;
import io.github.matyrobbrt.matybot.api.annotation.RegisterCommand;
import io.github.matyrobbrt.matybot.api.annotation.RegisterSlashCommand;
import io.github.matyrobbrt.matybot.api.command.slash.GuildSpecificSlashCommand;
import io.github.matyrobbrt.matybot.managers.tricks.ITrick.TrickType;
import io.github.matyrobbrt.matybot.managers.tricks.TrickManager;
import io.github.matyrobbrt.matybot.util.Constants;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

public final class AddTrickCommand extends GuildSpecificSlashCommand {

	@RegisterSlashCommand
	private static final AddTrickCommand CMD = new AddTrickCommand();

	@RegisterCommand
	private static final PrefixCmd PREFIX_CMD = new PrefixCmd();

	public AddTrickCommand() {
		super("");
		name = "add-trick";
		help = "Adds a new trick, either a string or an embed, if a string you only need the <names> and <body>.";
		category = new Category("Info");
		arguments = "(<string> <trick content body> (or) <embed> <title> " + "<description> <colour-as-hex-code>";
		enabledRolesGetter = guildId -> MatyBot.getConfigForGuild(guildId).trickManagerRoles.stream()
				.map(String::valueOf).toArray(String[]::new);

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
				TrickManager.addTrick(event.getGuild().getIdLong(), trickType.createFromCommand(event));
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

	private static class PrefixCmd extends Command {

		public PrefixCmd() {
			name = "add-trick";
			help = "Adds a new trick, either a string or an embed, if a string you only need the <names> and <body>.";
			category = new Category("Info");
			arguments = "(<string> <trick content body> (or) <embed> <title> " + "<description> <colour-as-hex-code>";
			aliases = new String[] {
					"addtrick"
			};
		}

		@SuppressWarnings({
				"deprecation", "unused"
		})
		@Override
		protected void execute(CommandEvent event) {
			final var msg = event.getMessage();

			if (!hasPerms(event.getMember())) {
				msg.reply("You do not have the permissions needed in order to use this command!").queue();
				return;
			}

			try {
				var args = event.getArgs();
				var firstSpace = args.indexOf(" ");

				if (false) {
					executeInteractive(event);
					return;
				}

				TrickManager.addTrick(event.getGuild().getIdLong(), TrickManager
						.getTrickType(args.substring(0, firstSpace)).createFromArgs(args.substring(firstSpace + 1)));
				msg.reply("Added trick!").mentionRepliedUser(false).queue();
			} catch (Exception e) {
				msg.reply("There was an exception while executing the command: **%s**!"
						.formatted(e.getLocalizedMessage())).mentionRepliedUser(false).queue();
				MatyBot.LOGGER.warn("Failure adding trick: {}", e.getMessage());
			}
		}

		private static void executeInteractive(final CommandEvent event) {
			sendMessageAndWait(
					event.getMessage().reply("Please provide the name(s) of the trick, separated by spaces."),
					namesMsg -> {
						final var names = namesMsg.getMessage().getContentRaw().split(" ");
						sendMessageAndWait(
								event.getMessage().reply("Please send the type of the trick. (`embed` or `string`)"),
								typeMsg -> {
									if (typeMsg.getMessage().getContentRaw().contentEquals("embed")) {
										embedInteractive(typeMsg, names);
									} else {
										typeMsg.getMessage().reply("Invalid type!").queue();
									}
								});
					});
		}

		private static void embedInteractive(final MessageReceivedEvent msgReceived, final String[] names) {
			sendMessageAndWait(msgReceived.getMessage().reply("Please provide the title of the embed."), titleMsg -> {
				@SuppressWarnings("unused")
				final var title = titleMsg.getMessage().getContentRaw();
			});
		}

		private static void sendMessageAndWait(final MessageAction action, Consumer<MessageReceivedEvent> onReceived) {
			action.queue(msg -> {
				Constants.EVENT_WAITER.waitForEvent(MessageReceivedEvent.class,
						e -> e.isFromGuild() && e.getMessage().getMessageReference() != null
								&& e.getMessage().getMessageReference().getMessageIdLong() == msg.getIdLong(),
						onReceived, 10, TimeUnit.MINUTES,
						() -> msg.reply("As 10 minutes have passed, this action is now cancelled!").queue());
			});
		}

		private static boolean hasPerms(final net.dv8tion.jda.api.entities.Member member) {
			return member.getRoles().stream().anyMatch(
					r -> MatyBot.getConfigForGuild(member.getGuild()).trickManagerRoles.contains(r.getIdLong()));
		}

	}

}
