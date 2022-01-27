package io.github.matyrobbrt.matybot.util;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.Transparency;
import java.awt.font.LineMetrics;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

@UtilityClass
public final class ImageUtils {

	/**
	 * Draws the avatar of an user
	 * 
	 * @param  userAvatar the avatar to render
	 * @param  graphics   the to render on
	 * @param  x          the x position where the avatar will be renderer
	 * @param  y          the y position where the avatar will be renderer
	 * @return            the {@code userAvatar} after it was renderer
	 */
	public static BufferedImage drawUserAvatar(final BufferedImage userAvatar, final Graphics2D graphics, final int x,
			final int y) {
		graphics.setStroke(new BasicStroke(4));
		final var circleBuffer = new BufferedImage(userAvatar.getWidth(), userAvatar.getHeight(),
				BufferedImage.TYPE_INT_ARGB);
		final var avatarGraphics = circleBuffer.createGraphics();
		avatarGraphics.setClip(new Ellipse2D.Float(0, 0, userAvatar.getWidth(), userAvatar.getHeight()));
		avatarGraphics.drawImage(userAvatar, 0, 0, userAvatar.getWidth(), userAvatar.getHeight(), null);
		avatarGraphics.dispose();
		graphics.drawImage(circleBuffer, x, y, null);
		return userAvatar;
	}

	/**
	 * Draws the avatar of an user
	 * 
	 * @param  member   the member whose avatar to render
	 * @param  graphics the to render on
	 * @param  x        the x position where the avatar will be renderer
	 * @param  y        the y position where the avatar will be renderer
	 * @param  size     the size of the avatar to render
	 * @return          the user's avatar after it was renderer
	 */
	public static BufferedImage drawUserAvatar(final Member member, final Graphics2D graphics, final int x, final int y,
			final int size) throws IOException {
		BufferedImage userAvatar = ImageIO.read(new URL(member.getEffectiveAvatarUrl()));
		userAvatar = resize(userAvatar, size);
		return drawUserAvatar(userAvatar, graphics, x, y);
	}

	/**
	 * Draws the avatar of an user
	 * 
	 * @param  user     the user whose avatar to render
	 * @param  graphics the to render on
	 * @param  x        the x position where the avatar will be renderer
	 * @param  y        the y position where the avatar will be renderer
	 * @param  size     the size of the avatar to render
	 * @return          the user's avatar after it was renderer
	 */
	public static BufferedImage drawUserAvatar(final User user, final Graphics2D graphics, final int x, final int y,
			final int size) throws IOException {
		BufferedImage userAvatar = ImageIO.read(new URL(user.getEffectiveAvatarUrl()));
		userAvatar = resize(userAvatar, size);
		return drawUserAvatar(userAvatar, graphics, x, y);
	}

