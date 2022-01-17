package io.github.matyrobbrt.matybot.util.database;

import static com.google.common.base.Preconditions.checkArgument;

import java.time.ZoneOffset;

import javax.sql.DataSource;

import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.jdbi.v3.sqlobject.customizer.TimestampedConfig;
import org.sqlite.SQLiteDataSource;

public class DatabaseManager {

	/**
	 * The data source connected to the database which is used by the application.
	 */
	private final DataSource dataSource;

	/**
	 * The NBT database used by the application
	 */
	private final MatyBotNBTDatabase nbtDatabase;

	/**
	 * The JDBI instance linked to the {@linkplain #dataSource database}.
	 */
	private final Jdbi jdbi;

	/**
	 * Creates a {@code DatabaseManager} by creating a {@link SQLiteDataSource}
	 * pointing at the SQLite database specified by the URL.
	 *
	 * @param url the url of the SQLite database to connect to
	 * @return a database manager connected to the specifiedSQLite database
	 * @throws IllegalArgumentException if the URL does not start with the
	 *                                  {@code jdbc:sqlite:} prefix
	 */
	public static DataSource connectSQLite(final String url) {
		checkArgument(url.startsWith("jdbc:sqlite:"), "SQLite DB URL does not start with 'jdbc:sqlite:': %s", url);

		SQLiteDataSource dataSource = new SQLiteDataSource();
		dataSource.setUrl(url);
		dataSource.setDatabaseName("matybot");

		return dataSource;
	}

	/**
	 * Constructs a {@code DatabaseManager} using the provided data source. Note
	 * that the database's connection returned by the data source must not require
	 * any username or password to connect.
	 *
	 * @param dataSource the SQL data source
	 */
	public DatabaseManager(final DataSource dataSource, final MatyBotNBTDatabase nbtDatabase) {
		this.dataSource = dataSource;
		this.jdbi = Jdbi.create(dataSource);
		this.nbtDatabase = nbtDatabase;

		// Install the SQL Objects and Guava plugins
		jdbi.installPlugin(new SqlObjectPlugin());
		// Set default timezone to UTC
		jdbi.getConfig(TimestampedConfig.class).setTimezone(ZoneOffset.UTC);

		try (Handle handle = jdbi.open()) {
			createTables(handle);
		}
	}

	private static void createTables(Handle handle) {
		handle.execute("""
				create table if not exists sticky_roles (
				    user_id unsigned big int not null,
					guild_id unsigned big int not null,
				    role_id unsigned big int not null,
				    primary key (user_id, guild_id, role_id)
				);""");

		handle.execute("""
				create table if not exists warnings (
					user_id unsigned big int not null,
					guild_id unsigned big int not null,
					warnings text not null,
					primary key (user_id, guild_id)
				)""");
	}

	/**
	 * {@return the SQL data source}
	 */
	public DataSource getDataSource() { return dataSource; }

	public MatyBotNBTDatabase getNbtDatabase() { return nbtDatabase; }

	/**
	 * {@return the JDBI instance linked to the database this manager is connected
	 * to}
	 */
	public Jdbi jdbi() {
		return jdbi;
	}

}
