package players.bots;

import gamerules.*;
import players.Player;

import java.util.List;

public class AlphaBeta extends Player {

    private double alphabeta(GameTreeNode node, double alpha, double beta, boolean maximizingPlayer) {
        if (node.getChildren().isEmpty()) {
            GameState state = node.getGameState();
            return EvaluationFunction.SIMPLE_GOAL_DISTANCE.eval(state, this);
        }
        double value;
        if (maximizingPlayer) {
            value = Double.NEGATIVE_INFINITY;
            for (GameTreeNode child : node.getChildren()) {
                value = Math.max(value, alphabeta(child, alpha, beta, false));
                alpha = Math.max(alpha, value);
                if (alpha >= beta) {
                    break;
                }
            }
        }
        else {
            value = Double.POSITIVE_INFINITY;
            for (GameTreeNode child : node.getChildren()) {
                value = Math.min(value, alphabeta(child, alpha, beta, true));
                beta = Math.min(beta, value);
                if (beta <= alpha) {
                    break;
                }
            }
        }
        return value;
    }

    @Override
    public Move returnMove(Board gameBoard) {
        GameTree tree = new GameTree(new GameState(gameBoard));
        tree.setMaxExpansionTime(500);
        while (!tree.limitReached()) {
            System.out.println("Expanding deepest layer");
            tree.expandDeepest();
        }

        List<GameTreeNode> children = tree.getRoot().getChildren();
        double[] values = new double[children.size()];
        double max = Double.NEGATIVE_INFINITY;
        int index = -1;

        for (int i = 0; i < children.size(); i++) {
            values[i] = alphabeta(children.get(i), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, false);

            if (values[i] > max) {
                max = values[i];
                index = i;
            }
            System.out.println(values[i]);
        }

        //double value = alphabeta(tree.getRoot(), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, true);
        //System.out.println(value);
        return children.get(index).getGameState().getMoveSequence().get(0);
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