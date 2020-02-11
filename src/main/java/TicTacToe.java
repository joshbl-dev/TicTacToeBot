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
            System.arraycopy(NUM_TO_EMOJI, row * board.length, board[row], 0, board[row].length);
        }

        if (Math.random() < .5) {
            moveAI();
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
        if (movesLeft()) {
            try {
                int moveNum = Integer.parseInt(move);
                if (moveNum >= 0 && moveNum <= 8) {
                    for (int row = 0; row < board.length; row++) {
                        for (int col = 0; col < board[row].length; col++) {
                            if (row * board.length + col == moveNum && openSpace(row, col)) {
                                placeMove(row, col, findMoveIndex(moveNum), ":x:");
                                return true;
                            }
                            else if (row * board.length + col == Integer.parseInt(move))
                                return false;
                        }
                    }
                }
            }
            catch (NumberFormatException e) {
                return false;
            }
        }
        return false;
    }

    // returns if open space on board
    private boolean openSpace(int row, int col) {
        return !board[row][col].equals(":x:") && !board[row][col].equals(":o:");
    }

    // places move on board
    private void placeMove(int row, int col, int index, String symbol) {
        moveRow = row;
        moveCol = col;
        board[moveRow][moveCol] = symbol;
        possibleMoves.remove(index);
    }

    // attempts a move in row/col for win
    private boolean attemptLineMove(String symbol, boolean isRow) {
        int count;
        int possibleMoveRow = 0;
        int possibleMoveCol = 0;
        for (int i = 0; i < board.length; i++) {
            count = 0;
            for (int j = 0; j < board[i].length; j++) {
                if (isRow && board[i][j].equals(symbol))
                    count++;
                else if (!isRow && board[j][i].equals(symbol))
                    count++;
                else {
                    if (isRow) {
                        possibleMoveRow = i;
                        possibleMoveCol = j;
                    }
                    else {
                        possibleMoveRow = j;
                        possibleMoveCol = i;
                    }
                }
                if (count == 2 && j == board[i].length - 1 && openSpace(possibleMoveRow, possibleMoveCol)) {
                    placeMove(possibleMoveRow, possibleMoveCol, findMoveIndex(possibleMoveRow * board.length + possibleMoveCol), ":o:");
                    return true;
                }
            }
        }
        return false;
    }

    // attempts a move in diagonals for win
    private boolean attemptDiagMove(String symbol, boolean isDiagNorm) {
        int count = 0;
        int possibleMoveRow = 0;
        int possibleMoveCol = 0;
        for (int i = 0; i < board.length; i++) {
            if (isDiagNorm && board[i][i].equals(symbol))
                count++;
            else if (!isDiagNorm && board[i][board.length - 1 - i].equals(symbol))
                count++;
            else {
                if (isDiagNorm) {
                    possibleMoveRow = i;
                    possibleMoveCol = i;
                }
                else {
                    possibleMoveRow = i;
                    possibleMoveCol = board.length - 1 - i;
                }
            }
            if (count == 2 && i == board.length - 1 && openSpace(possibleMoveRow, possibleMoveCol)) {
                placeMove(possibleMoveRow, possibleMoveCol, findMoveIndex(possibleMoveRow * board.length + possibleMoveCol), ":o:");
                return true;
            }
        }
        return false;
    }

    // returns if symbol can win next turn
    private boolean noNextTurnWin(String symbol) {
        if (attemptLineMove(symbol, true))
            return false;
        else if (attemptLineMove(symbol, false))
            return false;
        else if (attemptDiagMove(symbol, true))
            return false;
        else
            return !attemptDiagMove(symbol, false);
    }

    // plays the AI's move (currently random)
    public void moveAI() {
        if (movesLeft()) {
            // try to win/block
            if (noNextTurnWin(":o:") && noNextTurnWin(":x:")) {
                // try to go center
                if (Math.random() < .5 && openSpace(1, 1)) {
                    System.out.println("Moving bot center...");
                    placeMove(1, 1, findMoveIndex(4), ":o:");
                }
                // else go randomly
                else {
                    System.out.println("Moving bot randomly...");
                    int index = (int) (Math.random() * possibleMoves.size());
                    for (int row = 0; row < board.length; row++) {
                        for (int col = 0; col < board[row].length; col++) {
                            if (row * board.length + col == possibleMoves.get(index)) {
                                placeMove(row, col, index, ":o:");
                                return;
                            }
                        }
                    }
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