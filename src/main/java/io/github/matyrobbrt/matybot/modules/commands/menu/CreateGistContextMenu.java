package io.github.matyrobbrt.matybot.modules.commands.menu;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import io.github.matyrobbrt.matybot.MatyBot;
import io.github.matyrobbrt.matybot.util.BotUtils;
import io.github.matyrobbrt.matybot.util.DiscordUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command.Type;
import ssynx.gist.BetterGist;
import ssynx.gist.GistUtils;
import ssynx.gist.JaGistException;

public class CreateGistContextMenu extends io.github.matyrobbrt.matybot.api.command.slash.ContextMenu {

	private static final Random RAND = new Random();

	public CreateGistContextMenu() {
		name = "Gist";
		type = Type.MESSAGE;
	}

	@Override
	public void onMessageContextInteraction(MessageContextInteractionEvent event) {
		if (!GistUtils.hasToken()) {
			event.deferReply(true).setContent("I cannot create a gist! I have not been configured to do so.");
		}
		new Thread(() -> run(BotUtils.getGithubToken(), event)).start();
	}

	private static void run(final String token, final MessageContextInteractionEvent event) {
		if (event.getTarget().getAttachments().isEmpty()) {
			event.deferReply(true).setContent("The message doesn't have any attachements!").queue();
			return;
		}
		event.deferReply().queue(hook -> {
			try {
				final var gist = GistUtils.create(token, createGistFromMessage(event.getTarget()));
				final EmbedBuilder embed = new EmbedBuilder().setColor(Color.MAGENTA).setTimestamp(Instant.now())
						.setFooter("Requester ID: " + event.getMember().getIdLong(),
								event.getMember().getEffectiveAvatarUrl())
						.setThumbnail(event.getJDA().getSelfUser().getAvatarUrl())
						.setDescription("A gist has been created for the attachements of [this](%s) message."
								.formatted(DiscordUtils.createMessageLink(event.getTarget())))
						.addField("Gist Link", gist.getHtmlUrl(), false);
				hook.editOriginalEmbeds(embed.build()).queue();
			} catch (InterruptedException | ExecutionException | JaGistException e) {
				hook.editOriginal(String.format("Error while creating gist: **%s**", e.getLocalizedMessage()));
				MatyBot.LOGGER.error("Error while creating gist", e);
			}
		});
	}

	public static String generateName(int length) {
		int leftLimit = 48; // numeral '0'
		int rightLimit = 122; // letter 'z'
		return RAND.ints(leftLimit, rightLimit + 1).filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
				.limit(length).collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
				.toString();
	}

	private static String readInputStream(InputStream is) throws IOException {
		StringBuilder content = new StringBuilder();
		InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
		try (BufferedReader buffer = new BufferedReader(reader)) {
			String line;
			while ((line = buffer.readLine()) != null)
				content.append(line).append('\n');
		} finally {
			is.close();
			reader.close();
		}
		return content.toString();
	}

	public static BetterGist createGistFromMessage(final net.dv8tion.jda.api.entities.Message message)
			throws InterruptedException, ExecutionException {
		final var gist = new BetterGist(message.getContentRaw(), false);
		for (var attach : message.getAttachments()) {
			attach.retrieveInputStream().thenAccept(is -> {
				final String fileName = generateName(10) + "." + attach.getFileExtension();
				try {
					gist.addFile(fileName, readInputStream(is));
				} catch (IOException e) {
					MatyBot.LOGGER.error("Error while reading file for creating a gist", e);
				}
			}).get();
		}
		return gist;
	}
}
