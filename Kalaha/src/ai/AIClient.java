package ai;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import kalaha.Commands;
import kalaha.Errors;
import kalaha.GameState;
import kalaha.KalahaMain;

/**
 * This is the main class for your Kalaha AI bot. Currently it only makes a
 * random, valid move each turn.
 * 
 * @author Johan Hagelbäck
 */
public class AIClient implements Runnable {
	private int player;
	private JTextArea text;

	private PrintWriter out;
	private BufferedReader in;
	private Thread thr;
	private Socket socket;
	private boolean running;
	private boolean connected;

	/**
	 * Creates a new client.
	 */
	public AIClient() {
		player = -1;
		connected = false;

		// This is some necessary client stuff. You don't need
		// to change anything here.
		initGUI();

		try {
			addText("Connecting to localhost:" + KalahaMain.port);
			socket = new Socket("localhost", KalahaMain.port);
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			addText("Done");
			connected = true;
		} catch (Exception ex) {
			addText("Unable to connect to server");
			return;
		}
	}

	/**
	 * Starts the client thread.
	 */
	public void start() {
		// Don't change this
		if (connected) {
			thr = new Thread(this);
			thr.start();
		}
	}

	/**
	 * Creates the GUI.
	 */
	private void initGUI() {
		// Client GUI stuff. You don't need to change this.
		JFrame frame = new JFrame("My AI Client");
		frame.setLocation(Global.getClientXpos(), 445);
		frame.setSize(new Dimension(420, 250));
		frame.getContentPane().setLayout(new FlowLayout());

		text = new JTextArea();
		JScrollPane pane = new JScrollPane(text);
		pane.setPreferredSize(new Dimension(400, 210));

		frame.getContentPane().add(pane);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.setVisible(true);
	}

	/**
	 * Adds a text string to the GUI textarea.
	 * 
	 * @param txt The text to add
	 */
	public void addText(String txt) {
		// Don't change this
		text.append(txt + "\n");
		text.setCaretPosition(text.getDocument().getLength());
	}

	/**
	 * Thread for server communication. Checks when it is this client's turn to make
	 * a move.
	 */
	public void run() {
		String reply;
		running = true;

		try {
			while (running) {
				// Checks which player you are. No need to change this.
				if (player == -1) {
					out.println(Commands.HELLO);
					reply = in.readLine();

					String tokens[] = reply.split(" ");
					player = Integer.parseInt(tokens[1]);

					addText("I am player " + player);
				}

				// Check if game has ended. No need to change this.
				out.println(Commands.WINNER);
				reply = in.readLine();
				if (reply.equals("1") || reply.equals("2")) {
					int w = Integer.parseInt(reply);
					if (w == player) {
						addText("I won!");
					} else {
						addText("I lost...");
					}
					running = false;
				}
				if (reply.equals("0")) {
					addText("Even game!");
					running = false;
				}

				// Check if it is my turn. If so, do a move
				out.println(Commands.NEXT_PLAYER);
				reply = in.readLine();
				if (!reply.equals(Errors.GAME_NOT_FULL) && running) {
					int nextPlayer = Integer.parseInt(reply);

					if (nextPlayer == player) {
						out.println(Commands.BOARD);
						String currentBoardStr = in.readLine();
						boolean validMove = false;
						while (!validMove) {
							long startT = System.currentTimeMillis();
							// This is the call to the function for making a move.
							// You only need to change the contents in the getMove()
							// function.
							GameState currentBoard = new GameState(currentBoardStr);
							int cMove = getMove(currentBoard);

							// Timer stuff
							long tot = System.currentTimeMillis() - startT;
							double e = (double) tot / (double) 1000;

							out.println(Commands.MOVE + " " + cMove + " " + player);
							reply = in.readLine();
							if (!reply.startsWith("ERROR")) {
								validMove = true;
								addText("Made move " + cMove + " in " + e + " secs");
							}
						}
					}
				}

				// Wait
				Thread.sleep(100);
			}
		} catch (Exception ex) {
			running = false;
		}

		try {
			socket.close();
			addText("Disconnected from server");
		} catch (Exception ex) {
			addText("Error closing connection: " + ex.getMessage());
		}
	}

	/**
	 * This is the method that makes a move each time it is your turn. Here you need
	 * to change the call to the random method to your Minimax search.
	 * 
	 * @param currentBoard The current board state
	 * @return Move to make (1-6)
	 */

	// We are aiming for B grade (alpha-beta pruning with time constraint)

	public int max_player = 1; // I have considered first player as maximum player
	public int min_player = 2;// I have considered second player as minimum player
	public int maximumTimeForEveryMove = 5; // Maximum time for every move is 5