	/**
	 * Takes a BufferedImage and resizes it according to the provided targetSize
	 *
	 * @param  src        the source BufferedImage
	 * @param  targetSize maximum height (if portrait) or width (if landscape)
	 * @return            a resized version of the provided BufferedImage
	 */
	public static BufferedImage resize(final BufferedImage src, final int targetSize) {
		if (targetSize <= 0)
			return src;
		int targetWidth = targetSize;
		int targetHeight = targetSize;
		final float ratio = (float) src.getHeight() / (float) src.getWidth();
		if (ratio <= 1) { // square or landscape-oriented image
			targetHeight = (int) Math.ceil(targetWidth * ratio);
		} else { // portrait image
			targetWidth = Math.round(targetHeight / ratio);
		}

		final BufferedImage retImg = new BufferedImage(targetWidth, targetHeight,
				src.getTransparency() == Transparency.OPAQUE ? BufferedImage.TYPE_INT_RGB
						: BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g2d = retImg.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2d.drawImage(src, 0, 0, targetWidth, targetHeight, null);
		g2d.dispose();
		return retImg;
	}

	/**
	 * Makes the corners of an image rounded
	 * 
	 * @param  image        the image whose corner to round
	 * @param  cornerRadius the radius of the corners
	 * @return              the modified {@code} image
	 */
	public static BufferedImage makeRoundedCorner(final BufferedImage image, final float cornerRadius) {
		final int w = image.getWidth();
		final int h = image.getHeight();
		final var output = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		final var g2 = output.createGraphics();

		g2.setComposite(AlphaComposite.Src);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setColor(Color.WHITE);
		g2.fill(new RoundRectangle2D.Float(0, 0, w, h, cornerRadius, cornerRadius));
		g2.setComposite(AlphaComposite.SrcAtop);
		g2.drawImage(image, 0, 0, null);
		g2.dispose();

		return output;
	}

	public static BufferedImage cutoutImageMiddle(final BufferedImage image, final int baseWidth, final int baseHeight,
			final int cornerRadius) {
		final var output = new BufferedImage(baseWidth, baseHeight, BufferedImage.TYPE_INT_ARGB);

		final var g2 = output.createGraphics();
		final var area = new Area(new Rectangle2D.Double(0, 0, baseWidth, baseHeight));
		final var toSubtract = new Area(new RoundRectangle2D.Double(cornerRadius, cornerRadius,
				baseWidth - cornerRadius * 2, baseHeight - cornerRadius * 2, cornerRadius, cornerRadius));
		area.subtract(toSubtract);
		g2.setPaint(new TexturePaint(image, new Rectangle2D.Double(0, 0, baseWidth, baseHeight)));
		g2.fill(area);
		g2.dispose();
		return output;
	}

	public static void paintTextWithOutline(final Graphics g, final String text, final Font font,
			final Color outlineColor, final Color fillColor, final float outlineWidth) {
		final var outlineStroke = new BasicStroke(outlineWidth);

		if (g instanceof final Graphics2D g2) {
			// remember original settings
			final var originalColor = g2.getColor();
			final var originalStroke = g2.getStroke();
			final var originalHints = g2.getRenderingHints();

			// create a glyph vector from your text
			final var glyphVector = font.createGlyphVector(g2.getFontRenderContext(), text);
			// get the shape object
			final var textShape = glyphVector.getOutline();

			g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

			g2.setColor(outlineColor);
			g2.setStroke(outlineStroke);
			g2.draw(textShape); // draw outline

			g2.setColor(fillColor);
			g2.fill(textShape); // fill the shape

			// reset to original settings after painting
			g2.setColor(originalColor);
			g2.setStroke(originalStroke);
			g2.setRenderingHints(originalHints);
		}
	}

	public static final void drawCenteredString(Graphics2D graphics, String string, Font font, float centerX,
			float centerY) {
		float x = getCenteringStartX(graphics, string, font, centerX);
		float y = getCenteringStartY(graphics, string, font, centerY);
		drawString(graphics, string, font, x, y);
	}

	public static final float getCenteringStartX(Graphics2D graphics, String string, Font font, float centerX) {
		float textWidth = getTextWidth(graphics, string, font);
		return centerX - (textWidth / 2);
	}

	public static final float getCenteringStartY(Graphics2D graphics, String string, Font font, float centerY) {
		float textHeight = getTextHeight(graphics, string, font);
		return centerY - (textHeight / 2);
	}

	public static final float drawString(Graphics2D graphics, String string, Font font, float x, float y) {
		graphics.setFont(font);
		y += font.getLineMetrics(string, graphics.getFontRenderContext()).getAscent();
		graphics.drawString(string, x, y);
		return getTextHeight(graphics, string, font);
	}

	public static final float getTextWidth(Graphics2D graphics, String string, Font font) {
		return graphics.getFontMetrics(font).stringWidth(string);
	}

	public static final float getTextHeight(Graphics2D graphics, String string, Font font) {
		LineMetrics lm = font.getLineMetrics(string, graphics.getFontRenderContext());
		return lm.getHeight();
	}
}
