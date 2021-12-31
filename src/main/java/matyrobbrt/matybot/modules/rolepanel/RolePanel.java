package matyrobbrt.matybot.modules.rolepanel;

import com.google.gson.annotations.Expose;

public class RolePanel {

	@Expose
	public long channelID;

	@Expose
	public long messageID;

	@Expose
	public RoleAndEmoji[] emojis;

	public Long getRoleForEmoji(String emoji) {
		for (var set : emojis) {
			if (set.emoji.equalsIgnoreCase(emoji)) { return set.roleID; }
		}
		return null;
	}

	public static class RoleAndEmoji {

		@Expose
		public String emoji;

		@Expose
		public long roleID;
	}

}
