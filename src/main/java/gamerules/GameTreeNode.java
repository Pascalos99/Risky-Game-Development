package gamerules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameTreeNode {

    private final GameState gameState;
    private final GameTreeNode parent;
    protected ArrayList<GameTreeNode> children;

    public GameTreeNode(GameTreeNode parent, GameState gameState) {
        if (!parent.gameState.equals(gameState.getPrevious())) {
            throw new IllegalArgumentException("GameState parent doesn't match GameState of parent\n>:(");
        }
        this.gameState = gameState;
        this.parent = parent;
        this.children = new ArrayList<>();
    }

    public GameTreeNode(GameTreeNode parent, Move move) {
        this.gameState = new GameState(parent.gameState, move);
        this.parent = parent;
        this.children = new ArrayList<>();
    }

    public GameState getGameState() {
        return this.gameState;
    }

    public GameTreeNode getParent() {
        return this.parent;
    }

    public List<GameTreeNode> getChildren() {
        return Collections.unmodifiableList(this.children);
    }

}
