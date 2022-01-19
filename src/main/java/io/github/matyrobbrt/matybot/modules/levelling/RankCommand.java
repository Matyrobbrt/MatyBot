package io.github.matyrobbrt.matybot.modules.levelling;

import static io.github.matyrobbrt.matybot.util.ImageUtils.cutoutImageMiddle;
import static io.github.matyrobbrt.matybot.util.ImageUtils.drawUserAvatar;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.net.URL;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.List;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;

import io.github.matyrobbrt.matybot.MatyBot;
import io.github.matyrobbrt.matybot.api.annotation.RegisterSlashCommand;
import io.github.matyrobbrt.matybot.reimpl.BetterMember;
import io.github.matyrobbrt.matybot.reimpl.BetterMemberImpl;
import io.github.matyrobbrt.matybot.reimpl.MatyBotSlashCommand;
import io.github.matyrobbrt.matybot.util.database.dao.nbt.LevelData;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class RankCommand extends MatyBotSlashCommand {

	@RegisterSlashCommand
	private static final RankCommand CMD = new RankCommand();

	private static final char[] CHARS = new char[] {
			'k', 'm', 'b', 't'
	};

	private Font usedFont = null;

	private RankCommand() {
		guildOnly = false;
		name = "rank";
		help = "Get the rank card of yourself or another user.";
		options = List.of(
				new OptionData(OptionType.USER, "member",
						"Member to get the rank card for. Mutually exclusive with member_by_rank.", false),
				new OptionData(OptionType.INTEGER, "member_by_rank",
						"This will get the data of the member at the specified rank position. Mutually exclusive with member."));
		try {
			final var graphicsEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
			this.usedFont = Font
					.createFont(Font.TRUETYPE_FONT, MatyBot.class.getResourceAsStream("/fonts/code-new-roman.otf"))
					.deriveFont(12f);
			graphicsEnv.registerFont(this.usedFont);
		} catch (FontFormatException | IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Recursive implementation, invokes itself for each factor of a thousand,
	 * increasing the class on each invokation.
	 *
	 * @param  n         the number to format
	 * @param  iteration in fact this is the class from the array c
	 * @return           a String representing the number n formatted in a cool
	 *                   looking way.
	 */
	private static String xpFormat(final double n, final int iteration) {
		if (n < 1000)
			return String.valueOf(n);
		final double d = (long) n / 100 / 10.0;
		final boolean isRound = d * 10 % 10 == 0;// true if the decimal part is equal to 0 (then it's trimmed anyway)
		if (d < 1000) {
			return (d > 99.9 || isRound && d > 9.99 ? (int) d * 10 / 10 : d + "") + "" + CHARS[iteration];
		} else {
			return xpFormat(d, iteration + 1);
		}

	}

	@Override
	public void execute(final io.github.matyrobbrt.matybot.reimpl.SlashCommandEvent event) {
		if (!event.isFromGuild()) {
			event.deferReply(true).setContent("This command only works in guilds!").queue();
			return;
		}
		if (!LevellingModule.isLevellingEnabled(event.getGuild())) {
			event.deferReply().setContent("Levelling is not enabled on this server!").setEphemeral(true).queue();
			return;
		}

		final OptionMapping memberOption = event.getOption("member");
		final OptionMapping memberByRankOption = event.getOption("member_by_rank");
		BetterMember member = null;
		if (memberOption == null && memberByRankOption == null) {
			member = event.getMember();
		} else {
			if (memberByRankOption == null) {
				member = memberOption.getAsMember() == null ? event.getMember()
						: new BetterMemberImpl(memberOption.getAsMember());
			} else {
				final var orderedLb = event.getGuild().getData().getLeaderboardSorted();
				final int pos = (int) memberByRankOption.getAsDouble() - 1;
				if (pos >= orderedLb.size()) {
					member = event.getMember();
				} else {
					final var mmb = event.getGuild().getMemberById(orderedLb.get(pos));
					member = mmb == null ? event.getMember() : mmb;
				}
			}
		}

		event.deferReply().addFile(makeRankCard(member), "rank_card.png").mentionRepliedUser(false).queue();
	}

	@Nullable
	private File makeRankCard(final BetterMember member) {
		final var location = Paths.get("levels/cards/" + member.getIdLong() + ".png").toFile();
		final var guild = member.getGuild();
		final var guildData = member.getBetterGuild().getData();

		try {
			final LevelData levelData = member.getLevelData();
			final BufferedImage base = ImageIO.read(MatyBot.class.getResourceAsStream("/levels/background.png"));
			final BufferedImage outline = ImageIO.read(MatyBot.class.getResourceAsStream("/levels/outline.png"));
			final var rankCardBuffer = new BufferedImage(base.getWidth(), base.getHeight(),
					BufferedImage.TYPE_INT_ARGB);
			final var graphics = (Graphics2D) rankCardBuffer.getGraphics();
			graphics.addRenderingHints(
					new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			// graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
			// RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT);

			// Background
			BufferedImage background;
			if (levelData.getRankCard().getBackgroundImage().isBlank()) {
				final var bgBuf = new BufferedImage(base.getWidth(), base.getHeight(), BufferedImage.TYPE_INT_ARGB);
				final var bgGraphics = bgBuf.createGraphics();
				bgGraphics.setColor(levelData.getRankCard().getBackgroundColour());
				bgGraphics.fillRect(0, 0, base.getWidth(), base.getHeight());
				bgGraphics.dispose();
				background = bgBuf;
			} else {
				background = ImageIO.read(new URL(levelData.getRankCard().getBackgroundImage()));
			}

			final var bgBuffer = new BufferedImage(base.getWidth(), base.getHeight(), BufferedImage.TYPE_INT_ARGB);
			final var bgGraphics = bgBuffer.createGraphics();
			bgGraphics.setClip(new Rectangle2D.Float(0, 0, base.getWidth(), base.getHeight()));
			bgGraphics.drawImage(background, 0, 0, base.getWidth(), base.getHeight(), null);
			bgGraphics.dispose();
			graphics.drawImage(bgBuffer, 0, 0, base.getWidth(), base.getHeight(), null);

			// Outline
			BufferedImage outlineImg;
			if (levelData.getRankCard().getOutlineImage().isBlank()) {
				final var outBuf = new BufferedImage(outline.getWidth(), outline.getHeight(),
						BufferedImage.TYPE_INT_ARGB);
				final var outGraphics = outBuf.createGraphics();
				outGraphics.setColor(levelData.getRankCard().getOutlineColour());
				outGraphics.fillRect(0, 0, outline.getWidth(), outline.getHeight());
				outGraphics.dispose();
				outlineImg = outBuf;
			} else {
				outlineImg = ImageIO.read(new URL(levelData.getRankCard().getOutlineImage()));
			}

			outlineImg = cutoutImageMiddle(outlineImg, base.getWidth(), base.getHeight(), 20);

			final var outlineAlpha = levelData.getRankCard().getOutlineOpacity();
			final var alphaComp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, outlineAlpha);
			graphics.setComposite(alphaComp);
			graphics.drawImage(outlineImg, 0, 0, base.getWidth(), base.getHeight(), null);
			graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));

			// Name
			graphics.setStroke(new BasicStroke(3));
			graphics.setColor(levelData.getRankCard().getNameTextColour());

			var nameFontSize = 52f;
			if (member.getEffectiveName().length() > 12) {
				nameFontSize -= member.getEffectiveName().length() * 1.2f - 12;
			}
			graphics.setFont(this.usedFont.deriveFont(nameFontSize));

			graphics.drawString(member.getEffectiveName(), 250, 110);

			// Rank
			graphics.setColor(levelData.getRankCard().getRankTextColour());
			graphics.setFont(this.usedFont.deriveFont(35f));
			int rank = guildData.getLeaderboardSorted().indexOf(member.getIdLong());

			var xModifier = 0;
			if (rank >= 10) {
				xModifier += 15;
			}

			if (rank >= 100) {
				xModifier += 15;
			}

			if (rank >= 1000) {
				xModifier += 15;
			}

			if (rank >= 10000) {
				xModifier += 15;
			}

			if (rank < 0) {
				rank = 0;
			}

			graphics.drawString("Rank #" + (rank + 1), 690 - xModifier, 110);

			final var fontMetrics = graphics.getFontMetrics();
			final int textWidth = fontMetrics.stringWidth("Rank #" + (rank + 1));
			graphics.drawLine(250, 130 + fontMetrics.getDescent(), 690 - xModifier + textWidth,
					130 + fontMetrics.getDescent());

			// XP and Level
			int xp = levelData.getXp();
			int level = LevellingModule.getLevelForXP(xp, guild);
			int nextLevelXP = LevellingModule.getXPForLevel(level + 1, guild);
			xp -= LevellingModule.getXPForLevel(level, guild);
			nextLevelXP -= LevellingModule.getXPForLevel(level, guild);
			float xpPercent = (float) (xp * 100) / (float) nextLevelXP;
			final var decimalFormat = new DecimalFormat("#.#");
			decimalFormat.setRoundingMode(RoundingMode.CEILING);
			xpPercent = Float.parseFloat(decimalFormat.format(xpPercent));
			if (xp < 0) {
				xp = 0;
			}

			if (xpPercent < 0) {
				xpPercent = 0;
			}

			if (nextLevelXP < 0) {
				nextLevelXP = 0;
			}

			if (level < 0) {
				level = 0;
			}

			final var xpStr = xpFormat(xp, 0);
			final var levelStr = String.valueOf(level);
			final var nextLevelXPStr = xpFormat(nextLevelXP, 0);
			graphics.setColor(levelData.getRankCard().getLevelTextColour());
			graphics.drawString("Level " + levelStr, 250, 180);
			graphics.setFont(this.usedFont.deriveFont(25f));

			xModifier = 0;
			if (xpStr.length() > 2) {
				xModifier += 10;
			}

			if (xpStr.length() > 3 || nextLevelXPStr.length() > 3) {
				xModifier += 10;
			}

			if (xpStr.length() > 4 || nextLevelXPStr.length() > 4) {
				xModifier += 10;
			}

			graphics.setColor(levelData.getRankCard().getXpTextColour());
			graphics.drawString(xpStr + " / " + nextLevelXPStr, 670 - xModifier, 180);

			// XP Bar
			graphics.setColor(levelData.getRankCard().getXpOutlineColour());
			graphics.drawRoundRect(250, 200, 570, 40, 10, 10);

			graphics.setColor(levelData.getRankCard().getXpEmptyColour());
			graphics.fillRoundRect(250, 200, 570, 40, 10, 10);

			graphics.setColor(levelData.getRankCard().getXpFillColour());
			graphics.fillRoundRect(250, 200, (int) (570 * (xpPercent * 0.01f)), 40, 10, 10);

			graphics.setColor(levelData.getRankCard().getPercentTextColour());
			graphics.setFont(this.usedFont.deriveFont(30f));
			graphics.drawString(String.valueOf(xpPercent) + "%", 510, 230);

			// User Avatar
			final var userAvatar = drawUserAvatar(member, graphics, 60, 75, 128);

			graphics.setColor(levelData.getRankCard().getAvatarOutlineColour());
			graphics.drawOval(60, 75, userAvatar.getWidth(), userAvatar.getHeight());
			graphics.dispose();

			if (!location.exists()) {
				location.mkdirs();
			}
			ImageIO.write(rankCardBuffer, "png", location);
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return location;
	}
}
