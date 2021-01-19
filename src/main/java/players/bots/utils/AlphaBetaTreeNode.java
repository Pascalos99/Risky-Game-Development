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
        this.children = new ArrayList<>();
        this.gameState = gameState;
        this.evaluationScore = evaluationScore;
    }

    public AlphaBetaTreeNode(GameState gameState, List<AlphaBetaTreeNode> children) {
        this.parent = null;
        this.children = children;
        this.gameState = gameState;
        this.evaluationScore = Double.NEGATIVE_INFINITY;
    }

    public AlphaBetaTreeNode(AlphaBetaTreeNode parent, GameState gameState, double evaluationScore) {
        this.parent = parent;
        parent.addChild(this);
        this.children = new ArrayList<>();
        this.gameState = gameState;
        this.evaluationScore = evaluationScore;
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

    public AlphaBetaTreeNode fetchChildWithState(GameState state) {
        for (AlphaBetaTreeNode childNode : children) {
            if (childNode.getGameState().contentEquals(state)) {
                return childNode;
            }
        }
        return null;
    }

}