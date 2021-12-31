package com.tylersuehr.sql;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;

public class SQLiteJsonTable {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

	private final SQLiteDatabase db;
	private final String name;

	SQLiteJsonTable(SQLiteDatabase db, String name) {
		this.db = db;
		this.name = name;
	}

	public void add(String id, JsonElement json) {
		ResultSet fetched = db.query(name, " ID = '" + id + "'", null, null);

		// If not found, create empty row
		if (fetched == null) {
			db.execute(SQLBuilder.createInsert(name, new ContentValues().put("ID", id).put("json", "{}")));
		}

		db.execute(
				SQLBuilder.createUpdate(name, new ContentValues().put("json", json.toString()), " ID = '" + id + "'"));
	}

	public void push(String columnId, String elemId, JsonElement element) {
		JsonElement obj = fetch(columnId);
		if (obj.isJsonObject()) {
			obj.getAsJsonObject().add(elemId, element);
			add(columnId, obj);
		}
	}

	public void pushToArray(String id, JsonElement element) {
		JsonElement obj = fetch(id);
		if (obj.isJsonArray()) {
			obj.getAsJsonArray().add(element);
			add(id, obj);
		}
	}

	public JsonElement fetch(String columnId) {
		try {
			return GSON.fromJson(db.query(name, " ID = '" + columnId + "'", null, null).getString("json"),
					JsonElement.class);
		} catch (JsonSyntaxException | SQLException e) {}
		return null;
	}

	public JsonElement fetchElement(String columnId, String elementId) {
		JsonElement obj = fetch(columnId);
		if (obj == null) { return null; }
		if (obj.isJsonObject()) { return obj.getAsJsonObject().get(elementId); }
		return null;
	}

	public JsonElement deleteByID(String columnId) {
		JsonElement old = fetch(columnId);
		db.execute(SQLBuilder.createDelete(name, " ID = '" + columnId + "'"));
		return old;
	}

}
