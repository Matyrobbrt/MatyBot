package io.github.matyrobbrt.matybot.api.command.slash;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongFunction;

import com.jagrosh.jdautilities.command.SlashCommand;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;

public abstract class GuildSpecificSlashCommand extends SlashCommand {

	protected LongFunction<String[]> enabledRolesGetter = g -> new String[0];
	protected LongFunction<String[]> enabledUsersGetter = g -> new String[0];

	protected GuildSpecificSlashCommand(String doNothing) {
		// just to be on the safe side, configure it from the start
		guildOnly = true;
		defaultEnabled = false;
	}

	public static String[] convertLongListToStringArray(final List<Long> list) {
		return list.stream().map(String::valueOf).toArray(String[]::new);
	}

	public void buildAndUpsert(final JDA bot) {
		bot.getGuilds().forEach(guild -> {
			guild.upsertCommand(buildCommandData())
					.queue(cmd1 -> cmd1.updatePrivileges(guild, buildGuildPriviligies(guild.getIdLong())).queue());
		});
	}

	@SuppressWarnings("deprecation")
	public List<CommandPrivilege> buildGuildPriviligies(final long guildId) {
		List<CommandPrivilege> privileges = new ArrayList<>();
		// Privilege Checks
		for (String role : getEnabledRoles())
			privileges.add(CommandPrivilege.enableRole(role));
		for (String user : getEnabledUsers())
			privileges.add(CommandPrivilege.enableUser(user));
		for (String role : getDisabledRoles())
			privileges.add(CommandPrivilege.disableRole(role));
		for (String user : getDisabledUsers())
			privileges.add(CommandPrivilege.disableUser(user));
		// Co/Owner checks
		if (isOwnerCommand() && client != null) {
			// Clear array, we have the priority here.
			privileges.clear();
			// Add owner
			privileges.add(CommandPrivilege.enableUser(client.getOwnerId()));
			// Add co-owners
			if (client.getCoOwnerIds() != null)
				for (String user : client.getCoOwnerIds())
					privileges.add(CommandPrivilege.enableUser(user));
		}
		for (var role : enabledRolesGetter.apply(guildId)) {
			privileges.add(CommandPrivilege.enableRole(role));
		}
		for (var user : enabledUsersGetter.apply(guildId)) {
			privileges.add(CommandPrivilege.enableRole(user));
		}

		// can only have up to 10 privileges
		if (privileges.size() > 10)
			privileges = privileges.subList(0, 10);

		return privileges;
	}
}
