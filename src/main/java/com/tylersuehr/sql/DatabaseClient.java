package com.tylersuehr.sql;

public class DatabaseClient extends SQLiteOpenHelper {

	private final SQLiteDatabase db;

	public DatabaseClient(String name, int version) {
		super(name, version);
		this.db = getWritableInstance();
	}

	@Override
	protected void onCreate(SQLiteDatabase db) {
	}

	@Override
	protected void onUpdate(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

	/**
	 * Expose the SQLiteDatabase to anything that wants to use it.
	 */
	public SQLiteDatabase getDatabase() { return db; }
}
