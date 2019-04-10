package ai;

import kalaha.*;

import java.io.*;

/**
 * Class for operations on an opening book
 * @author Philipp Fischbeck, Johannes Frohnhofen
 */
public class OpenBook {
    private Node root;
    private int bestMove;
    private int player;
    private int opponent;

    /**
     * Create empty open book
     */
    OpenBook() {
        this.root = new Node(new GameState());
        this.player = 0;
        this.opponent = 0;
    }

    /**
     * Load open book from a file
     * @param filename Name of the file from which to load
     * @param player The player for which to decide the move
     * @param client The AI Client class
     */
    OpenBook(String filename, int player, AIClient client) {
        readFromFile(filename);
        client.addText(isValid() ? "I successfully read the OpenBook." : "I could not read the OpenBook.");
        this.player = player;
        // Opposite of 1 is 2, opposite of 2 is 1
        this.opponent = 3 - player;
    }

    public boolean readFromFile(String filename) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            root = readNode(reader);
            reader.close();
            return true;
        } catch(Exception e) {
            root = null;
            return false;
        }
    }

    public boolean writeToFile(String filename) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
            writeNode(root, writer);
            writer.close();
            return true;
        } catch(IOException e) {
            return false;
        }
    }

    /**
     * Finds and returns the best move for the given game state based on the open book
     * @param currentBoard The GameState
     * @return The best move
     */
    public int findBestMove(GameState currentBoard) {
        Node startingNode = find(this.root, currentBoard);
        miniMax(startingNode, player == currentBoard.getNextPlayer(), true);
        return this.bestMove;
    }

    public Node getRootNode() {
        return this.root;
    }

    public boolean isValid() {
        return this.root != null;
    }

    private Node readNode(BufferedReader reader) throws IOException {
        String line = reader.readLine();
        if(line.equals("null"))
            return null;
        // Read nodes from file recursively
        Node node = new Node(line);
        for(int i = 1; i <= 6; i++)
            node.setChild(i, readNode(reader));
        return node;
    }

    private void writeNode(Node node, BufferedWriter writer) throws IOException {
        if(node == null) {
            writer.write("null\n");
            return;
        }

        writer.write(node.getGameState().toString() + "\n");
        // Write nodes recursively
        for(int i = 1; i <= 6; i++)
            writeNode(node.getChild(i), writer);
    }

    /**
     * Recursively searches for a node with a certain GameState in the opening book
     * @param node The current node that is examined
     * @param currentBoard The GameState to look for
     * @return The corresponding node if found, null otherwise
     */
    private Node find(Node node, GameState currentBoard) {
        if(node == null || node.getGameState().toString().equals(currentBoard.toString()))
            return node;

        for(int i = 1; i <= 6; i++) {
            Node result = find(node.getChild(i), currentBoard);
            if(result != null)
                return result;
        }

        return null;
    }

    /**
     * A mini max implementation similar to the MiniMax class, but using the moves from the opening book
     */
    private int miniMax(Node node, boolean isMaximizer, boolean topLevel) {
        if(node == null)
            return isMaximizer ? Integer.MAX_VALUE : Integer.MIN_VALUE;

        int value = isMaximizer ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        for(int i = 1; i <= 6; i++) {
            int newValue = miniMax(node.getChild(i), !isMaximizer, false);
            if(topLevel && newValue >= value)
                bestMove = i;
            value = isMaximizer ? Math.max(value, newValue) : Math.min(value, newValue);
        }

        if(value == Integer.MAX_VALUE || value == Integer.MIN_VALUE)
            return GameStateEvaluator.evaluation(node.getGameState(), player, opponent);
        else
            return value;
    }
}
