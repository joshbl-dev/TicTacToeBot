import net.dv8tion.jda.api.entities.User;

import java.util.ArrayList;

public class TicTacToe {

    private User player;
    private String[][] board;
    private int moveRow;
    private int moveCol;

    private ArrayList<Integer> possibleMoves;

    private static final String[] NUM_TO_EMOJI = {":zero:", ":one:", ":two:", ":three:", ":four:", ":five:", ":six:", ":seven:", ":eight:"};

    private int findMoveIndex(int move) {
        for (int  i = 0; i < possibleMoves.size(); i++) {
            if (possibleMoves.get(i) == move)
                return i;
        }
        return -1;
    }

    public TicTacToe(User user) {
        player = user;
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

    public User getPlayer() {
        return player;
    }

    public int getMoveCol() {
        return moveCol;
    }

    public int getMoveRow() {
        return moveRow;
    }

    public boolean movesLeft() {
        return possibleMoves.size() != 0;
    }

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
                            System.out.println(possibleMoves);
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

    public void moveAI() {
        int index = (int) (Math.random() * possibleMoves.size());
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                if (row * board.length + col == possibleMoves.get(index)) {
                    moveRow = row;
                    moveCol = col;
                    System.out.println(possibleMoves);
                    possibleMoves.remove(index);
                    board[moveRow][moveCol] = ":o:";
                    return;
                }
            }
        }
    }
    
    public boolean checkWin(int row, int col, String symbol) {
        System.out.println("Checking for win " + symbol);
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
        // col win
        if (row == col) {
            for (int i = 0; i < board.length; i++) {
                if (!board[i][i].equals(symbol))
                    break;
                else if (i == board.length - 1)
                    return true;
            }
        }
        // other col win
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

    public String toString() {
        String boardString = "";
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                boardString += board[row][col];
            }
            boardString += "\n";
        }
        boardString = boardString.substring(0, boardString.length() - 1);
        return boardString;
    }
}
