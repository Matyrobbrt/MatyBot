package io.github.matyrobbrt.matybot.util.database.dao.nbt;

import java.awt.Color;

import io.github.matyrobbrt.javanbt.nbt.CompoundNBT;
import io.github.matyrobbrt.javanbt.serialization.Deserializer;
import io.github.matyrobbrt.javanbt.serialization.NBTSerializable;
import io.github.matyrobbrt.javanbt.serialization.Serializers;
import io.github.matyrobbrt.matybot.modules.levelling.LevellingModule;
import io.github.matyrobbrt.matybot.util.nbt.NBTBuilder;
import net.dv8tion.jda.api.entities.Guild;

public class LevelData implements NBTSerializable<CompoundNBT> {

	public static final RankCard DEFAULT_RANK_CARD = new RankCard();

	public static final Deserializer<CompoundNBT, LevelData> DESERIALIZER = Serializers
			.registerDeserializer(LevelData.class, nbt -> {
				LevelData data = new LevelData();
				data.deserializeNBT(nbt);
				return data;
			});

	@Override
	public CompoundNBT serializeNBT() {
		return new NBTBuilder().putInt("xp", xp).put("rankCard", rankCard).build();
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		xp = nbt.getInt("xp");
		if (nbt.contains("rankCard")) {
			rankCard.deserializeNBT(nbt.getCompound("rankCard"));
		}
	}

	private int xp;
	private RankCard rankCard = new RankCard();

	public int addXp(final int amount) {
		xp += amount;
		return xp;
	}

	public int getXp() { return xp; }

	public void setXp(int xp) { this.xp = xp; }

	public RankCard getRankCard() { return rankCard; }

	public void setRankCard(final RankCard rankCard) { this.rankCard = rankCard; }

	public int getXpInGuild(final Guild guild) {
		return LevellingModule.getLevelForXP(xp, guild);
	}

	public static class RankCard implements NBTSerializable<CompoundNBT> {

		private float outlineOpacity = 0.5f;

		private String backgroundImage = "";
		private String outlineImage = "";
		private String xpOutlineImage = "";
		private String xpEmptyImage = "";
		private String xpFillImage = "";
		private String avatarOutlineImage = "";

		private Color backgroundColour = Color.BLACK;
		private Color outlineColour = Color.WHITE;
		private Color rankTextColour = Color.RED;
		private Color levelTextColour = Color.GREEN;
		private Color xpOutlineColour = Color.BLUE;
		private Color xpEmptyColour = Color.YELLOW;
		private Color xpFillColour = Color.CYAN;
		private Color avatarOutlineColour = Color.MAGENTA;
		private Color percentTextColour = Color.ORANGE;
		private Color xpTextColour = Color.PINK;
		private Color nameTextColour = Color.DARK_GRAY;

		@Override
		public CompoundNBT serializeNBT() {
			final CompoundNBT stringsNBT = new NBTBuilder().putFloat("outlineOpacity", outlineOpacity)
					.putString("backgroundImage", backgroundImage)
					.putString("outlineImage", outlineImage).putString("xpOutlineImage", xpOutlineImage)
					.putString("xpEmptyImage", xpEmptyImage).putString("xpFillImage", xpFillImage)
					.putString("avatarOutlineImage", avatarOutlineImage).build();

			return NBTBuilder.of(stringsNBT).putColor("backgroundColour", backgroundColour)
					.putColor("outlineColour", outlineColour).putColor("rankTextColour", rankTextColour)
					.putColor("levelTextColour", levelTextColour).putColor("xpOutlineColour", xpOutlineColour)
					.putColor("xpEmptyColour", xpEmptyColour).putColor("xpFillColour", xpFillColour)
					.putColor("avatarOutlineColour", avatarOutlineColour)
					.putColor("percentTextColour", percentTextColour).putColor("xpTextColour", xpTextColour)
					.putColor("nameTextColour", nameTextColour).build();
		}

