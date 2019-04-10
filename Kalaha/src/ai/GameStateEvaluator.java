package ai;

import kalaha.*;

public class GameStateEvaluator {
    
    /**
     * Evaluate the given GameState based on the situation rating of us and the opponent
     * @param currentBoard The GameState to evaluate
     * @param player The player whose situation should be rated
     * @param player Opponent of the player whose situation should be rated
     * @return The evaluation value. Positive values are better for us.
     */
    public static int evaluation(GameState currentBoard, int player, int opponent) {
        // Return diff of player situation ratings
        return rateSituation(currentBoard, player) - rateSituation(currentBoard, opponent);
    }

    /**
     * Rates the current situation of a player.
     * @param currentBoard The GameState to be rated
     * @param player The player whose situation should be rated
     * @return The rating. Positive values are better for the player.
     */
    private static int rateSituation(GameState currentBoard, int player) {
        // Number of seeds collected in the own house
        int inOwnHouse = currentBoard.getScore(player);

        // Number of seeds collected in the own ambos
        int inOwnAmbos = 0;
        for (int i = 1; i <= 6; i++)
            inOwnAmbos += currentBoard.getSeeds(i, player);

        // The coefficients have been derived using a genetic algorithm. 
        return (int)(83.1429105 * inOwnHouse + 16.8570895 * inOwnAmbos);
    }

}