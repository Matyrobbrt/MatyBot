package matyrobbrt.matybot.util.database.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public interface Warnings {

	static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

	/**
	 * @deprecated use {@link #update(long, long, WarningsDocument)}
	 * @param userId
	 * @param guildId
	 * @param warningsDocument
	 */
	@Deprecated(forRemoval = false)
	@SqlUpdate("update warnings set warnings = :warn where user_id = :user and guild_id = :guild")
	void updateRaw(@Bind("user") long userId, @Bind("guild") long guildId, @Bind("warn") String warningsDocument);

	/**
	 * @deprecated use {@link #insert(long, long, WarningsDocument)}
	 * @param userId
	 * @param guildId
	 * @param warningsDocument
	 */
	@Deprecated(forRemoval = false)
	@SqlUpdate("insert into warnings values(:user, :guild, :warn)")
	void insertRaw(@Bind("user") long userId, @Bind("guild") long guildId, @Bind("warn") String warningsDocument);

	default void update(long userId, long guildId, WarningsDocument doc) {
		updateRaw(userId, guildId, GSON.toJson(doc.toJson()));
	}

	default void insert(long userId, long guildId, WarningsDocument doc) {
		insertRaw(userId, guildId, GSON.toJson(doc.toJson()));
	}

	/**
	 * @deprecated use {@link #getWarnings(long, long)}
	 * @param userId
	 * @param guildId
	 * @return
	 */
	@Deprecated(forRemoval = false)
	@SqlQuery("select warnings from warnings where user_id = :user and guild_id = :guild")
	String getWarningsRaw(@Bind("user") long userId, @Bind("guild") long guildId);

	default WarningsDocument getWarnings(long userId, long guildId) {
		String raw = getWarningsRaw(userId, guildId);
		if (raw == null) { return null; }
		return WarningsDocument.fromJson(GSON.fromJson(raw, JsonObject.class));
	}

	default void addWarn(long userId, long guildId, WarningDocument doc) {
		WarningsDocument warns = getWarnings(userId, guildId);
		if (warns == null) {
			insert(userId, guildId, new WarningsDocument(doc));
		} else {
			warns.getWarns().add(doc);
			update(userId, guildId, warns);
		}
	}

	default WarningDocument removeWarn(long userId, long guildId, int index) {
		WarningsDocument docs = getWarnings(userId, guildId);
		if (docs != null) {
			if (index < docs.getWarns().size()) {
				var toReturn = docs.getWarns().get(index);
				docs.getWarns().remove(index);
				update(userId, guildId, docs);
				return toReturn;
			}
		}
		return null;
	}

	@SqlUpdate("delete from warnings where user_id = :user and guild_id = :guild")
	void clear(@Bind("user") long userId, @Bind("guild") long guildId);

	public static record WarningDocument(String reason, long warner, long timeStamp, JsonObject jsonObject) {

		public WarningDocument(String reason, long warner, long timeStamp) {
			this(reason, warner, timeStamp, formatToJson(reason, warner, timeStamp));
		}

		public WarningDocument(JsonObject obj) {
			this(obj.get("reason").getAsString(), obj.get("warner").getAsLong(), obj.get("timestamp").getAsLong(), obj);
		}

		public static JsonObject formatToJson(String reason, long warner, long timeStamp) {
			JsonObject obj = new JsonObject();
			obj.addProperty("reason", reason);
			obj.addProperty("warner", warner);
			obj.addProperty("timestamp", timeStamp);
			return obj;
		}

	}

	public static class WarningsDocument {

		public WarningsDocument(WarningDocument... docs) {
			warns.addAll(Arrays.asList(docs));
		}

		private final List<WarningDocument> warns = new ArrayList<>();

		public List<WarningDocument> getWarns() { return warns; }

		public JsonObject toJson() {
			JsonObject obj = new JsonObject();
			obj.addProperty("size", warns.size());
			for (int i = 0; i < warns.size(); i++) {
				obj.add(String.valueOf(i), warns.get(i).jsonObject());
			}
			return obj;
		}

		public static WarningsDocument fromJson(JsonObject obj) {
			WarningsDocument docs = new WarningsDocument();
			int size = obj.get("size").getAsInt();
			for (int i = 0; i < size; i++) {
				docs.warns.add(new WarningDocument(obj.get(String.valueOf(i)).getAsJsonObject()));
			}
			return docs;
		}

	}
}
