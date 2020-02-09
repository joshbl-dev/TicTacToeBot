import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TicTacToeUpdater {

	private ArrayList<TicTacToe> tttGames;
	private ScheduledExecutorService scheduledExecutorService;

	public TicTacToeUpdater() {
		onStart();
		startSaving();
	}

	// loads all ongoing tictactoe games
	private void onStart() {
		try {
			System.out.println("\nNow loading bot data...");
			FileInputStream fileInputStream = new FileInputStream("src/main/MehmeData");
			ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
			tttGames = (ArrayList<TicTacToe>) objectInputStream.readObject();
			System.out.println("Loaded " + tttGames.size() + " TicTacToe games");
			System.out.println("Completed loading bot data!\n");
		}
		catch (IOException | ClassNotFoundException e) {
			System.out.println(e.toString());
		}
	}

	// enables the serialization and saving process that runs every 30 seconds
	private void startSaving() {
		scheduledExecutorService = Executors.newScheduledThreadPool (1);
		Runnable saveDataRunnable = () -> {
			try {
				System.out.println("\nNow saving bot data...");
				FileOutputStream fileOutputStream = new FileOutputStream("src/main/MehmeData");
				ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
				objectOutputStream.writeObject(tttGames);
				System.out.println("Saved " + tttGames.size() + " TicTacToe games");
				objectOutputStream.close();
				fileOutputStream.close();
				System.out.println("Completed saving bot data!\n");

			}
			catch(IOException e) {
				System.out.println(e.toString());
			}
		};
		scheduledExecutorService.scheduleAtFixedRate(saveDataRunnable, 10, 30, TimeUnit.SECONDS);
	}

	// finds player's board from list of ongoing games
	private TicTacToe getPlayerBoard(User user) {
		for (TicTacToe game : tttGames) {
			if (game.getPlayerID().equals(user.getId())) {
				return game;
			}
		}
		return null;
	}

	// removes player from the list of ongoing games
	private void removePlayerBoard(User user) {
		for (int i = 0; i < tttGames.size(); i++) {
			if (tttGames.get(i).getPlayerID().equals(user.getId()))
				tttGames.remove(i);
		}
	}

	// tictactoe message commands
	public void onMessageReceived(MessageReceivedEvent event) {
		MessageChannel channel = event.getChannel();
		User author = event.getAuthor();
		String[] messagePhrases = event.getMessage().getContentDisplay().toLowerCase().split(" ");

		TicTacToe board = getPlayerBoard(author);

		// normal commands

		// help
		if (messagePhrases.length >= 2 && messagePhrases[1].equals("help")) {
			EmbedBuilder eb = new EmbedBuilder();
			eb.setTitle("Mehme TicTacToe Instruction:");
			eb.setColor(new Color(80, 255, 236));
			eb.addField("**Start a game**", "!ttt start", false);
			eb.addField("**Make a move**", "!ttt move [#]", false);
			eb.addField("**Get game board**", "!ttt get", false);
			channel.sendMessage(eb.build()).queue();
		}
		// start
		else if (messagePhrases.length >= 2 && messagePhrases[1].equals("start")) {
			if (board == null) {
				channel.sendMessage("Creating game for <@" + author.getId() + ">").queue();
				tttGames.add(board = new TicTacToe(author));
			}
			else
				channel.sendMessage("Game already created for <@" + author.getId() + ">").queue();
			channel.sendMessage(board.toEmbed()).queue();
		}
		// move
		else if (messagePhrases.length >= 3 && messagePhrases[1].equals("move")) {
			if (board == null)
				channel.sendMessage("No board found for <@" + author.getId() + ">. Create one with the command \"!start ttt\"").queue();
			else if (!board.movesLeft())
				channel.sendMessage("No moves remaining in <@" + author.getId() + ">'s TicTacToe game").queue();
			else if (board.playMove(messagePhrases[2].toLowerCase())) {
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
		else if (messagePhrases.length >= 2 && messagePhrases[1].equals("get")) {
			if (board == null)
				channel.sendMessage("No board found for <@" + author.getId() + ">. Create one with the command \"!start ttt\"").queue();
			else {
				channel.sendMessage(board.toEmbed()).queue();
			}
		}
		// end
		else if (messagePhrases.length >= 2 && messagePhrases[1].equals("end")) {
			removePlayerBoard(author);
			channel.sendMessage("<@" + author.getId() + ">'s TicTacToe game has ended").queue();
		}

		// admin commands

		if (author.getId().equals("221748640236961792")) {
			// remove all/specific tictactoe games
			if (messagePhrases.length >= 3 && messagePhrases[1].equals("remove")) {
				List<Member> taggedMembers;
				if (messagePhrases[2].equals("all")) {
					System.out.println("Removing all TicTacToe games");
					channel.sendMessage("All TicTacToe games have been removed").queue();
					tttGames = new ArrayList<>();
				}
				else {
					taggedMembers = event.getMessage().getMentionedMembers();
					for (Member member : taggedMembers) {
						System.out.println("Removing " + member.getEffectiveName() + "'s TicTacToe game");
						channel.sendMessage("<@" + author.getId() + ">'s TicTacToe game has been removed").queue();
						removePlayerBoard(member.getUser());
					}
				}
			}
			// disable/enable saving
			else if (messagePhrases.length >= 3 && messagePhrases[1].equals("save")) {
				if (messagePhrases[2].equals("disable")) {
					System.out.println("Saving disabled");
					channel.sendMessage("Saving disabled").queue();
					scheduledExecutorService.shutdown();
				}
				else if (messagePhrases[2].equals("enable")) {
					System.out.println("Saving enabled");
					channel.sendMessage("Saving enabled").queue();
					startSaving();
				}
			}
		}
	}
}
