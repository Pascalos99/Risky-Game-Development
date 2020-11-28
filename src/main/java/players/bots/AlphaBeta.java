package players.bots;

import gamerules.*;
import players.Player;
import players.bots.utils.ValueNode;

import java.util.List;

public class AlphaBeta extends Player {

    private ValueNode alphabeta(GameTreeNode node, double alpha, double beta, boolean maximizingPlayer) {
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
            System.out.println("Expanding deepest layer");
            tree.expandDeepest();
        }

//        List<GameTreeNode> children = tree.getRoot().getChildren();
//        double[] values = new double[children.size()];
//        double max = Double.NEGATIVE_INFINITY;
//        int index = -1;
//
//        for (int i = 0; i < children.size(); i++) {
//            values[i] = alphabeta(children.get(i), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, false);
//
//            if (values[i] > max) {
//                max = values[i];
//                index = i;
//            }
//            System.out.println(values[i]);
//        }

        ValueNode valueNode = alphabeta(tree.getRoot(), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, true);
        System.out.println(valueNode.getValue());
        return valueNode.getGameTreeNode().getGameState().getMoveSequence().get(0);
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