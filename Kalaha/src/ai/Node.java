package ai;

import kalaha.*;

/**
 * Data structure for a tree of game states for the opening book
 * @author Philipp Fischbeck, Johannes Frohnhofen
 */
public class Node {
    private GameState gameState;
    private Node[] children;


    Node(String board) {
        this.gameState = new GameState(board);
        this.children = new Node[6];
    }

    Node(GameState gameState) {
        this.gameState = gameState;
        this.children = new Node[6];
    }

    GameState getGameState() {
        return this.gameState;
    }

    Node getChild(int index) {
        return this.children[index - 1];
    }

    void setChild(int index, Node child) {
        this.children[index - 1] = child;
    }
}
