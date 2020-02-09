import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;


import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;
import java.awt.*;
import java.io.*;
import java.nio.channels.Channel;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BotRunner extends ListenerAdapter {

    private static TicTacToeUpdater ticTacToeUpdater = new TicTacToeUpdater();

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws LoginException {
        // builds discord interaction
        JDABuilder builder = new JDABuilder(args[0]);
        builder.addEventListeners(new BotRunner());
        builder.build();
        System.out.println("Finished building JDA!");
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        MessageChannel channel = event.getChannel();
        Message message = event.getMessage();
        String[] messagePhrases = message.getContentDisplay().toLowerCase().split(" ");

        if (messagePhrases.length > 0) {
            // help command
            if (messagePhrases[0].equals("!help")) {
                EmbedBuilder eb = new EmbedBuilder();
                eb.setTitle("Mehme Commands:");
                eb.setColor(new Color(0, 0, 255));
                eb.addField("**List help modules**", "!help", false);
                eb.addField("**List TicTacToe commands**", "!ttt help", false);
                channel.sendMessage(eb.build()).queue();
            }
            // tictactoe commands
            else if (messagePhrases[0].equals("!ttt"))
                ticTacToeUpdater.onMessageReceived(event);
        }
    }

    @Override
    public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event) {
        super.onGuildMemberJoin(event);
        Member member = event.getMember();
        Guild guild = event.getGuild();

        // adds default role to Diamond Testing Discord server
        List<Role> roles = guild.getRolesByName("Tyro", true);
        if (guild.getName().equals("Diamond Testing") && roles.size() > 0) {
            System.out.println("Adding role Tyro to " + member.getEffectiveName());
            guild.addRoleToMember(member, roles.get(0)).complete();
        }
    }
}