package matyrobbrt.matybot.modules.commands.normal;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import matyrobbrt.matybot.api.annotation.RegisterCommand;
import matyrobbrt.matybot.modules.logging.events.JoinLeaveEvents;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message.MentionType;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;

public class SimulateJoinCommand extends Command {

	@RegisterCommand
	public static final SimulateJoinCommand COMMAND = new SimulateJoinCommand();

	public SimulateJoinCommand() {
		this.name = "simulatejoin";
		this.ownerCommand = true;
		this.aliases = new String[] {
				"simjoin"
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
		JoinLeaveEvents.INSTANCE
				.onGuildMemberJoin(new GuildMemberJoinEvent(event.getJDA(), event.getResponseNumber(), member));
	}

}
