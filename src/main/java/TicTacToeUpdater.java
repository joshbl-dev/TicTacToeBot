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
	private boolean saving;

	public TicTacToeUpdater() {
		onStart();
		startSaving();
	}

	// loads all ongoing tictactoe games
	@SuppressWarnings("unchecked")
	private void onStart() {
		try {
			System.out.println("\nNow loading bot data...");
			File srcFolder = new File("src");
			if (!srcFolder.exists()) {
				srcFolder.mkdir();
			}
			File dataFile = new File("src/MehmeData");
			if (!dataFile.exists()) {
				dataFile.createNewFile();
				FileOutputStream fileOutputStream = new FileOutputStream("src/MehmeData");
				ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
				objectOutputStream.writeObject(new ArrayList<TicTacToe>());
				objectOutputStream.close();
				fileOutputStream.close();
			}
			FileInputStream fileInputStream = new FileInputStream("src/MehmeData");
			ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
			System.out.println("MAde it to here");
			tttGames = (ArrayList<TicTacToe>) objectInputStream.readObject();
			if (tttGames == null)
				tttGames = new ArrayList<>();
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
					if (saving) {
						System.out.println("\nNow saving bot data...");
						FileOutputStream fileOutputStream = new FileOutputStream("src/MehmeData");
						ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
						objectOutputStream.writeObject(tttGames);
						System.out.println("Saved " + tttGames.size() + " TicTacToe games");
						objectOutputStream.close();
						fileOutputStream.close();
						System.out.println("Completed saving bot data!\n");
						unqueueSaving();
					}

				} catch (IOException e) {
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
	private boolean removePlayerBoard(User user) {
		for (int i = 0; i < tttGames.size(); i++) {
			if (tttGames.get(i).getPlayerID().equals(user.getId())) {
				tttGames.remove(i);
				queueSaving();
				return true;
			}
		}
		return false;
	}
	
	private void queueSaving() {
		if (!saving) {
			saving = true;
			System.out.println("Saving queued");
		}
	}
	
	private void unqueueSaving() {
		saving = false;
		System.out.println("Saving unqueued");
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
				queueSaving();
			}
			else
				channel.sendMessage("Game already created for <@" + author.getId() + ">").queue();
			channel.sendMessage(board.toEmbed()).queue();
		}
		// move
		else if (messagePhrases.length >= 3 && messagePhrases[1].equals("move")) {
			if (board == null)
				channel.sendMessage("No board found for <@" + author.getId() + ">. Create one with the command \"!ttt start\"").queue();
			else if (!board.movesLeft()) {
				channel.sendMessage("No moves remaining in <@" + author.getId() + ">'s TicTacToe game").queue();
				removePlayerBoard(author);
			}
			else if (board.playMove(messagePhrases[2].toLowerCase())) {
				if (board.checkWin(board.getMoveRow(), board.getMoveCol(), ":x:")) {
					channel.sendMessage(board.toEmbed()).queue();
					channel.sendMessage("<@" + author.getId() + "> has won TicTacToe!").queue();
					removePlayerBoard(author);
				}
				else if (!board.movesLeft()) {
					channel.sendMessage(board.toEmbed()).queue();
					channel.sendMessage("<@" + author.getId() + ">'s TicTacToe game ended in a tie!").queue();
					removePlayerBoard(author);
				}
				else {
					board.moveAI();
					if (board.checkWin(board.getMoveRow(), board.getMoveCol(), ":o:")) {
						channel.sendMessage(board.toEmbed()).queue();
						channel.sendMessage( "Mehme has won TicTacToe against <@" + author.getId() + ">").queue();
						removePlayerBoard(author);
					}
					else if (!board.movesLeft()) {
						channel.sendMessage(board.toEmbed()).queue();
						channel.sendMessage("<@" + author.getId() + ">'s TicTacToe game ended in a tie!").queue();
						removePlayerBoard(author);
					}
					else {
						queueSaving();
						channel.sendMessage(board.toEmbed()).queue();
					}
				}
			}
			else
				channel.sendMessage("<@" + author.getId() + "> Invalid syntax or move. Command syntax: \"!ttt move [number]\"").queue();
		}
		// get
		else if (messagePhrases.length >= 2 && messagePhrases[1].equals("get")) {
			if (board == null)
				channel.sendMessage("No board found for <@" + author.getId() + ">. Create one with the command \"!ttt start\"").queue();
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
					queueSaving();
				}
				else {
					taggedMembers = event.getMessage().getMentionedMembers();
					for (Member member : taggedMembers) {
						if (removePlayerBoard(member.getUser())) {
							System.out.println("Removing " + member.getEffectiveName() + "'s TicTacToe game");
							channel.sendMessage("<@" + member.getIdLong() + ">'s TicTacToe game has been removed").queue();
						}
						else
							channel.sendMessage("No board found for <@" + member.getIdLong() + ">").queue();
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