	public int getMove(GameState currentBoard) {

		long startTime = System.currentTimeMillis(); // I have assigned current time in milliseconds to
														// variable(startTime)
		Utility_Object bestMove = new Utility_Object();// I have initialized the object to compare the best moves in
														// perticular time(max time)

		if (player == max_player)
			bestMove.setEval_score(Integer.MIN_VALUE); // to comparision, max player is assigning to -infinity
		else if (player == min_player)
			bestMove.setEval_score(Integer.MAX_VALUE);// to comparision, min player is assigning to +infinity

		// This loop Iterate till the maximum execution time and returns best move.
		while (maximumTimeForEveryMove >= ((double) (System.currentTimeMillis() - startTime) / (double) 1000)) {

			Utility_Object uo = miniMaxAlgorithmImplementation(currentBoard.clone(), 6, player, Integer.MIN_VALUE,
					Integer.MAX_VALUE, startTime);// we are taking 6 as depth

			if (player == max_player && uo.getEval_score() > bestMove.getEval_score()) {
				bestMove.setEval_score(uo.getEval_score());
				bestMove.setAmbo_value(uo.getAmbo_value());
			} // comparision for maximum player since the values are +ve always

			if (player == min_player && uo.getEval_score() < bestMove.getEval_score()) {
				bestMove.setEval_score(uo.getEval_score());
				bestMove.setAmbo_value(uo.getAmbo_value());
			} // comparision for minimum player since the values are -ve always

		}
		return bestMove.getAmbo_value(); // return the best ambo value after executing for maximum time
	}

	/*
	 * The below miniMaxAlgoritmImplementation takes
	 * 
	 * @param gs- GameState object
	 * 
	 * @param depth- depth for the search
	 * 
	 * @param player- current player either Ai is maximizer or minimizer
	 * 
	 * @param alpha- alpha value
	 * 
	 * @param beta- beta value
	 * 
	 * @startTime - system time is sent to function.
	 * 
	 * returns the Utility Object which consits of score, best move.
	 * 
	 * 
	 * Algoritm Implementation::::
	 * 
	 * function minimax(position,depth,maximizingPlayer,alpha,beta)
	 * 
	 * if depth==0 or game over { return the static score }
	 * 
	 * if maximizingplayer { maxValue= -Infinity
	 * 
	 * for each child of position
	 * eval=minimax(position,depth-1,maximizingPlayer,alpha,beta)
	 * maxValue=max(maxValue,eval) alpha=max(alpha,eval)
	 * 
	 * if beta<=alpha break return maxValue }
	 *
	 * 
	 * else {minValue= +Infinity for each child of position
	 * eval=minimax(position,depth-1,maximizingPlayer,alpha,beta)
	 * minValue=min(minValue,eval) beta=min(beta,eval)
	 * 
	 * if beta<=alpha break
	 * 
	 * return minValue }
	 */

	public Utility_Object miniMaxAlgorithmImplementation(GameState gs, int depth, int player, int alpha, int beta,
			long startTime) {

		// int nextPlayer = gs.getNextPlayer();

		int bestPossibleMove = 1;

		if (gs.gameEnded() || depth == 0
				|| maximumTimeForEveryMove <= ((double) (System.currentTimeMillis() - startTime) / (double) 1000)) {

			Utility_Object uo = new Utility_Object();

			uo.setEval_score((player == max_player) ? gs.getScore(max_player) - gs.getScore(min_player)
					: gs.getScore(min_player) - gs.getScore(max_player));

			return uo;
		}

		if (player == max_player) {

			int max_value = Integer.MIN_VALUE;

			int z = 1;

			while (z < 7) {

				if (gs.moveIsPossible(z)) {

					GameState gameState = gs.clone();
					gameState.makeMove(z);
					int nextPlayer = gameState.getNextPlayer();

					Utility_Object uo = miniMaxAlgorithmImplementation(gameState, depth - 1, nextPlayer, alpha, beta,
							startTime);

					// max_value=Math.max(uo.eval,max_value);

					if (uo.getEval_score() > max_value) {
						max_value = uo.getEval_score();
						bestPossibleMove = z;
					}

					if (uo.getEval_score() > alpha) {
						alpha = uo.getEval_score();
					}

					if (beta <= alpha)
						break;

				}
				z++;
			}

			return new Utility_Object(max_value, bestPossibleMove);

		} else {

			int min_value = Integer.MAX_VALUE;

			int z = 1;

			while (z < 7) {
				if (gs.moveIsPossible(z)) {

					GameState gameState = gs.clone();
					gameState.makeMove(z);
					int nextPlayer = gameState.getNextPlayer();
					Utility_Object uo = miniMaxAlgorithmImplementation(gameState, depth - 1, nextPlayer, alpha, beta,
							startTime);

					// min_value=Math.min(uo.eval,min_value);

					if (uo.getEval_score() < min_value) {
						min_value = uo.getEval_score();
						bestPossibleMove = z;
					}

					if (uo.getEval_score() < beta) {
						beta = uo.getEval_score();
					}

					if (beta <= alpha)
						break;

				}
				z++;
			}

			return new Utility_Object(min_value, bestPossibleMove);
		}

	}

	/**
	 * Returns a random ambo number (1-6) used when making a random move.
	 * 
	 * @return Random ambo number
	 */
	public int getRandom() {
		return 1 + (int) (Math.random() * 6);
	}
}