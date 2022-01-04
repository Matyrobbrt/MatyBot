package matyrobbrt.matybot.modules.rolepanel;

import static matyrobbrt.matybot.modules.rolepanel.RolePanelsModule.getPanelForChannelAndMessage;

import matyrobbrt.matybot.api.event.AnnotationEventListener;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

public class RolePanelHandler extends AnnotationEventListener {

	@Override
	@SubscribeEvent
	public void onEventHandleAnnotation(GenericEvent event) {
		super.onEventHandleAnnotation(event);
	}

	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent event) {
		Member member = event.retrieveMember().complete();
		if ((member == null) || member.getUser().isBot()) { return; }
		RolePanel panel = getPanelForChannelAndMessage(event.getChannel().getIdLong(), event.getMessageIdLong());
		if (panel == null) { return; }
		final var emote = event.getReactionEmote();
		Long roleId = panel.getRoleForEmoji(emote.isEmoji() ? emote.getAsCodepoints() : emote.getAsReactionCode());
		if (roleId == null) { return; }
		Role role = event.getGuild().getRoleById(roleId);
		if (role == null) { return; }

		if (!member.getRoles().contains(role)) {
			event.getGuild().addRoleToMember(member, role).queue();
		}
	}

	@Override
	public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
		Member member = event.retrieveMember().complete();
		if ((member == null) || member.getUser().isBot()) { return; }
		RolePanel panel = getPanelForChannelAndMessage(event.getChannel().getIdLong(), event.getMessageIdLong());
		if (panel == null) { return; }
		final var emote = event.getReactionEmote();
		Long roleId = panel.getRoleForEmoji(emote.isEmoji() ? emote.getAsCodepoints() : emote.getAsReactionCode());
		if (roleId == null) { return; }
		Role role = event.getGuild().getRoleById(roleId);
		if (role == null) { return; }

		if (member.getRoles().contains(role)) {
			event.getGuild().removeRoleFromMember(member, role).queue();
		}
	}

}
