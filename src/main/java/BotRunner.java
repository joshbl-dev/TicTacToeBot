import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;


import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BotRunner extends ListenerAdapter {

    private static TicTacToeUpdater ticTacToeUpdater = new TicTacToeUpdater();

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws LoginException {
//        Scanner s = new Scanner(System.in);
//        System.out.print("Token: ");
//        String token = s.nextLine();
        JDABuilder builder = new JDABuilder(args[0]);
        builder.addEventListeners(new BotRunner());
        builder.build();
        System.out.println("Finished building JDA!");
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        ticTacToeUpdater.updateGames(event);
    }

    @Override
    public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event) {
        super.onGuildMemberJoin(event);
        Member member = event.getMember();
        Guild guild = event.getGuild();
        if (guild.getRolesByName("Tyro", true).size() > 0)
            guild.addRoleToMember(member, event.getGuild().getRolesByName("Tyro", true).get(0)).complete();
    }
}