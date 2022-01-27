package io.github.matyrobbrt.matybot.managers;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import io.github.matyrobbrt.javanbt.nbt.CompoundNBT;
import io.github.matyrobbrt.javanbt.serialization.Deserializer;
import io.github.matyrobbrt.javanbt.serialization.NBTSerializable;
import io.github.matyrobbrt.javanbt.serialization.Serializers;
import io.github.matyrobbrt.matybot.MatyBot;
import io.github.matyrobbrt.matybot.util.BotUtils;
import io.github.matyrobbrt.matybot.util.DiscordUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public final class CustomPingManager extends ListenerAdapter {

	private final Executor executor = Executors.newSingleThreadExecutor(r -> {
		final var thread = new Thread(r, "CustomPingChecker");
		thread.setDaemon(true);
		return thread;
	});

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if (event.isFromGuild() && !event.getAuthor().isBot() && !event.getAuthor().isSystem()) {
			executor.execute(() -> {
				try {
					tryPing(event);
				} catch (Exception e) {
					MatyBot.LOGGER.error("Exception while trying to use custom pings!", e);
				}
			});
		}
	}

	private static void tryPing(MessageReceivedEvent event) {
		final var customPings = MatyBot.nbtDatabase().getDataForGuild(event.getGuild()).getAllCustomPings();
		final var rawMessage = event.getMessage().getContentRaw();
		customPings.forEach((member, pings) -> {
			if (member == event.getMember().getIdLong()) { return; }
			if (event.getGuild().getMemberById(member) == null) {
				customPings.remove(member);
				MatyBot.nbtDatabase().setDirty();
				MatyBot.LOGGER.debug("Removing custom pings for {} in guild {} as they left the guild!", member,
						event.getGuild());
				return;
			}
			pings.forEach(ping -> {
				final var matcher = ping.pattern().matcher(rawMessage);
				if (matcher.matches()) {
					MatyBot.getInstance().openDM(member, dm -> sendPingMessage(dm, event.getMember(),
							event.getMessage(), event.getGuild(), ping.text()), () -> {
								MatyBot.LOGGER.debug("Removing custom pings for {} in guild {} as I cannnot DM them!",
										member, event.getGuild());
								customPings.remove(member);
								MatyBot.nbtDatabase().setDirty();
							});
				}
			});
		});
	}

	private static void sendPingMessage(PrivateChannel dm, Member author, Message original, Guild from,
			String pingText) {
		final var embed = new EmbedBuilder()
				.setAuthor("New ping from: " + author.getEffectiveName(), author.getAvatarUrl(), null)
				.addField(pingText, original.getContentRaw().isEmpty() ? "[Embed]" : original.getContentRaw(), false)
				.addField("Link", DiscordUtils.createMessageLink(original), false)
				.setColor(BotUtils.generateRandomColor());
		dm.sendMessageEmbeds(embed.build()).queue();
	}

	public record CustomPing(Pattern pattern, String text) implements NBTSerializable<CompoundNBT> {

		public CustomPing(String pattern, String text) {
			this(Pattern.compile(pattern), text);
		}

		public static final Deserializer<CompoundNBT, CustomPing> DESERIALIZER = Serializers.registerDeserializer(
				CustomPing.class, nbt -> new CustomPing(nbt.getString("pattern"), nbt.getString("text")));

		@Override
		public CompoundNBT serializeNBT() {
			return io.github.matyrobbrt.matybot.util.nbt.NBTBuilder.of().putString("pattern", pattern.pattern())
					.putString("text", text).build();
		}

		@Override
		public void deserializeNBT(CompoundNBT nbt) {
		}
	}

}
