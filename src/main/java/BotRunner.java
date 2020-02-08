import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;


import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;
import java.io.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class BotRunner extends ListenerAdapter {

    private static ArrayList<TicTacToe> tttGames = new ArrayList<>();

    public static void main(String[] args) throws LoginException {
//        Scanner s = new Scanner(System.in);
//        System.out.print("Token: ");
//        String token = s.nextLine();
        JDABuilder builder = new JDABuilder(args[0]);
        builder.addEventListeners(new BotRunner());
        builder.build();
        System.out.println("Finished building JDA!");

        try {
            System.out.println("Now loading bot data...");
            FileInputStream fileInputStream = new FileInputStream("src/main/MehmeData");
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            tttGames = (ArrayList<TicTacToe>) objectInputStream.readObject();
            System.out.println("Loaded " + tttGames.size() + " TicTacToe games");
            System.out.println("Completed loading bot data!");
        }
        catch (IOException | ClassNotFoundException e) {
            System.out.println(e.toString());
        }

        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool ( 1 );
        Runnable saveDataRunnable = () -> {
            try {
                System.out.println("Now saving bot data...");
                FileOutputStream fileOutputStream = new FileOutputStream("src/main/MehmeData");
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
                objectOutputStream.writeObject(tttGames);
                System.out.println("Saved " + tttGames.size() + " TicTacToe games");
                objectOutputStream.close();
                fileOutputStream.close();
                System.out.println("Completed saving bot data!");

            }
            catch(IOException e) {
                System.out.println(e.toString());
            }
        };
        scheduledExecutorService.scheduleAtFixedRate(saveDataRunnable, 10, 30, TimeUnit.SECONDS);
    }

    public TicTacToe getPlayerBoard(User user) {
        for (TicTacToe game : tttGames) {
            if (game.getPlayerID().equals(user.getId())) {
                return game;
            }
        }
        return null;
    }

    public void removePlayerBoard(User user) {
        for (int i = 0; i < tttGames.size(); i++) {
            if (tttGames.get(i).getPlayerID().equals(user.getId()))
                tttGames.remove(i);
        }
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        MessageChannel channel = event.getChannel();
        User author = event.getAuthor();
        Message message = event.getMessage();
        String[] messagePhrases = message.getContentDisplay().split(" ");
        TicTacToe board = getPlayerBoard(author);

        // help
        if (messagePhrases.length >= 2 && messagePhrases[0].toLowerCase().equals("!help") && messagePhrases[1].toLowerCase().equals("ttt")) {
            channel.sendMessage("Mehme TicTacToe Instruction: "
                            + "\n!start ttt - starts a TicTacToe game"
                            + "\n!move [#] - plays a move on player's TicTacToe game"
                            + "\n!get board - returns current player's TicTacToe game board.").queue();
        }
        // start
        else if (messagePhrases.length >= 2 && messagePhrases[0].toLowerCase().equals("!start") && messagePhrases[1].toLowerCase().equals("ttt")) {
            if (board == null) {
                channel.sendMessage("Creating game for <@" + author.getId() + ">").queue();
                tttGames.add(board = new TicTacToe(author));
            }
            else
                channel.sendMessage("Game already created for <@" + author.getId() + ">").queue();
            channel.sendMessage(board.toEmbed()).queue();
        }
        // move
        else if (messagePhrases.length >= 2 && messagePhrases[0].toLowerCase().equals("!move")) {
            if (board == null)
                channel.sendMessage("No board found for <@" + author.getId() + ">. Create one with the command \"!start ttt\"").queue();
            else if (!board.movesLeft())
                channel.sendMessage("No moves remaining in <@" + author.getId() + ">'s TicTacToe game").queue();
            else if (board.playMove(messagePhrases[1].toLowerCase())) {
                if (board.checkWin(board.getMoveRow(), board.getMoveCol(), ":x:")) {
                    channel.sendMessage(board.toEmbed()).queue();
                    channel.sendMessage("<@" + author.getId() + "> has won TicTacToe!").queue();
                    removePlayerBoard(author);
                }
                else if (!board.movesLeft()) {
                    channel.sendMessage(board.toEmbed()).queue();
                    channel.sendMessage("<@" + author.getId() + ">'s TicTacToe game ended in a tie!").queue();
                }
                else {
                    board.moveAI();
                    channel.sendMessage(board.toEmbed()).queue();
                    if (board.checkWin(board.getMoveRow(), board.getMoveCol(), ":o:")) {
                        channel.sendMessage( "Mehme has won TicTacToe against <@" + author.getId() + ">").queue();
                        removePlayerBoard(author);
                    }
                    else if (!board.movesLeft()) {
                        channel.sendMessage(board.toEmbed()).queue();
                        channel.sendMessage("<@" + author.getId() + ">'s TicTacToe game ended in a tie!").queue();
                    }
                }
            }
            else
                channel.sendMessage("<@" + author.getId() + "> Invalid syntax or move. Command syntax: \"!move [number]\"").queue();
        }
        // get
        else if (messagePhrases.length >= 2 && messagePhrases[0].toLowerCase().equals("!get") && messagePhrases[1].toLowerCase().equals("ttt")) {
            if (board == null)
                channel.sendMessage("No board found for <@" + author.getId() + ">. Create one with the command \"!start ttt\"").queue();
            else {
                channel.sendMessage(board.toEmbed()).queue();
            }
        }
        // end
        else if (messagePhrases.length >= 2 && messagePhrases[0].toLowerCase().equals("!end") && messagePhrases[1].toLowerCase().equals("ttt")) {
            removePlayerBoard(author);
            channel.sendMessage("<@" + author.getId() + ">'s TicTacToe game has ended").queue();
        }
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