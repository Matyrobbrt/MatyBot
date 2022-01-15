package matyrobbrt.matybot.modules.logging.events;

import static matyrobbrt.matybot.MatyBot.LOGGER;
import static matyrobbrt.matybot.util.ImageUtils.cutoutImageMiddle;
import static matyrobbrt.matybot.util.ImageUtils.drawCenteredString;
import static matyrobbrt.matybot.util.ImageUtils.drawUserAvatar;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import matyrobbrt.matybot.MatyBot;
import matyrobbrt.matybot.api.event.AnnotationEventListener;
import matyrobbrt.matybot.modules.logging.LoggingModule;
import matyrobbrt.matybot.util.BotUtils;
import matyrobbrt.matybot.util.database.dao.StickyRoles;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import net.dv8tion.jda.api.utils.TimeFormat;

public class JoinLeaveEvents extends AnnotationEventListener {

	public static final JoinLeaveEvents INSTANCE = new JoinLeaveEvents();

	@Override
	@SubscribeEvent
	public void onEventHandleAnnotation(GenericEvent event) {
		super.onEventHandleAnnotation(event);
	}

	private Font font = null;

	public JoinLeaveEvents() {
		try {
			final var graphicsEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
			this.font = Font.createFont(Font.TRUETYPE_FONT, MatyBot.class.getResourceAsStream("/fonts/minecraft.ttf"))
					.deriveFont(12f);
			graphicsEnv.registerFont(this.font);
		} catch (FontFormatException | IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		final var loggingChannel = LoggingModule.getLoggingChannel(event.getGuild());

		Member member = event.getMember();
		final var roles = BotUtils.getOldUserRoles(event.getGuild(), member.getIdLong());
		if (!roles.isEmpty()) {
			new Thread(() -> roles.forEach(role -> event.getGuild().addRoleToMember(member, role).queue()),
					"Adding roles for " + member.getUser().getAsTag()).start();
		}

		MatyBot.getConfigForGuild(event).joinRoles.forEach(roleId -> {
			final var role = event.getGuild().getRoleById(roleId);
			if (role != null && !member.getRoles().contains(role)) {
				event.getGuild().addRoleToMember(member, role).queue();
			}
		});

		final var user = event.getUser();
		final var embed = new EmbedBuilder();
		final var dateJoinedDiscord = member.getTimeCreated().toInstant();
		embed.setColor(Color.GREEN);
		embed.setTitle("User Joined");
		embed.setThumbnail(user.getEffectiveAvatarUrl());
		embed.addField("User:", user.getAsTag(), true);
		if (!roles.isEmpty()) {
			embed.addField("Roles:", roles.stream().map(IMentionable::getAsMention).collect(Collectors.joining()),
					true);
		}

		embed.addField("Joined Discord:", TimeFormat.RELATIVE.format(dateJoinedDiscord), true);
		embed.setFooter("User ID: " + user.getId());
		embed.setTimestamp(Instant.now());

		loggingChannel.sendMessageEmbeds(embed.build()).queue();

		BotUtils.getChannelIfPresent(MatyBot.getConfigForGuild(event.getGuild()).welcomeChannel, welcomeChannel -> {
			BotUtils.createMessage(welcomeChannel).addFile(makeJoinImage(member, font), "welcome.png")
					.append(String.format("Everyone welcome %s!", member.getAsMention())).mention(member)
					.queue(m -> m.addReaction("U+1F44B").queue());
		});
	}

	@Override
	public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
		final var loggingChannel = LoggingModule.getLoggingChannel(event.getGuild());

		Member member = event.getMember();
		List<Role> roles = null;
		if (member != null) {
			roles = member.getRoles();
			final List<Long> roleIds = roles.stream().map(ISnowflake::getIdLong).toList();
			BotUtils.clearOldUserRoles(member.getIdLong(), event.getGuild().getOwnerIdLong());
			MatyBot.database().useExtension(StickyRoles.class,
					data -> data.insert(member.getIdLong(), event.getGuild().getIdLong(), roleIds));
		}

		User user = event.getUser();
		final var embed = new EmbedBuilder();
		embed.setColor(java.awt.Color.RED);
		embed.setTitle("User Left");
		embed.setThumbnail(user.getEffectiveAvatarUrl());
		embed.addField("User:", user.getAsTag(), true);
		if (roles != null && !roles.isEmpty()) {
			embed.addField("Roles:", roles.stream().map(IMentionable::getAsMention).collect(Collectors.joining()),
					true);
			LOGGER.info(BotUtils.Markers.EVENTS, "User {} had the following roles before leaving: {}", user, roles);
		} else if (roles == null) {
			embed.addField("Roles:", "_Could not obtain user's roles._", true);
		}
		embed.setFooter("User ID: " + user.getId());
		embed.setTimestamp(Instant.now());

		loggingChannel.sendMessageEmbeds(embed.build()).queue();
	}

