package matyrobbrt.matybot.modules.commands.normal;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import matyrobbrt.matybot.api.annotation.RegisterCommand;
import matyrobbrt.matybot.modules.logging.events.JoinLeaveEvents;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message.MentionType;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;

public class SimulateLeaveCommand extends Command {

	@RegisterCommand
	public static final SimulateLeaveCommand COMMAND = new SimulateLeaveCommand();

	public SimulateLeaveCommand() {
		this.name = "simulateleave";
		this.ownerCommand = true;
		this.aliases = new String[] {
				"simleave"
		};
	}

	@Override
	protected void execute(CommandEvent event) {
		Member member = null;
		try {
			member = event.getGuild().getMemberById(Long.parseLong(event.getArgs()));
		} catch (NumberFormatException e) {
			member = event.getMessage().getMentions(MentionType.USER).isEmpty() ? event.getMember()
					: event.getMessage().getMentionedMembers().get(0);
		}
		JoinLeaveEvents.INSTANCE.onGuildMemberRemove(new GuildMemberRemoveEvent(event.getJDA(),
				event.getResponseNumber(), event.getGuild(), member.getUser(), member));
	}

}
