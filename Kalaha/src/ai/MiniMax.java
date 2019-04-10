package ai;

import kalaha.*;

import java.util.ArrayList;
import java.util.Comparator;
import javafx.util.Pair;

/**
 *
 * @author Philipp Fischbeck, Johannes Frohnhofen
 */
class MiniMax {
    private int bestMove;
    private int bestValue;
    private int levelBestMove;
    private int levelBestValue;
    private final int player;
    private final int opponent;
    private final AIClient client;
    private long startTime;
    
    // More than half the seeds
    private static final int SEEDS_TO_WIN = 37;
    
    // Maximal time in milliseconds
    private static final long MAX_TIME = 5000;

    /**
     * Creates a new tree/graph for the given GameState in order to calculate the best move.
     * @param player The player that we play as
     * @param client The client (needed for text output)
     */
    MiniMax(int player, AIClient client) {
        this.player = player;
        // Opposite of 1 is 2, opposite of 2 is 1
        this.opponent = 3 - player;
        this.client = client;
    }

    /**
     * Calculates and returns the (according to the algorithm) best move
     * @param currentBoard The GameState to examine
     * @return The best move (int between 1 and 6)
     */
    public int findBestMove(GameState currentBoard) {
        runAlgorithm(currentBoard);
        return bestMove;
    }

    /**
     * Runs the minimax algorithm
     * @param currentBoard The GameState to examine
     */
    private void runAlgorithm(GameState currentBoard) {
        startTime = System.currentTimeMillis();
        
        // Save best value and move of the current level/depth
        levelBestMove = validMove(currentBoard);
        levelBestValue = Integer.MIN_VALUE;
        
        int depth = 0;
        
        while (!shouldStop()) {
            depth++;
            
            levelBestValue = miniMax(currentBoard, depth, Integer.MIN_VALUE, Integer.MAX_VALUE, true);
            
            // Use new value/move if it is better than the previous best or we reached full depth (because there is still time left)
            if (levelBestValue > bestValue || !shouldStop()) {
                bestMove = levelBestMove;
                bestValue = levelBestValue;
            }
            
            // Stop searching deeper if win is possible or loss is inevitable
            if (levelBestValue == Integer.MIN_VALUE || levelBestValue == Integer.MAX_VALUE)
                break;
                
        }
        
        client.addText("Found best move with depth of " + depth + ", value of " + bestValue);
    }
    
    /**
     * Recursively evaluate the given GameState based on the minimax algorithm with alpha-beta pruning.
     * @param currentBoard The GameState to evaluate
     * @param remainingDepth The remaining depth
     * @param alpha The alpha valule
     * @param beta The beta value
     * @param topLevel Whether this is the top level (root) of the game tree
     * @return The rating of the GameState. Higher is better for the max player.
     */
    private int miniMax(GameState currentBoard, int remainingDepth, int alpha, int beta, boolean topLevel) {
        final boolean isMaximizer = (player == currentBoard.getNextPlayer());
        
        // Check whether this is a terminal node
        if (isGameDecided(currentBoard))
            return utility(currentBoard);
        
        // Check whether the maximum depth is reached
        if (remainingDepth == 0)
        {
            return evaluation(currentBoard);
        }
        
        // Check whether time is up
        if (shouldStop())
            // If we don't have more time for expanding, assume worst-case if this is a minimizer
            // Otherwise use evaluation function
            return isMaximizer ?  evaluation(currentBoard): Integer.MIN_VALUE;
        
        int value = isMaximizer ? alpha : beta;
        
        // Create a list of the possible moves, the corresponding game states and their evaluation values
        
        ArrayList<Pair<Integer, Integer>> pairs = new ArrayList();
        GameState[] gameStates = new GameState[6];
                
        for (int i = 1; i <= 6; i++) {
            gameStates[i-1] = currentBoard.clone();
            if (gameStates[i-1].makeMove(i)) {
                pairs.add(new Pair(i, evaluation(gameStates[i-1])));
            }
        }
        
        // Sort the list in order to improve pruning chances
        // Sorting order is based on whether this is a maximizer or minimizer
        pairs.sort(new Comparator<Pair<Integer, Integer>>() {
            @Override
            public int compare(final Pair<Integer, Integer> o1, final Pair<Integer, Integer> o2) {
                return (o2.getValue() - o1.getValue()) * (isMaximizer ? 1 : -1);
            }
        });
        
        // Recursively iterate over the possible moves
        for (Pair<Integer, Integer> pair : pairs)
        {
            GameState newBoard = gameStates[pair.getKey() - 1];
            if (isMaximizer) {
                int newValue = miniMax(newBoard, remainingDepth - 1, value, beta, false);
                // Save best move if we are at the root
                if (newValue > value && topLevel)
                    levelBestMove = pair.getKey();
                value = Math.max(value, newValue);
                // Beta pruning
                if (value >= beta)
                    break;
            } else {
                int newValue = miniMax(newBoard, remainingDepth - 1, alpha, value, false);
                value = Math.min(value, newValue);
                // Alpha pruning
                if (value <= alpha)
                    break;
            }
        }
        
        return value;
    }

    /**
     * Returns a utility value based on the winner of the game state.
     * @param currentBoard The GameState to check.
     * @return The utility value. Positive infinity if we are winning, negative infinity if the opponent is winning. 0 if it is a draw or the game hasn't ended yet.
     */
    private int utility(GameState currentBoard) {
        if (currentBoard.getWinner() == player || currentBoard.getScore(player) >= SEEDS_TO_WIN)
            return Integer.MAX_VALUE;
        if (currentBoard.getWinner() == opponent || currentBoard.getScore(opponent) >= SEEDS_TO_WIN)
            return Integer.MIN_VALUE;;
        return 0;
    }

    /**
     * Forward the evaluation request to the GameStateEvaluator
     * @param currentBoard The GameState to evaluate
     * @return The evaluation value. Positive values are better for us.
     */
    private int evaluation(GameState currentBoard) {
        return GameStateEvaluator.evaluation(currentBoard, player, opponent);
    }

    /**
     * Checks whether the given time for move calculation is over.
     * @return Whether the algorithm should stop its calculations.
     */
    private boolean shouldStop() {
        return System.currentTimeMillis() - startTime >= MAX_TIME;
    }
    
    /**
     * Calculates a valid move for the given GameSate.
     * @param currentBoard The GameState to check
     * @return A valid move.
     */
    private int validMove(GameState currentBoard) {
        for (int i = 1; i <= 6; i++)
            if (currentBoard.moveIsPossible(i))
                return i;
        return 0;
    }

    /**
     * Checks whether a certain GameState already is decided. This can either mean that the game is over or that a player has more than half of the seeds.
     * @param currentBoard The GameState to be checked
     * @return Whether the game is decided
     */
    private static boolean isGameDecided(GameState currentBoard) {
        return currentBoard.gameEnded() || currentBoard.getScore(1) >= SEEDS_TO_WIN || currentBoard.getScore(2) >= SEEDS_TO_WIN;
    }
}
