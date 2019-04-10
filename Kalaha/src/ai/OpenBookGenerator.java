package ai;

import kalaha.*;

public class OpenBookGenerator {

    public static void main(String[] args) {
        OpenBook openBook = new OpenBook();
        // Generate an opening book with a depth of 5
        buildGameTree(openBook.getRootNode(), 5);
        openBook.writeToFile("openBook.txt");
    }

    public static void buildGameTree(Node node, int level) {
        if (level == 0)
            return;

        for (int i = 1; i <= 6; i++) {
            GameState gameState = node.getGameState().clone();
            if (gameState.makeMove(i)) {
                Node childNode = new Node(gameState);
                node.setChild(i, childNode);
                buildGameTree(childNode, level - 1);
            }
        }
    }

}