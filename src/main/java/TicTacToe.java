import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;

public class TicTacToe implements Serializable {

    private String playerName;
    private String playerID;
    private String[][] board;
    private int moveRow;
    private int moveCol;

    private ArrayList<Integer> possibleMoves;

    private static final String[] NUM_TO_EMOJI = {":zero:", ":one:", ":two:", ":three:", ":four:", ":five:", ":six:", ":seven:", ":eight:"};

    public TicTacToe(User user) {
        playerName = user.getName();
        playerID = user.getId();
        board = new String[3][3];
        possibleMoves = new ArrayList<>();

        for (int i = 0; i < 9; i++)
            possibleMoves.add(i);

        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                board[row][col] = NUM_TO_EMOJI[row * board.length + col];
            }
        }
    }

    // accessors

    public String getPlayerID() {
        return playerID;
    }

    public int getMoveCol() {
        return moveCol;
    }

    public int getMoveRow() {
        return moveRow;
    }

    // finds index of move in possibleMoves
    private int findMoveIndex(int move) {
        for (int  i = 0; i < possibleMoves.size(); i++) {
            if (possibleMoves.get(i) == move)
                return i;
        }
        return -1;
    }

    // returns if moves left on board
    public boolean movesLeft() {
        return possibleMoves.size() != 0;
    }

    // plays the user's move
    public boolean playMove(String move) {
        try {
            int moveNum = Integer.parseInt(move);
            if (moveNum >= 0 && moveNum <= 8) {
                for (int row = 0; row < board.length; row++) {
                    for (int col = 0; col < board[row].length; col++) {
                        if (row * board.length + col == moveNum && !board[row][col].equals(":x:") && !board[row][col].equals(":o:")) {
                            moveRow = row;
                            moveCol = col;
                            board[moveRow][moveCol] = ":x:";
                            possibleMoves.remove(findMoveIndex(moveNum));
                            return true;
                        } else if (row * board.length + col == Integer.parseInt(move))
                            return false;
                    }
                }
            }
        }
        catch (NumberFormatException e) {
            return false;
        }
        return false;
    }

    // plays the AI's move (currently random)
    public void moveAI() {
        int index = (int) (Math.random() * possibleMoves.size());
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                if (row * board.length + col == possibleMoves.get(index)) {
                    moveRow = row;
                    moveCol = col;
                    possibleMoves.remove(index);
                    board[moveRow][moveCol] = ":o:";
                    return;
                }
            }
        }
    }

    // checks for a win
    public boolean checkWin(int row, int col, String symbol) {
        // col win
        for (int i = 0; i < board.length; i++) {
            if (!board[i][col].equals(symbol))
                break;
            else if (i == board.length - 1)
                return true;
        }

        // row win
        for (int i = 0; i < board.length; i++) {
            if (!board[row][i].equals(symbol))
                break;
            else if (i == board.length - 1)
                return true;
        }

        // diag win
        if (row == col) {
            for (int i = 0; i < board.length; i++) {
                if (!board[i][i].equals(symbol))
                    break;
                else if (i == board.length - 1)
                    return true;
            }
        }

        // other diag win
        if (row + col == board.length - 1) {
            for (int i = 0; i < board.length; i++) {
                if (!board[i][board.length - 1 - i].equals(symbol))
                    break;
                else if (i == board.length - 1)
                    return true;
            }
        }
        return false;
    }

    // returns formatted board for discord output
    public MessageEmbed toEmbed() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(playerName + "'s TicTacToe Game");
        eb.setColor(new Color(80, 255, 236));

        StringBuilder boardString = new StringBuilder();
        for (String[] row : board) {
            for (String cell : row) {
                boardString.append(cell);
            }
            boardString.append("\n");
        }
        boardString = new StringBuilder(boardString.substring(0, boardString.length() - 1));

        eb.addField("", boardString.toString(), true);
        eb.addBlankField(true);
        eb.addField("", ":x: " + playerName + "\n:o: Mehme", true);

        return eb.build();
    }
}
