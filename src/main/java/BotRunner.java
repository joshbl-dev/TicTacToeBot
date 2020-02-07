import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.Scanner;

public class BotRunner extends ListenerAdapter {

    private ArrayList<TicTacToe> tttGames = new ArrayList<>();

    public static void main(String[] args) throws LoginException {
        Scanner s = new Scanner(System.in);
        System.out.print("Token: ");
        String token = s.nextLine();
        JDABuilder builder = new JDABuilder(token);
        builder.addEventListeners(new BotRunner());
        builder.build();
        System.out.println("Finished building JDA!");
    }

    public TicTacToe getPlayerBoard(User user) {
        for (TicTacToe game : tttGames) {
            if (game.getPlayer().equals(user)) {
                return game;
            }
        }
        return null;
    }

    public void removePlayerBoard(User user) {
        for (int i = 0; i < tttGames.size(); i++) {
            if (tttGames.get(i).getPlayer().equals(user))
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
                channel.sendMessage("Creating game for " + author.getName()).queue();
                tttGames.add(board = new TicTacToe(author));
            }
            else
                channel.sendMessage("Game already created for " + author.getName()).queue();
            channel.sendMessage(board.toString()).queue();
        }
        // move
        else if (messagePhrases.length >= 2 && messagePhrases[0].toLowerCase().equals("!move")) {
            if (board == null)
                channel.sendMessage("No Game found").queue();
            else if (!board.movesLeft())
                channel.sendMessage("No moves remaining").queue();
            else if (board.playMove(messagePhrases[1].toLowerCase())) {
                if (board.checkWin(board.getMoveRow(), board.getMoveCol(), ":x:")) {
                    channel.sendMessage(author.getName() + " has won TicTacToe!").queue();
                    channel.sendMessage(board.toString()).queue();
                    removePlayerBoard(author);
                }
                else if (!board.movesLeft())
                    channel.sendMessage(author.getName() + "\'s TicTacToe game ended in a tie!").queue();
                else {
                    board.moveAI();
                    channel.sendMessage(board.toString()).queue();
                    if (board.checkWin(board.getMoveRow(), board.getMoveCol(), ":o:")) {
                        channel.sendMessage( "Mehme has won TicTacToe!").queue();
                        channel.sendMessage(board.toString()).queue();
                        removePlayerBoard(author);
                    }
                    else if (!board.movesLeft())
                        channel.sendMessage(author.getName() + "\'s TicTacToe game ended in a tie!").queue();
                }
            }
            else
                channel.sendMessage("Invalid syntax or move. Command syntax: \"!move [number]\"").queue();
        }
        // get
        else if (messagePhrases.length >= 2 && messagePhrases[0].toLowerCase().equals("!get") && messagePhrases[1].toLowerCase().equals("board")) {
            if (board == null)
                channel.sendMessage("No board found. Create one with the command \"!start ttt\"").queue();
            else {
                channel.sendMessage(board.toString()).queue();
            }
        }
    }
}