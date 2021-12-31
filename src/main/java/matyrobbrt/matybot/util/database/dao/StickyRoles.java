package matyrobbrt.matybot.util.database.dao;

import java.util.List;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.transaction.Transactional;

public interface StickyRoles extends Transactional<StickyRoles> {

	/// Insertion methods ///

	/**
	 * Inserts an entry for the given user and role into the table.
	 *
	 * @param userId the snowflake ID of the user
	 * @param roleId the snowflake ID of the role
	 */
	@SqlUpdate("insert into sticky_roles values (:user, :guild, :role)")
	void insert(@Bind("user") long userId, @Bind("guild") long guildId, @Bind("role") long roleId);

	/**
	 * Inserts an entry for each role in the given iterable with the given user into
	 * the table.
	 *
	 * @param userId the snowflake ID of the user
	 * @param roles  an iterable of snowflake IDs of roles
	 */
	default void insert(long userId, long guildId, Iterable<Long> roles) {
		roles.forEach(roleId -> insert(userId, guildId, roleId));
	}

	/// Query methods ///

	/**
	 * Gets the stored roles for the given user.
	 *
	 * @param userId the snowflake ID of the user
	 * @return the list of role snowflake IDs which are associated with the user
	 */
	@SqlQuery("select role_id from sticky_roles where user_id = :user and guild_id = :guild")
	List<Long> getRoles(@Bind("user") long userId, @Bind("guild") long guildId);

	/// Deletion methods ///

	/**
	 * Delete the entry for the given user and role from the table.
	 *
	 * @param userId the snowflake ID of the user
	 * @param roleId the snowflake ID of the role
	 */
	@SqlUpdate("delete from sticky_roles where user_id = :user and role_id = :role and guild_id = :guild")
	void delete(@Bind("user") long userId, @Bind("guild") long guildId, @Bind("role") long roleId);

	/**
	 * Clears all entries for the given user from the table.
	 *
	 * @param userId the snowflake ID of the user
	 */
	@SqlUpdate("delete from sticky_roles where user_id = :user and guild_id = :guild")
	void clear(@Bind("user") long userId, @Bind("guild") long guildId);

}
