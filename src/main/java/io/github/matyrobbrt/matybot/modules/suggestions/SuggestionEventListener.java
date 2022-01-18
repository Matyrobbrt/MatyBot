package io.github.matyrobbrt.matybot.modules.suggestions;

import java.awt.Color;
import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import io.github.matyrobbrt.matybot.MatyBot;
import io.github.matyrobbrt.matybot.util.Constants;
import io.github.matyrobbrt.matybot.util.DiscordUtils;
import io.github.matyrobbrt.matybot.util.database.dao.nbt.SuggestionData.SuggestionStatus;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.ThreadChannel.AutoArchiveDuration;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.MarkdownUtil;

public class SuggestionEventListener extends ListenerAdapter {

	@Override
	public void onButtonInteraction(ButtonInteractionEvent event) {
		if (!SuggestionsModule.areSuggestionsEnabled(event.getGuild())) { return; }
		final var buttonId = event.getButton().getId();
		if (buttonId.startsWith("approve_suggestion_")) {
			executeIfPerms(event, () -> {
				try {
					final var msgId = Long.parseLong(buttonId.replace("approve_suggestion_", ""));
					approveSuggestion(msgId, event);
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			});
		}
		if (buttonId.startsWith("consider_suggestion_")) {
			executeIfPerms(event, () -> {
				try {
					final var msgId = Long.parseLong(buttonId.replace("consider_suggestion_", ""));
					considerSuggestion(msgId, event);
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			});
		}
		if (buttonId.startsWith("deny_suggestion_")) {
			executeIfPerms(event, () -> {
				try {
					final var msgId = Long.parseLong(buttonId.replace("deny_suggestion_", ""));
					denySuggestion(msgId, event);
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			});
		}
	}

	private static void executeIfPerms(final ButtonInteractionEvent event, final Runnable ifPerms) {
		if (!SuggestionsModule.memberCanApproveSuggestion(event.getMember())) {
			event.deferReply(true).setContent("You do not have the required permissions for this action!").queue();
		} else {
			ifPerms.run();
		}
	}

	private static void denySuggestion(final long messageId, final ButtonInteractionEvent event) {
		final var suggestionData = MatyBot.nbtDatabase().getDataForGuild(event.getGuild()).getSuggestions()
				.get(messageId);
		if (suggestionData == null) { return; }
		MatyBot.getInstance().getChannelIfPresent(suggestionData.getChannelId(), suggestionChannel -> {
			suggestionChannel.retrieveMessageById(messageId).queue(suggestionMessage -> {
				if (suggestionMessage == null) { return; }
				event.deferReply(true).setContent("Please check your DMs for continuing this action!").queue(hook -> {
					MatyBot.getInstance().openDM(event.getMember().getIdLong(), dm -> {
						Message dmMessage;
						try {
							dmMessage = dm.sendMessageEmbeds(new EmbedBuilder().setDescription(
									"You want to deny %s suggestion. Please provide a reason for the denial by replying to this message with said reason. You have 14 minutes."
											.formatted(MarkdownUtil.maskedLink("this",
													DiscordUtils.createMessageLink(suggestionMessage))))
									.build()).submit().get();
						} catch (InterruptedException | ExecutionException e1) {
							e1.printStackTrace();
							return;
						}
						Constants.EVENT_WAITER.waitForEvent(MessageReceivedEvent.class,
								e -> !e.isFromGuild() && e.getAuthor().getIdLong() == event.getMember().getIdLong()
										&& e.getMessage().getMessageReference() != null && e.getMessage()
												.getMessageReference().getMessageIdLong() == dmMessage.getIdLong(),
								e -> {
									final var reason = e.getMessage().getContentRaw();
									suggestionData.setStatus(SuggestionStatus.DENIED);
									suggestionData.setDenialReason(reason);
									suggestionMessage.editMessage("** **")
											.setEmbeds(new EmbedBuilder(suggestionMessage.getEmbeds().get(0))
													.addField("Denied by", event.getMember().getUser().getAsTag(), true)
													.addField("Denial reason", reason, false)
													.setTitle("Suggestion denied").setColor(Color.RED).build())
											.setActionRows().queue();
									hook.editOriginal("Action succesful!").queue();
									dm.sendMessage("Thank you! The suggestion was succesfully denied.").queue();
									final var thread = event.getGuild().getThreadChannelById(messageId);
									thread.addThreadMember(event.getMember()).queue($ -> {
										thread.sendMessageEmbeds(new EmbedBuilder().setDescription(
												"The suggestion linked to this thread has been denied by %s. They have been added in this thread as any discussion about the denial should happen here."
														.formatted(event.getMember().getAsMention()))
												.setColor(Color.RED).build()).queue();
										thread.getManager().setAutoArchiveDuration(AutoArchiveDuration.TIME_24_HOURS)
												.queue();
									});
									MatyBot.getInstance().openDM(suggestionData.getOwnerId(), ownerDm -> {
										ownerDm.sendMessageEmbeds(new EmbedBuilder().setTitle("Suggestion denied")
												.setAuthor(event.getGuild().getName(), null,
														event.getGuild().getIconUrl())
												.setDescription(
														"One of your suggestions in %s has been denied by %s. %s"
																.formatted(event.getGuild().getName(),
																		event.getMember().getUser().getAsTag(),
																		MarkdownUtil.maskedLink("Jump to the message.",
																				DiscordUtils.createMessageLink(
																						suggestionMessage))))
												.addField("Denial reason", reason, false).setColor(Color.RED)
												.setTimestamp(Instant.now()).build()).queue();
									});
								}, 14, TimeUnit.MINUTES, () -> {
									hook.editOriginal("You did not provide a reason! We will not complete the denial.")
											.queue();
									dm.sendMessage("You did not provide a reason! We will not complete the denial.")
											.reference(dmMessage).queue();
								});
					}, () -> hook.editOriginal("We could not DM you! You cannot finish this action.").queue());
				});
			});
		});
	}

	private static void considerSuggestion(final long messageId, final ButtonInteractionEvent event) {
		final var suggestionData = MatyBot.nbtDatabase().getDataForGuild(event.getGuild()).getSuggestions()
				.get(messageId);
		if (suggestionData == null) { return; }
		MatyBot.getInstance().getChannelIfPresent(suggestionData.getChannelId(), suggestionChannel -> {
			suggestionChannel.retrieveMessageById(messageId).queue(suggestionMessage -> {
				if (suggestionMessage != null) {
					suggestionData.setStatus(SuggestionStatus.CONSIDERED);
					suggestionMessage.editMessage("** **")
							.setEmbeds(new EmbedBuilder(suggestionMessage.getEmbeds().get(0))
									.addField("Taken into consideration by", event.getMember().getUser().getAsTag(),
											true)
									.setTitle("Suggestion taken into consideration").setColor(Color.YELLOW).build())
							.setActionRows().queue();
					final var thread = event.getGuild().getThreadChannelById(messageId);
					thread.sendMessageEmbeds(new EmbedBuilder().setColor(Color.YELLOW).setDescription(
							"The suggestion linked to this thread has been taken into consideration, and such, this thread will now be archived.")
							.build()).queue(s -> thread.getManager().setArchived(true).queue());
					event.reply("Action succesful!").setEphemeral(true).queue();
					MatyBot.getInstance().openDM(suggestionData.getOwnerId(), dm -> {
						dm.sendMessageEmbeds(new EmbedBuilder().setTitle("Suggestion taken into consideration")
								.setAuthor(event.getGuild().getName(), null, event.getGuild().getIconUrl())
								.setDescription(
										"One of your suggestions in %s has been taken into consideration by %s. %s"
												.formatted(event.getGuild().getName(),
														event.getMember().getUser().getAsTag(),
														MarkdownUtil.maskedLink("Jump to the message.",
																DiscordUtils.createMessageLink(suggestionMessage))))
								.setColor(Color.YELLOW).setTimestamp(Instant.now()).build()).queue();
					});
				}
			});
		});
	}

	private static void approveSuggestion(final long messageId, final ButtonInteractionEvent event) {
		final var suggestionData = MatyBot.nbtDatabase().getDataForGuild(event.getGuild()).getSuggestions()
				.get(messageId);
		if (suggestionData == null) { return; }
		MatyBot.getInstance().getChannelIfPresent(suggestionData.getChannelId(), suggestionChannel -> {
			suggestionChannel.retrieveMessageById(messageId).queue(suggestionMessage -> {
				if (suggestionMessage != null) {
					suggestionData.setStatus(SuggestionStatus.APPROVED);
					MatyBot.nbtDatabase().setDirtyAndSave();
					suggestionMessage.editMessage("** **")
							.setEmbeds(new EmbedBuilder(suggestionMessage.getEmbeds().get(0))
									.setTitle("Suggestion approved")
									.addField("Approved by", event.getMember().getUser().getAsTag(), true)
									.setColor(Color.GREEN).build())
							.setActionRows().queue();
					final var thread = event.getGuild().getThreadChannelById(messageId);
					thread.sendMessageEmbeds(new EmbedBuilder().setColor(Color.GREEN).setDescription(
							"The suggestion linked to this thread has been approved, and such, this thread will now be archived.")
							.build()).queue(s -> thread.getManager().setArchived(true).queue());
					event.reply("Approval succesful!").setEphemeral(true).queue();
					MatyBot.getInstance().openDM(suggestionData.getOwnerId(), dm -> {
						dm.sendMessageEmbeds(new EmbedBuilder().setTitle("Suggestion approved")
								.setAuthor(event.getGuild().getName(), null, event.getGuild().getIconUrl())
								.setDescription("One of your suggestions in %s has been approved by %s. %s".formatted(
										event.getGuild().getName(), event.getMember().getUser().getAsTag(),
										MarkdownUtil.maskedLink("Jump to the message.",
												DiscordUtils.createMessageLink(suggestionMessage))))
								.setColor(Color.GREEN).setTimestamp(Instant.now()).build()).queue();
					});
				}
			});
		});
	}

}