		@Override
		public void deserializeNBT(CompoundNBT nbt) {
			outlineOpacity = nbt.getFloat("outlineOpacity");

			backgroundImage = nbt.getString("backgroundImage");
			outlineImage = nbt.getString("outlineImage");
			xpOutlineImage = nbt.getString("xpOutlineImage");
			xpEmptyImage = nbt.getString("xpEmptyImage");
			xpFillImage = nbt.getString("xpFillImage");

			backgroundColour = readColour("backgroundColour", nbt);
			outlineColour = readColour("outlineColour", nbt);
			rankTextColour = readColour("rankTextColour", nbt);
			levelTextColour = readColour("levelTextColour", nbt);
			xpOutlineColour = readColour("xpOutlineColour", nbt);
			xpEmptyColour = readColour("xpEmptyColour", nbt);
			xpFillColour = readColour("xpFillColour", nbt);
			avatarOutlineColour = readColour("avatarOutlineColour", nbt);
			percentTextColour = readColour("percentTextColour", nbt);
			xpTextColour = readColour("xpTextColour", nbt);
			nameTextColour = readColour("nameTextColour", nbt);
		}

		private static Color readColour(String key, CompoundNBT nbt) {
			return new Color(nbt.getInt(key));
		}

		public String getBackgroundImage() { return backgroundImage; }

		public void setBackgroundImage(String backgroundImage) { this.backgroundImage = backgroundImage; }

		public String getOutlineImage() { return outlineImage; }

		public void setOutlineImage(String outlineImage) { this.outlineImage = outlineImage; }

		public Color getBackgroundColour() { return backgroundColour; }

		public void setBackgroundColour(Color backgroundColour) { this.backgroundColour = backgroundColour; }

		public Color getOutlineColour() { return outlineColour; }

		public void setOutlineColour(Color outlineColour) { this.outlineColour = outlineColour; }

		public Color getLevelTextColour() { return levelTextColour; }

		public void setLevelTextColour(Color levelTextColour) { this.levelTextColour = levelTextColour; }

		public Color getRankTextColour() { return rankTextColour; }

		public void setRankTextColour(Color rankTextColour) { this.rankTextColour = rankTextColour; }

		public Color getXpFillColour() { return xpFillColour; }

		public void setXpFillColour(Color xpFillColour) { this.xpFillColour = xpFillColour; }

		public Color getPercentTextColour() { return percentTextColour; }

		public void setPercentTextColour(Color percentTextColour) { this.percentTextColour = percentTextColour; }

		public Color getAvatarOutlineColour() { return avatarOutlineColour; }

		public void setAvatarOutlineColour(Color avatarOutlineColour) {
			this.avatarOutlineColour = avatarOutlineColour;
		}

		public Color getXpOutlineColour() { return xpOutlineColour; }

		public void setXpOutlineColour(Color xpOutlineColour) { this.xpOutlineColour = xpOutlineColour; }

		public Color getXpEmptyColour() { return xpEmptyColour; }

		public void setXpEmptyColour(Color xpEmptyColour) { this.xpEmptyColour = xpEmptyColour; }

		public Color getXpTextColour() { return xpTextColour; }

		public void setXpTextColour(Color xpTextColour) { this.xpTextColour = xpTextColour; }

		public Color getNameTextColour() { return nameTextColour; }

		public void setNameTextColour(Color nameTextColour) { this.nameTextColour = nameTextColour; }

		public String getXpEmptyImage() { return xpEmptyImage; }

		public void setXpEmptyImage(String xpEmptyImage) { this.xpEmptyImage = xpEmptyImage; }

		public String getXpOutlineImage() { return xpOutlineImage; }

		public void setXpOutlineImage(String xpOutlineImage) { this.xpOutlineImage = xpOutlineImage; }

		public String getAvatarOutlineImage() { return avatarOutlineImage; }

		public void setAvatarOutlineImage(String avatarOutlineImage) { this.avatarOutlineImage = avatarOutlineImage; }

		public String getXpFillImage() { return xpFillImage; }

		public void setXpFillImage(String xpFillImage) { this.xpFillImage = xpFillImage; }

		public float getOutlineOpacity() { return outlineOpacity; }

		public void setOutlineOpacity(float outlineOpacity) { this.outlineOpacity = outlineOpacity; }
	}
}
