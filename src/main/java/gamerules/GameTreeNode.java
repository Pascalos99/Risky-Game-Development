package gamerules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameTreeNode implements Comparable<GameTreeNode>{

    private final GameState gameState;
    private final GameTreeNode parent;
    protected ArrayList<GameTreeNode> children;
    
    protected boolean isExpanded = false;
    
    private boolean addedToParent = false;

    private double fscore;
    private double gscore;

    public double getFscore() {
        return fscore;
    }

    public void setFscore(double fscore) {
        this.fscore = fscore;
    }

    public double getGscore() {
        return gscore;
    }

    public void setGscore(double gscore) {
        this.gscore = gscore;
    }

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
    
    public void addToParent() {
    	if (parent != null) parent.children.add(this);
    	addedToParent = true;
    }
    
    public boolean isAddedToParent() {
    	return addedToParent;
    }

    public void addChild(GameTreeNode childNode) {
        this.children.add(childNode);
    }

    @Override
    public int compareTo(GameTreeNode other) {
        return Double.compare(this.getFscore(),other.getFscore());
    }
}
