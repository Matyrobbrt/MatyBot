package io.github.matyrobbrt.matybot.api.command.slash;

import java.util.ArrayList;
import java.util.List;

import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.commands.Command.Type;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;

public class ContextMenu implements EventListener {

	public net.dv8tion.jda.api.interactions.commands.Command.Type type;
	public String name;

	/**
	 * The list of role IDs who can use this Slash Command. Because command
	 * privileges are restricted to a Guild, these will not take effect for Global
	 * commands.<br>
	 * This is useless if {@link #defaultEnabled} isn't false.
	 */
	protected String[] enabledRoles = new String[] {};

	/**
	 * The list of user IDs who can use this Menu. Because command privileges are
	 * restricted to a Guild, these will not take effect for Global menus.<br>
	 * This is useless if {@link #defaultEnabled} isn't false.
	 */
	protected String[] enabledUsers = new String[] {};

	/**
	 * The list of role IDs who cannot use this Menu. Because command privileges are
	 * restricted to a Guild, these will not take effect for Global menus.<br>
	 * This is useless if {@link #defaultEnabled} isn't true.
	 */
	protected String[] disabledRoles = new String[] {};

	/**
	 * The list of user IDs who cannot use this Menu. Because command privileges are
	 * restricted to a Guild, these will not take effect for Global menus.<br>
	 * This is useless if {@link #defaultEnabled} isn't true.
	 */
	protected String[] disabledUsers = new String[] {};

	/**
	 * Whether this menu is disabled by default. If disabled, you must give yourself
	 * permission to use it.<br>
	 * In order for {@link #enabledUsers} and {@link #enabledRoles} to work, this
	 * must be set to false.
	 *
	 * @see #enabledRoles
	 * @see #enabledUsers
	 */
	protected boolean defaultEnabled = true;

	protected boolean guildOnly = true;

	protected ContextMenu() {

	}

	public void onMessageContextInteraction(final MessageContextInteractionEvent event) {

	}

	public void onUserContextInteraction(final UserContextInteractionEvent event) {

	}

	@Override
	public final void onEvent(GenericEvent event) {
		if (event instanceof MessageContextInteractionEvent msgEvent && msgEvent.getName().equals(name)
				&& type == Type.MESSAGE) {
			onMessageContextInteraction(msgEvent);
		} else if (event instanceof UserContextInteractionEvent userEvent && userEvent.getName().equals(name)
				&& type == Type.USER) {
			onUserContextInteraction(userEvent);
		}
	}

	public CommandData buildCommandData() {
		var data = Commands.context(type, name);
		// Check for children
		/*
		 * if (children.length != 0) { // Temporary map for easy group storage
		 * Map<String, SubcommandGroupData> groupData = new HashMap<>(); for
		 * (ContextMenuCommand child : children) {
		 * data.addSubcommands(child.buildCommandData()); } }
		 */
		data.setDefaultEnabled(defaultEnabled);
		return data;
	}

	public List<CommandPrivilege> buildPrivileges() {
		List<CommandPrivilege> privileges = new ArrayList<>();
		// Privilege Checks
		for (String role : enabledRoles) {
			privileges.add(CommandPrivilege.enableRole(role));
		}
		for (String user : enabledUsers) {
			privileges.add(CommandPrivilege.enableUser(user));
		}
		for (String role : disabledRoles) {
			privileges.add(CommandPrivilege.disableRole(role));
		}
		for (String user : disabledUsers) {
			privileges.add(CommandPrivilege.disableUser(user));
		}

		// can only have up to 10 privileges
		if (privileges.size() > 10) {
			privileges = privileges.subList(0, 10);
		}

		return privileges;
	}

	public boolean isGuildOnly() { return guildOnly; }

}
