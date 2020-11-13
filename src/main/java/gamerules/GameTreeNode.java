package gamerules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameTreeNode {

    private final GameState gameState;
    private final GameTreeNode parent;
    protected ArrayList<GameTreeNode> children;
    
    protected boolean isExpanded = false;

    public GameTreeNode(GameTreeNode parent, GameState gameState) {
    	if (parent == null);
    	else if (!parent.gameState.equals(gameState.getPrevious())) {
            throw new IllegalArgumentException("GameState parent doesn't match GameState of parent\n>:(");
        }
        this.gameState = gameState;
        this.parent = parent;
        this.children = new ArrayList<>();
    }

    public GameTreeNode(GameTreeNode parent, Move move) {
    	this(parent, new GameState(parent.gameState, move));
    }

    public GameState getGameState() {
        return this.gameState;
    }

    public GameTreeNode getParent() {
        return this.parent;
    }
    
    public GameTreeNode getStateAfterMove(Move m) {
    	return new GameTreeNode(this, gameState.getStateAfterMove(m));
    }

    public List<GameTreeNode> getChildren() {
        return Collections.unmodifiableList(this.children);
    }
    
    public boolean isRoot() {
    	return parent == null;
    }
    
    public int getDepth() {
    	return gameState.getDepth();
    }
    
    public boolean isExpanded() {
    	return isExpanded;
    }

}
