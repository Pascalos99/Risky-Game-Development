package players.bots;

import gamerules.*;
import players.Player;
import players.bots.utils.ValueNode;

public class AlphaBeta extends Player {

    private GameState winning;

    private ValueNode alphabeta(GameTreeNode node, double alpha, double beta, boolean maximizingPlayer) {
        if (node.getGameState().hasWon(this) && node.getGameState().getDepth() == 1) {
            winning = node.getGameState();
        }
        if (node.getChildren().isEmpty()) {
            GameState state = node.getGameState();
            return new ValueNode(EvaluationFunction.SIMPLE_GOAL_DISTANCE.eval(state, this), node);
        }
        ValueNode valueNode;
        if (maximizingPlayer) {
            valueNode = new ValueNode(Double.NEGATIVE_INFINITY, null);
            for (GameTreeNode child : node.getChildren()) {
                valueNode = ValueNode.max(valueNode, alphabeta(child, alpha, beta, false));
                alpha = Math.max(alpha, valueNode.getValue());
                if (alpha >= beta) {
                    break;
                }
            }
        }
        else {
            valueNode = new ValueNode(Double.POSITIVE_INFINITY, null);
            for (GameTreeNode child : node.getChildren()) {
                valueNode = ValueNode.min(valueNode, alphabeta(child, alpha, beta, true));
                beta = Math.min(beta, valueNode.getValue());
                if (beta <= alpha) {
                    break;
                }
            }
        }
        return valueNode;
    }

    @Override
    public Move returnMove(Board gameBoard) {
        GameTree tree = new GameTree(new GameState(gameBoard));
        tree.setMaxExpansionTime(500);
        while (!tree.limitReached()) {
            tree.expandDeepest();
        }
        Move move;
        ValueNode valueNode = alphabeta(tree.getRoot(), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, true);
        if (winning != null) {
            System.out.println("Winning node");
            move = winning.getMoveSequence().get(0);
        }
        else {
            System.out.println("Best node");
            move = valueNode.getGameTreeNode().getGameState().getMoveSequence().get(0);
        }
        return move;
    }

    @Override
    public String getTypeName() {
        return "Alpha-beta pruning";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public Player getNewInstance() {
        return new AlphaBeta();
    }

}