	public static File makeJoinImage(final Member member, final Font font) {
		final var location = Paths.get("joins/" + member.getIdLong() + ".png").toFile();
		final var guild = member.getGuild();
		// final var profile = member.getUser().retrieveProfile().complete();

		try {
			final int width = 950;
			final int height = 300;
			final Color mainColour = Color.BLACK;
			final BufferedImage joinImageBuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			final Graphics2D graphics = (Graphics2D) joinImageBuffer.getGraphics();
			graphics.addRenderingHints(
					new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			// Background
			BufferedImage background;
			final var bgBuf = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			final var backgroundGraphics = bgBuf.createGraphics();
			backgroundGraphics.setColor(mainColour);
			backgroundGraphics.fillRect(0, 0, width, height);
			backgroundGraphics.dispose();
			background = bgBuf;
			// background = ImageIO.read(new URL(profile.getBannerUrl())); }

			final var bgBuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			final var bgGraphics = bgBuffer.createGraphics();
			bgGraphics.setClip(new Rectangle2D.Float(0, 0, width, height));
			bgGraphics.drawImage(background, 0, 0, width, height, null);
			bgGraphics.dispose();
			graphics.drawImage(bgBuffer, 0, 0, width, height, null);

			// Outline
			BufferedImage outlineImg;
			final var outBuf = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			final var outGraphics = outBuf.createGraphics();
			outGraphics.setColor(Color.MAGENTA);
			outGraphics.fillRect(0, 0, width, height);
			outGraphics.dispose();
			outlineImg = outBuf;

			outlineImg = cutoutImageMiddle(outlineImg, width, height, 12);

			final var outlineAlpha = 0.5f;
			final var alphaComp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, outlineAlpha);
			graphics.setComposite(alphaComp);
			graphics.drawImage(outlineImg, 0, 0, width, height, null);
			graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));

			// Welcome
			graphics.setStroke(new BasicStroke(3));
			graphics.setColor(Color.LIGHT_GRAY);
			drawCenteredString(graphics, "Welcome", font.deriveFont(50f), 600, 80);

			// Member name
			final String memberName = member.getUser().getAsTag();
			var nameFontSize = 52f;
			if (memberName.length() > 12) {
				nameFontSize -= memberName.length() * 1.2f - 12;
			}
			graphics.setColor(Color.GREEN);
			drawCenteredString(graphics, memberName, font.deriveFont(nameFontSize), 600, 135);
			graphics.setColor(mainColour);

			// Guild name
			var guildNameFontSize = 35f;
			final String guildName = "To " + guild.getName();
			if (guildName.length() > 30) {
				guildNameFontSize -= guildName.length() * .8f - 30;
			}
			graphics.setColor(Color.LIGHT_GRAY);
			drawCenteredString(graphics, guildName, font.deriveFont(guildNameFontSize), 600, 190);

			// Member count
			final String memberCount = "Member #" + guild.getMemberCount();
			drawCenteredString(graphics, memberCount, font.deriveFont(29.0f), 600, 240);

			// User avatar
			final var userAvatar = drawUserAvatar(member, graphics, 40, 50, 200);
			graphics.setColor(Color.CYAN);
			graphics.drawOval(40, 50, userAvatar.getWidth(), userAvatar.getHeight());

			// Finally, dispose the final graphics
			graphics.dispose();

			if (!location.exists()) {
				location.mkdirs();
			}
			ImageIO.write(joinImageBuffer, "png", location);
		} catch (final IOException e) {
			e.printStackTrace();
		}

		return location;
	}

}
