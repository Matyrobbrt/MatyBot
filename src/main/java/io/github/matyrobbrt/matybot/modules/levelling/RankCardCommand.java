package io.github.matyrobbrt.matybot.modules.levelling;

import static io.github.matyrobbrt.matybot.util.database.dao.nbt.LevelData.DEFAULT_RANK_CARD;

import java.awt.Color;
import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.List;

import javax.imageio.ImageIO;

import com.jagrosh.jdautilities.command.CooldownScope;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;

import io.github.matyrobbrt.matybot.MatyBot;
import io.github.matyrobbrt.matybot.api.annotation.RegisterSlashCommand;
import io.github.matyrobbrt.matybot.util.BotUtils;
import io.github.matyrobbrt.matybot.util.database.dao.nbt.LevelData.RankCard;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class RankCardCommand extends SlashCommand {

	private static final Choice[] CHOICES = {
			makeChoice("background_colour"), makeChoice("outline_colour"), makeChoice("rank_text_colour"),
			makeChoice("level_text_colour"), makeChoice("xp_text_colour"), makeChoice("name_text_colour"),
			makeChoice("xp_outline_colour"), makeChoice("xp_empty_colour"), makeChoice("xp_fill_colour"),
			makeChoice("avatar_outline_colour"), makeChoice("percent_text_colour"), makeChoice("xp_text_colour"),
			makeChoice("outline_alpha"), makeChoice("background_image"), makeChoice("outline_image"),
			makeChoice("xp_outline_image"), makeChoice("xp_empty_image"), makeChoice("xp_fill_image"),
			makeChoice("avatar_outline_image"), makeChoice("randomize"), makeChoice("default")
	};

	private static Choice makeChoice(final String name) {
		return new Choice(name, name);
	}

	@RegisterSlashCommand
	private static final RankCardCommand CMD = new RankCardCommand();

	public RankCardCommand() {
		name = "rank-card";
		guildOnly = true;
		cooldown = 20;
		help = "Configures your rank card";
		cooldownScope = CooldownScope.USER_GUILD;
		options = List.of(new OptionData(OptionType.STRING, "key", "The rank card configuration option")
				.setRequired(false).addChoices(CHOICES),
				new OptionData(OptionType.STRING, "new_value", "The value to set"));
	}

	@Override
	protected void execute(SlashCommandEvent event) {
		if (!LevellingModule.isLevellingEnabled(event.getGuild())) {
			event.deferReply().setContent("Levelling is not enabled on this server!").setEphemeral(true).queue();
			return;
		}

		final OptionMapping keyOption = event.getOption("key");
		String key = "";
		if (keyOption != null) {
			key = keyOption.getAsString();
		}

		final var xp = MatyBot.nbtDatabase().getDataForGuild(event.getGuild()).getLevelDataForUser(event.getMember())
				.getXp();
		final var level = LevellingModule.getLevelForXP(xp, event.getGuild());

		if (key.isBlank()) {
			final var embed = new EmbedBuilder().setColor(Color.BLUE).setTimestamp(Instant.now())
					.setAuthor(event.getMember().getEffectiveName(), null, event.getMember().getEffectiveAvatarUrl());
			embed.setDescription("__Example Values__:\n" + "**colorAsHex**: FFAB15\n" + "**numberFrom0to255**: 187\n"
					+ "**fileOrURL**: https://media.discordapp.net/whateverImage.png");

			embed.addField("Background Color", "/rank-card background_colour colorAsHex", true);
			embed.addField("Outline Color", "/rank-card outline_colour colorAsHex", true);
			embed.addField("Rank Text Color", "/rank-card rank_text_colour colorAsHex", true);
			embed.addField("Level Text Color", "/rank-card level_text_colour colorAsHex", true);
			embed.addField("XP Bar Outline Color", "/rank-card xp_outline_colour colorAsHex", true);
			embed.addField("XP Bar Empty Color", "/rank-card xp_empty_colour colorAsHex", true);
			embed.addField("XP Bar Fill Color", "/rank-card xp_fill_colour colorAsHex", true);
			embed.addField("Avatar Outline Color", "/rank-card avatar_outline_colour colorAsHex", true);
			embed.addField("Percent Text Color", "/rank-card percent_text_colour colorAsHex", true);
			embed.addField("XP Text Color", "/rank-card xp_text_colour colorAsHex", true);
			embed.addField("Outline Opacity", "/rank-card outline_alpha numberFrom0to255", true);

			embed.addBlankField(false);

			// Premium
			embed.addField("Background Image (level 20+)", "/rank-card background_image fileOrURL", true);
			embed.addField("Outline Image (level 20+)", "/rank-card outline_image fileOrURL", true);
			embed.addField("XP Bar Outline Image (level 20+)", "/rank-card xp_outline_image fileOrURL", true);
			embed.addField("XP Bar Empty Image (level 20+)", "/rank-card xp_empty_image fileOrURL", true);
			embed.addField("XP Bar Fill Image (level 20+)", "/rank-card xp_fill_image fileOrURL", true);
			embed.addField("Avatar Outline Image (level 20+)", "/rank-card avatar_outline_image fileOrURL", true);

			embed.addField("Randomize Colors", "/rank-card randomize", false);

			event.deferReply().addEmbeds(embed.build())
					.queue(hook -> hook.editOriginal("The options for customizing your rank card are: ").queue());
		} else {
			String[] args = new String[] {
					key
			};
			if (event.getOption("new_value") != null) {
				args = new String[] {
						key, event.getOption("new_value").getAsString()
				};
			}
			final var member = event.getMember();
			final var card = MatyBot.nbtDatabase().getDataForGuild(event.getGuild()).getLevelDataForUser(member)
					.getRankCard();

			switch (key) {
			case "background_colour":
				if (args.length == 1) {
					event.deferReply(true).setContent("Your Rank Card's Background Colour is (Red: "
							+ card.getBackgroundColour().getRed() + ", Green: " + card.getBackgroundColour().getGreen()
							+ ", Blue: " + card.getBackgroundColour().getBlue() + ")").queue();
					return;
				}

				try {
					card.setBackgroundColour(Color.decode(args[1]));
				} catch (final NumberFormatException e) {
					card.setBackgroundColour(DEFAULT_RANK_CARD.getBackgroundColour());
				}

				event.deferReply(true).setContent("Your Rank Card's Background Colour has been changed to: (Red: "
						+ card.getBackgroundColour().getRed() + ", Green: " + card.getBackgroundColour().getGreen()
						+ ", Blue: " + card.getBackgroundColour().getBlue() + ")").queue();

				break;
			case "outline_colour":
				if (args.length == 1) {
					event.deferReply(true)
							.setContent("Your Rank Card's Outline Colour is (Red: " + card.getOutlineColour().getRed()
									+ ", Green: " + card.getOutlineColour().getGreen() + ", Blue: "
									+ card.getOutlineColour().getBlue() + ")")
							.queue();
					return;
				}

				try {
					card.setOutlineColour(Color.decode(args[1]));
				} catch (final NumberFormatException e) {
					card.setOutlineColour(DEFAULT_RANK_CARD.getOutlineColour());
				}

				event.deferReply(true)
						.setContent("Your Rank Card's Outline Colour has been changed to: (Red: "
								+ card.getOutlineColour().getRed() + ", Green: " + card.getOutlineColour().getGreen()
								+ ", Blue: " + card.getOutlineColour().getBlue() + ")")
						.queue();

				MatyBot.nbtDatabase().setDirtyAndSave();
				break;
			case "rank_text_colour":
				if (args.length == 1) {
					event.deferReply(true).setContent("Your Rank Card's Rank Text Colour is (Red: "
							+ card.getRankTextColour().getRed() + ", Green: " + card.getRankTextColour().getGreen()
							+ ", Blue: " + card.getRankTextColour().getBlue() + ")").queue();
					return;
				}

				try {
					card.setRankTextColour(Color.decode(args[1]));
				} catch (final NumberFormatException e) {
					card.setRankTextColour(DEFAULT_RANK_CARD.getRankTextColour());
				}

				event.deferReply(true)
						.setContent("Your Rank Card's Rank Text Colour has been changed to: (Red: "
								+ card.getRankTextColour().getRed() + ", Green: " + card.getRankTextColour().getGreen()
								+ ", Blue: " + card.getRankTextColour().getBlue() + ")")
						.queue();

				MatyBot.nbtDatabase().setDirtyAndSave();
				break;
			case "level_text_colour":
				if (args.length == 1) {
					event.deferReply(true).setContent("Your Rank Card's Level Text Colour is (Red: "
							+ card.getLevelTextColour().getRed() + ", Green: " + card.getLevelTextColour().getGreen()
							+ ", Blue: " + card.getLevelTextColour().getBlue() + ")").queue();
					return;
				}

				try {
					card.setLevelTextColour(Color.decode(args[1]));
				} catch (final NumberFormatException e) {
					card.setLevelTextColour(DEFAULT_RANK_CARD.getLevelTextColour());
				}

				event.deferReply(true).setContent("Your Rank Card's Level Text Colour has been changed to: (Red: "
						+ card.getLevelTextColour().getRed() + ", Green: " + card.getLevelTextColour().getGreen()
						+ ", Blue: " + card.getLevelTextColour().getBlue() + ")").queue();

				MatyBot.nbtDatabase().setDirtyAndSave();
				break;
			case "xp_outline_colour":
				if (args.length == 1) {
					event.deferReply(true).setContent("Your Rank Card's XP Bar Outline Colour is (Red: "
							+ card.getXpOutlineColour().getRed() + ", Green: " + card.getXpOutlineColour().getGreen()
							+ ", Blue: " + card.getXpOutlineColour().getBlue() + ")").queue();
					return;
				}

				try {
					card.setXpOutlineColour(Color.decode(args[1]));
				} catch (final NumberFormatException e) {
					card.setXpOutlineColour(DEFAULT_RANK_CARD.getXpOutlineColour());
				}

				event.deferReply(true).setContent("Your Rank Card's XP Bar Outline Colour has been changed to: (Red: "
						+ card.getXpOutlineColour().getRed() + ", Green: " + card.getXpOutlineColour().getGreen()
						+ ", Blue: " + card.getXpOutlineColour().getBlue() + ")").queue();

				MatyBot.nbtDatabase().setDirtyAndSave();
				break;
			case "xp_empty_colour":
				if (args.length == 1) {
					event.deferReply(true).setContent("Your Rank Card's XP Bar Empty Colour is (Red: "
							+ card.getXpEmptyColour().getRed() + ", Green: " + card.getXpEmptyColour().getGreen()
							+ ", Blue: " + card.getXpEmptyColour().getBlue() + ")").queue();
					return;
				}

				try {
					card.setXpEmptyColour(Color.decode(args[1]));
				} catch (final NumberFormatException e) {
					card.setXpEmptyColour(DEFAULT_RANK_CARD.getXpEmptyColour());
				}

				event.deferReply(true)
						.setContent("Your Rank Card's XP Bar Empty Colour has been changed to: (Red: "
								+ card.getXpEmptyColour().getRed() + ", Green: " + card.getXpEmptyColour().getGreen()
								+ ", Blue: " + card.getXpEmptyColour().getBlue() + ")")
						.queue();

				MatyBot.nbtDatabase().setDirtyAndSave();
				break;
			case "xp_fill_colour":
				if (args.length == 1) {
					event.deferReply(true)
							.setContent("Your Rank Card's XP Bar Fill Colour is (Red: "
									+ card.getXpFillColour().getRed() + ", Green: " + card.getXpFillColour().getGreen()
									+ ", Blue: " + card.getXpFillColour().getBlue() + ")")
							.queue();
					return;
				}

				try {
					card.setXpFillColour(Color.decode(args[1]));
				} catch (final NumberFormatException e) {
					card.setXpFillColour(DEFAULT_RANK_CARD.getXpEmptyColour());
				}

				event.deferReply(true)
						.setContent("Your Rank Card's XP Bar Fill Colour has been changed to: (Red: "
								+ card.getXpFillColour().getRed() + ", Green: " + card.getXpFillColour().getGreen()
								+ ", Blue: " + card.getXpFillColour().getBlue() + ")")
						.queue();

				MatyBot.nbtDatabase().setDirtyAndSave();
				break;
			case "avatar_outline_colour":
				if (args.length == 1) {
					event.deferReply(true)
							.setContent("Your Rank Card's Avatar Outline Colour is (Red: "
									+ card.getAvatarOutlineColour().getRed() + ", Green: "
									+ card.getAvatarOutlineColour().getGreen() + ", Blue: "
									+ card.getAvatarOutlineColour().getBlue() + ")")
							.queue();
					return;
				}

				try {
					card.setAvatarOutlineColour(Color.decode(args[1]));
				} catch (final NumberFormatException e) {
					card.setAvatarOutlineColour(DEFAULT_RANK_CARD.getAvatarOutlineColour());
				}

				event.deferReply(true)
						.setContent("Your Rank Card's Avatar Outline Colour has been changed to: (Red: "
								+ card.getAvatarOutlineColour().getRed() + ", Green: " + card.getAvatarOutlineColour()
								+ ", Blue: " + card.getAvatarOutlineColour().getBlue() + ")")
						.queue();

				MatyBot.nbtDatabase().setDirtyAndSave();
				break;
			case "percent_text_colour":
				if (args.length == 1) {
					event.deferReply(true)
							.setContent("Your Rank Card's Percent Text Colour is " + card.getPercentTextColour())
							.queue();
					return;
				}

				try {
					card.setPercentTextColour(Color.decode(args[1]));
				} catch (final NumberFormatException e) {
					card.setPercentTextColour(DEFAULT_RANK_CARD.getPercentTextColour());
				}

				event.deferReply(true).setContent("Your Rank Card's Percent Text Colour has been changed to: (Red: "
						+ card.getPercentTextColour().getRed() + ", Green: " + card.getPercentTextColour().getGreen()
						+ ", Blue: " + card.getPercentTextColour().getBlue() + ")").queue();

				MatyBot.nbtDatabase().setDirtyAndSave();
				break;
			case "xp_text_colour":
				if (args.length == 1) {
					event.deferReply(true).setContent("Your Rank Card's XP Text Colour is " + card.getXpTextColour())
							.queue();
					return;
				}

				try {
					card.setXpTextColour(Color.decode(args[1]));
				} catch (final NumberFormatException e) {
					card.setXpTextColour(DEFAULT_RANK_CARD.getXpTextColour());
				}

				event.deferReply(true)
						.setContent("Your Rank Card's XP Text Colour has been changed to: (Red: "
								+ card.getXpTextColour().getRed() + ", Green: " + card.getXpTextColour().getGreen()
								+ ", Blue: " + card.getXpTextColour().getBlue() + ")")
						.queue();

				MatyBot.nbtDatabase().setDirtyAndSave();
				break;
			case "name_text_colour":
				if (args.length == 1) {
					event.deferReply(true)
							.setContent("Your Rank Card's Name Text Colour is " + card.getNameTextColour()).queue();
					return;
				}

				try {
					card.setNameTextColour(Color.decode(args[1]));
				} catch (final NumberFormatException e) {
					card.setNameTextColour(DEFAULT_RANK_CARD.getNameTextColour());
				}

				event.deferReply(true)
						.setContent("Your Rank Card's Name Text Colour has been changed to: (Red: "
								+ card.getNameTextColour().getRed() + ", Green: " + card.getNameTextColour().getGreen()
								+ ", Blue: " + card.getNameTextColour().getBlue() + ")")
						.queue();

				MatyBot.nbtDatabase().setDirtyAndSave();
				break;
			case "outline_alpha":
				if (args.length == 1) {
					event.deferReply(true)
							.setContent("Your Rank Card's Outline Opacity is " + card.getOutlineOpacity() * 255)
							.queue();
					return;
				}

				var opacity = 0;
				try {
					opacity = Integer.parseInt(args[1]);
				} catch (final NumberFormatException e) {
					opacity = 255;
				}

				if (opacity < 0) {
					opacity = 0;
				}
				if (opacity > 255) {
					opacity = 255;
				}

				card.setOutlineOpacity(opacity / 255f);

				event.deferReply(true).setContent(
						"Your Rank Card's Outline Opacity has been changed to: " + card.getOutlineOpacity() * 255)
						.queue();

				MatyBot.nbtDatabase().setDirtyAndSave();
				break;
			case "background_image":
				if (level <= 20) {
					if (args.length == 1) {
						event.deferReply(true)
								.setContent(card.getBackgroundImage().isBlank() ? "You have no Background Image set!"
										: "Your Background Image is: <" + card.getBackgroundImage() + ">")
								.queue();
						return;
					}

					card.setBackgroundImage(grabImageURL(args[1]));
					event.deferReply(true).setContent("Your Rank Card's Background Image has been updated!").queue();
					MatyBot.nbtDatabase().setDirtyAndSave();
				} else {
					event.deferReply(true).setContent("You must be level 20+ in order to set this value.").queue();
				}
				break;
			case "outline_image":
				if (level >= 20) {
					if (args.length == 1) {
						event.deferReply(true)
								.setContent(card.getOutlineImage().isBlank() ? "You have no Outline Image set!"
										: "Your Outline Image is: <" + card.getOutlineImage() + ">")
								.queue();
						return;
					}

					card.setOutlineImage(grabImageURL(args[1]));
					event.deferReply(true).setContent("Your Rank Card's Outline Image has been updated!").queue();
					MatyBot.nbtDatabase().setDirtyAndSave();
				} else {
					event.deferReply(true).setContent("You must be level 20+ in order to set this value.").queue();
				}
				break;
			case "xp_outline_image":
				if (level >= 20) {
					if (args.length == 1) {
						event.deferReply(true)
								.setContent(card.getXpOutlineImage().isBlank() ? "You have no XP Bar Outline Image set!"
										: "Your XP Bar Outline Image is: <" + card.getXpOutlineImage() + ">")
								.queue();
						return;
					}

					card.setXpOutlineImage(grabImageURL(args[1]));
					event.deferReply(true).setContent("Your Rank Card's XP Bar Outline Image has been updated!")
							.queue();
					MatyBot.nbtDatabase().setDirtyAndSave();
				} else {
					event.deferReply(true).setContent("You must be level 20+ in order to set this value.").queue();
				}
				break;
			case "xp_empty_image":
				if (level >= 20) {
					if (args.length == 1) {
						event.deferReply(true)
								.setContent(card.getXpEmptyImage().isBlank() ? "You have no XP Bar Empty Image set!"
										: "Your XP Bar Empty Image is: <" + card.getXpEmptyImage() + ">")
								.queue();
						return;
					}

					card.setXpEmptyImage(grabImageURL(args[1]));
					event.deferReply(true).setContent("Your Rank Card's XP Bar Empty Image has been updated!").queue();
					MatyBot.nbtDatabase().setDirtyAndSave();
				} else {
					event.deferReply(true).setContent("You must be level 20+ in order to set this value.").queue();
				}
				break;
			case "xp_fill_image":
				if (level >= 20) {
					if (args.length == 1) {
						event.deferReply(true)
								.setContent(card.getXpFillImage().isBlank() ? "You have no XP Bar Fill Image set!"
										: "Your XP Bar Fill Image is: <" + card.getXpFillImage() + ">")
								.queue();
						return;
					}

					card.setXpFillImage(grabImageURL(args[1]));
					event.deferReply(true).setContent("Your Rank Card's XP Bar Fill Image has been updated!").queue();
					MatyBot.nbtDatabase().setDirtyAndSave();
				} else {
					event.deferReply(true).setContent("You must be level 20+ in order to set this value.").queue();
				}
				break;
			case "avatar_outline_image":
				if (level >= 20) {
					if (args.length == 1) {
						event.deferReply(true)
								.setContent(card.getOutlineImage().isBlank() ? "You have no Avatar Outline Image set!"
										: "Your Avatar Outline Image is: <" + card.getOutlineImage() + ">")
								.queue();
						return;
					}

					card.setAvatarOutlineImage(grabImageURL(args[1]));
					event.deferReply(true).setContent("Your Rank Card's Avatar Outline Image has been updated!")
							.queue();
					MatyBot.nbtDatabase().setDirtyAndSave();
				} else {
					event.deferReply(true).setContent("You must be a level 20+ in order to set this value.").queue();
				}
				break;
			case "randomize":
				card.setBackgroundColour(BotUtils.generateRandomColor());
				card.setOutlineColour(BotUtils.generateRandomColor());
				card.setRankTextColour(BotUtils.generateRandomColor());
				card.setLevelTextColour(BotUtils.generateRandomColor());
				card.setXpOutlineColour(BotUtils.generateRandomColor());
				card.setXpEmptyColour(BotUtils.generateRandomColor());
				card.setXpFillColour(BotUtils.generateRandomColor());
				card.setAvatarOutlineColour(BotUtils.generateRandomColor());
				card.setPercentTextColour(BotUtils.generateRandomColor());
				card.setXpTextColour(BotUtils.generateRandomColor());

				event.deferReply(true).setContent("Your Rank Card's colours have been updated!").queue();
				MatyBot.nbtDatabase().setDirtyAndSave();
				break;
			case "default":
				MatyBot.nbtDatabase().getDataForGuild(event.getGuild()).getLevelDataForUser(event.getMember())
						.setRankCard(new RankCard());
				event.deferReply(true).setContent("Your Rank Card has been set to the default value!")
						.setEphemeral(true).queue();
				MatyBot.nbtDatabase().setDirty();
				break;
			default:
				event.deferReply(true).setContent(
						"You must supply a valid setting to change. Use `" + "/rank-card` for more information!")
						.queue();
				break;
			}
		}

	}

	public static String grabImageURL(final String txt) {
		var imageStr = "";
		try {
			if (ImageIO.read(new URL(txt)) != null) {
				imageStr = txt;
			}
		} catch (final IOException e) {
			imageStr = "";
		}

		return imageStr;
	}

}
