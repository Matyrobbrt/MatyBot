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

	@SqlUpdate("update warnings set warnings = :warn where user_id = :user")
	void updateRaw(@Bind("user") long userId, @Bind("warn") String warningsDocument);

	@SqlUpdate("insert into warnings values(:user, :warn)")
	void insertRaw(@Bind("user") long userId, @Bind("warn") String warningsDocument);

	default void update(long userId, WarningsDocument doc) {
		updateRaw(userId, GSON.toJson(doc.toJson()));
	}

	default void insert(long userId, WarningsDocument doc) {
		insertRaw(userId, GSON.toJson(doc.toJson()));
	}

	@SqlQuery("select warnings from warnings where user_id = :user")
	String getWarningsRaw(@Bind("user") long userId);

	default WarningsDocument getWarnings(long userId) {
		String raw = getWarningsRaw(userId);
		if (raw == null) { return null; }
		return WarningsDocument.fromJson(GSON.fromJson(raw, JsonObject.class));
	}

	default void addWarn(long userId, WarningDocument doc) {
		WarningsDocument warns = getWarnings(userId);
		if (warns == null) {
			insert(userId, new WarningsDocument(doc));
		} else {
			warns.getWarns().add(doc);
			update(userId, warns);
		}
	}

	default WarningDocument removeWarn(long userId, int index) {
		WarningsDocument docs = getWarnings(userId);
		if (docs != null) {
			if (index < docs.getWarns().size()) {
				var toReturn = docs.getWarns().get(index);
				docs.getWarns().remove(index);
				update(userId, docs);
				return toReturn;
			}
		}
		return null;
	}

	@SqlUpdate("delete from warnings where user_id = :user")
	void clear(@Bind("user") long userId);

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
