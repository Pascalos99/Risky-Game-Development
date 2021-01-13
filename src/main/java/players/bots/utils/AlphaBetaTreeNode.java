package players.bots.utils;

import gamerules.GameState;
import gamerules.Move;

import java.util.ArrayList;
import java.util.List;

public class AlphaBetaTreeNode {

    private AlphaBetaTreeNode parent;
    private final List<AlphaBetaTreeNode> children;

    private final GameState gameState;
    private final double evaluationScore;

    public AlphaBetaTreeNode(GameState gameState, double evaluationScore) {
        this.parent = null;
        this.evaluationScore = evaluationScore;
        this.gameState = gameState;
        this.children = new ArrayList<>();
    }

    public AlphaBetaTreeNode(GameState gameState, List<AlphaBetaTreeNode> children) {
        this.parent = null;
        this.evaluationScore = Double.NEGATIVE_INFINITY;
        this.gameState = gameState;
        this.children = children;
    }

    public AlphaBetaTreeNode(AlphaBetaTreeNode parent, GameState gameState, double evaluationScore) {
        this.parent = parent;
        parent.addChild(this);
        this.gameState = gameState;
        this.evaluationScore = evaluationScore;
        this.children = new ArrayList<>();
    }

    public List<Move> getAllPossibleMoves() {
        return gameState.getAllPossibleMoves(gameState.getAllPawnsOf(gameState.currentPlayer()));
    }

    public AlphaBetaTreeNode getParent() {
        return parent;
    }

    public void setParent(AlphaBetaTreeNode parent) {
        this.parent = parent;
    }

    public List<AlphaBetaTreeNode> getChildren() {
        return children;
    }

    public GameState getGameState() {
        return gameState;
    }

    public double getEvaluationScore() {
        return evaluationScore;
    }

    public void addChild(AlphaBetaTreeNode childNode) {
        children.add(childNode);
    }

    public void removeChild(AlphaBetaTreeNode childNode) {
        children.remove(childNode);
    }

    public AlphaBetaTreeNode max(AlphaBetaTreeNode nodeToCompareWith) {
        if (this.evaluationScore >= nodeToCompareWith.evaluationScore) {
            return this;
        }
        else {
            return nodeToCompareWith;
        }
    }

    public AlphaBetaTreeNode min(AlphaBetaTreeNode nodeToCompareWith) {
        if (this.evaluationScore < nodeToCompareWith.evaluationScore) {
            return this;
        }
        else {
            return nodeToCompareWith;
        }
    }

    public boolean isChildOf(AlphaBetaTreeNode node) {
        for (AlphaBetaTreeNode childNode : children) {
            if (childNode == node) {
                return true;
            }
        }
        return false;
    }

    public AlphaBetaTreeNode fetchChildWithState(GameState state) {
        for (AlphaBetaTreeNode childNode : children) {
            if (childNode.getGameState().contentEquals(state)) {
                return childNode;
            }
        }
        return null;
    }

}