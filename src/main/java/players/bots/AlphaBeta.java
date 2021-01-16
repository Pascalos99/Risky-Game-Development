package players.bots;

import gamerules.*;
import gamerules.evaluation_functions.SimpleGoalDistance;
import players.Player;
import players.bots.utils.AlphaBetaTree;
import players.bots.utils.AlphaBetaTreeNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AlphaBeta extends Player implements DeterministicReturn {

    private boolean initialCall = true;
    private GameState winning;
    private int numberOfPlayers;
    private AlphaBetaTree tree;
    private int currentDepth;

    private int nodesEncountered;
    private int duplicatesEncountered;
    private long timeOptimalOrder;
    private long timeDuplicateChecking;
    
    private EvaluationFunction evaluationFunction;
    
    public AlphaBeta() {
    	this(new SimpleGoalDistance());
    }
    public AlphaBeta(EvaluationFunction evaluationFunction) {
    	this.evaluationFunction = evaluationFunction;
    }

    // Bare bones
    private AlphaBetaTreeNode alphaBetaV1(AlphaBetaTreeNode node, double alpha, double beta, int player, int depth) {
        nodesEncountered++;
        // Depth-1 winning node
        if (node.getGameState().hasWon(this) && node.getGameState().getDepth() == 1) {
            winning = node.getGameState();
        }
        // Leaf node
        if (depth == 0) {
            return node;
        }
        AlphaBetaTreeNode bestValue;
        // Maximizing
        if (player == 1) {
            bestValue = new AlphaBetaTreeNode(null, Double.NEGATIVE_INFINITY);

            for (Move move : node.getAllPossibleMoves()) {
                GameState childState = node.getGameState().getStateAfterMove(move);
                double childScore = tree.computeEvaluationScore(childState);
                AlphaBetaTreeNode childNode = new AlphaBetaTreeNode(node, childState, childScore);

                bestValue = bestValue.max(alphaBetaV1(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
                alpha = Math.max(alpha, bestValue.getEvaluationScore());
                if (alpha >= beta) {
                    break;
                }
            }
        }
        // Minimizing
        else {
            bestValue = new AlphaBetaTreeNode(null, Double.POSITIVE_INFINITY);

            for (Move move : node.getAllPossibleMoves()) {
                GameState childState = node.getGameState().getStateAfterMove(move);
                double childScore = tree.computeEvaluationScore(childState);
                AlphaBetaTreeNode childNode = new AlphaBetaTreeNode(node, childState, childScore);

                bestValue = bestValue.min(alphaBetaV1(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
                beta = Math.min(beta, bestValue.getEvaluationScore());
                if (beta <= alpha) {
                    break;
                }
            }
        }
        return bestValue;
    }

    // Bare bones
    // + duplicate child prevention
    private AlphaBetaTreeNode alphaBetaV2(AlphaBetaTreeNode node, double alpha, double beta, int player, int depth) {
        nodesEncountered++;
        // Depth-1 winning node
        if (node.getGameState().hasWon(this) && node.getGameState().getDepth() == 1) {
            winning = node.getGameState();
        }
        // Leaf node
        if (depth == 0) {
            return node;
        }
        AlphaBetaTreeNode bestValue;
        // Maximizing
        if (player == 1) {
            bestValue = new AlphaBetaTreeNode(null, Double.NEGATIVE_INFINITY);

            if (node.getChildren().isEmpty()) {
                // Not expanded
                for (Move move : node.getAllPossibleMoves()) {
                    GameState childState = node.getGameState().getStateAfterMove(move);
                    double childScore = tree.computeEvaluationScore(childState);
                    AlphaBetaTreeNode childNode = new AlphaBetaTreeNode(node, childState, childScore);

                    bestValue = bestValue.max(alphaBetaV2(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
                    alpha = Math.max(alpha, bestValue.getEvaluationScore());
                    if (alpha >= beta) {
                        break;
                    }
                }
            }
            else if (node.getChildren().size() < node.getAllPossibleMoves().size()) {
                // Partially expanded
                for (Move move : node.getAllPossibleMoves()) {
                    GameState childState = node.getGameState().getStateAfterMove(move);
                    AlphaBetaTreeNode childNode = node.fetchChildWithState(childState);
                    if (childNode == null) {
                        double childScore = tree.computeEvaluationScore(childState);
                        childNode = new AlphaBetaTreeNode(node, childState, childScore);
                    }

                    bestValue = bestValue.max(alphaBetaV2(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
                    alpha = Math.max(alpha, bestValue.getEvaluationScore());
                    if (alpha >= beta) {
                        break;
                    }
                }
            }
            else {
                // Fully expanded
                for (AlphaBetaTreeNode childNode : node.getChildren()) {
                    bestValue = bestValue.max(alphaBetaV2(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
                    alpha = Math.max(alpha, bestValue.getEvaluationScore());
                    if (alpha >= beta) {
                        break;
                    }
                }
            }
        }
        // Minimizing
        else {
            bestValue = new AlphaBetaTreeNode(null, Double.POSITIVE_INFINITY);

            if (node.getChildren().isEmpty()) {
                // Not expanded
                for (Move move : node.getAllPossibleMoves()) {
                    GameState childState = node.getGameState().getStateAfterMove(move);
                    double childScore = tree.computeEvaluationScore(childState);
                    AlphaBetaTreeNode childNode = new AlphaBetaTreeNode(node, childState, childScore);

                    bestValue = bestValue.min(alphaBetaV2(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
                    beta = Math.min(beta, bestValue.getEvaluationScore());
                    if (beta <= alpha) {
                        break;
                    }
                }
            }
            else if (node.getChildren().size() < node.getAllPossibleMoves().size()) {
                // Partially expanded
                for (Move move : node.getAllPossibleMoves()) {
                    GameState childState = node.getGameState().getStateAfterMove(move);
                    AlphaBetaTreeNode childNode = node.fetchChildWithState(childState);
                    if (childNode == null) {
                        double childScore = tree.computeEvaluationScore(childState);
                        childNode = new AlphaBetaTreeNode(node, childState, childScore);
                    }

                    bestValue = bestValue.min(alphaBetaV2(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
                    beta = Math.min(beta, bestValue.getEvaluationScore());
                    if (beta <= alpha) {
                        break;
                    }
                }
            }
            else {
                // Fully expanded
                for (AlphaBetaTreeNode childNode : node.getChildren()) {
                    bestValue = bestValue.min(alphaBetaV2(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
                    beta = Math.min(beta, bestValue.getEvaluationScore());
                    if (beta <= alpha) {
                        break;
                    }
                }
            }
        }
        return bestValue;
    }

    // Bare bones
    // + optimal ordering
    private AlphaBetaTreeNode alphaBetaV3(AlphaBetaTreeNode node, double alpha, double beta, int player, int depth) {
        nodesEncountered++;
        // Depth-1 winning node
        if (node.getGameState().hasWon(this) && node.getGameState().getDepth() == 1) {
            winning = node.getGameState();
        }
        // Leaf node
        if (depth == 0) {
            return node;
        }
        AlphaBetaTreeNode bestValue;
        // Maximizing
        if (player == 1) {
            bestValue = new AlphaBetaTreeNode(null, Double.NEGATIVE_INFINITY);

            List<Move> moves = node.getAllPossibleMoves();
            while (!moves.isEmpty()) {
                GameState childState = fetchNextBestChildState(moves, node.getGameState(), true);
                double childScore = tree.computeEvaluationScore(childState);
                AlphaBetaTreeNode childNode = new AlphaBetaTreeNode(node, childState, childScore);

                bestValue = bestValue.max(alphaBetaV3(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
                alpha = Math.max(alpha, bestValue.getEvaluationScore());
                if (alpha >= beta) {
                    break;
                }
            }
        }
        // Minimizing
        else {
            bestValue = new AlphaBetaTreeNode(null, Double.POSITIVE_INFINITY);

            List<Move> moves = node.getAllPossibleMoves();
            while (!moves.isEmpty()) {
                GameState childState = fetchNextBestChildState(moves, node.getGameState(), false);
                double childScore = tree.computeEvaluationScore(childState);
                AlphaBetaTreeNode childNode = new AlphaBetaTreeNode(node, childState, childScore);

                bestValue = bestValue.min(alphaBetaV3(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
                beta = Math.min(beta, bestValue.getEvaluationScore());
                if (beta <= alpha) {
                    break;
                }
            }
        }
        return bestValue;
    }

    // Bare bones
    // + duplicate child prevention
    // + optimal ordering
    private AlphaBetaTreeNode alphaBetaV4(AlphaBetaTreeNode node, double alpha, double beta, int player, int depth) {
        nodesEncountered++;
        // Depth-1 winning node
        if (node.getGameState().hasWon(this) && node.getGameState().getDepth() == 1) {
            winning = node.getGameState();
        }
        // Leaf node
        if (depth == 0) {
            return node;
        }
        AlphaBetaTreeNode bestValue;
        // Maximizing
        if (player == 1) {
            bestValue = new AlphaBetaTreeNode(null, Double.NEGATIVE_INFINITY);

            if (node.getChildren().isEmpty()) {
                // Not expanded
                List<Move> moves = node.getAllPossibleMoves();
                while (!moves.isEmpty()) {
                    GameState childState = fetchNextBestChildState(moves, node.getGameState(), true);
                    double childScore = tree.computeEvaluationScore(childState);
                    AlphaBetaTreeNode childNode = new AlphaBetaTreeNode(node, childState, childScore);

                    bestValue = bestValue.max(alphaBetaV4(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
                    alpha = Math.max(alpha, bestValue.getEvaluationScore());
                    if (alpha >= beta) {
                        break;
                    }
                }
            }
            else if (node.getChildren().size() < node.getAllPossibleMoves().size()) {
                // Partially expanded
                List<Move> moves = node.getAllPossibleMoves();
                while (!moves.isEmpty()) {
                    GameState childState = fetchNextBestChildState(moves, node.getGameState(), true);
                    AlphaBetaTreeNode childNode = node.fetchChildWithState(childState);
                    if (childNode == null) {
                        double childScore = tree.computeEvaluationScore(childState);
                        childNode = new AlphaBetaTreeNode(node, childState, childScore);
                    }

                    bestValue = bestValue.max(alphaBetaV4(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
                    alpha = Math.max(alpha, bestValue.getEvaluationScore());
                    if (alpha >= beta) {
                        break;
                    }
                }
            }
            else {
                // Fully expanded
                for (AlphaBetaTreeNode childNode : node.getChildren()) {
                    bestValue = bestValue.max(alphaBetaV4(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
                    alpha = Math.max(alpha, bestValue.getEvaluationScore());
                    if (alpha >= beta) {
                        break;
                    }
                }
            }
        }
        // Minimizing
        else {
            bestValue = new AlphaBetaTreeNode(null, Double.POSITIVE_INFINITY);

            if (node.getChildren().isEmpty()) {
                // Not expanded
                List<Move> moves = node.getAllPossibleMoves();
                while (!moves.isEmpty()) {
                    GameState childState = fetchNextBestChildState(moves, node.getGameState(), false);
                    double childScore = tree.computeEvaluationScore(childState);
                    AlphaBetaTreeNode childNode = new AlphaBetaTreeNode(node, childState, childScore);

                    bestValue = bestValue.min(alphaBetaV4(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
                    beta = Math.min(beta, bestValue.getEvaluationScore());
                    if (beta <= alpha) {
                        break;
                    }
                }
            }
            else if (node.getChildren().size() < node.getAllPossibleMoves().size()) {
                // Partially expanded
                List<Move> moves = node.getAllPossibleMoves();
                while (!moves.isEmpty()) {
                    GameState childState = fetchNextBestChildState(moves, node.getGameState(), false);
                    AlphaBetaTreeNode childNode = node.fetchChildWithState(childState);
                    if (childNode == null) {
                        double childScore = tree.computeEvaluationScore(childState);
                        childNode = new AlphaBetaTreeNode(node, childState, childScore);
                    }

                    bestValue = bestValue.min(alphaBetaV4(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
                    beta = Math.min(beta, bestValue.getEvaluationScore());
                    if (beta <= alpha) {
                        break;
                    }
                }
            }
            else {
                // Fully expanded
                for (AlphaBetaTreeNode childNode : node.getChildren()) {
                    bestValue = bestValue.min(alphaBetaV4(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
                    beta = Math.min(beta, bestValue.getEvaluationScore());
                    if (beta <= alpha) {
                        break;
                    }
                }
            }
        }
        return bestValue;
    }

    // Bare bones
    // + duplicate child prevention
    // + optimal ordering
    // + subtrees
    private AlphaBetaTreeNode alphaBetaV5(AlphaBetaTreeNode node, double alpha, double beta, int player, int depth) {
        nodesEncountered++;
        // Depth-1 winning node
        if (node.getGameState().hasWon(this) && node.getGameState().getDepth() == 1) {
            winning = node.getGameState();
        }
        // Leaf node
        if (depth == 0) {
            return node;
        }
        AlphaBetaTreeNode bestValue;
        // Maximizing
        if (player == 1) {
            bestValue = new AlphaBetaTreeNode(null, Double.NEGATIVE_INFINITY);

            if (node.getChildren().isEmpty()) {
                // Not expanded
                List<Move> moves = node.getAllPossibleMoves();
                while (!moves.isEmpty()) {
                    GameState childState = fetchNextBestChildState(moves, node.getGameState(), true);
                    double childScore = tree.computeEvaluationScore(childState);
                    AlphaBetaTreeNode childNode = new AlphaBetaTreeNode(node, childState, childScore);
                    tree.addNodeToLayer(childNode, childState.getDepth());

                    bestValue = bestValue.max(alphaBetaV5(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
                    alpha = Math.max(alpha, bestValue.getEvaluationScore());
                    if (alpha >= beta) {
                        break;
                    }
                }
            }
            else if (node.getChildren().size() < node.getAllPossibleMoves().size()) {
                // Partially expanded
                List<Move> moves = node.getAllPossibleMoves();
                while (!moves.isEmpty()) {
                    GameState childState = fetchNextBestChildState(moves, node.getGameState(), true);
                    AlphaBetaTreeNode childNode = node.fetchChildWithState(childState);
                    if (childNode == null) {
                        double childScore = tree.computeEvaluationScore(childState);
                        childNode = new AlphaBetaTreeNode(node, childState, childScore);
                        tree.addNodeToLayer(childNode, childState.getDepth());
                    }

                    bestValue = bestValue.max(alphaBetaV5(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
                    alpha = Math.max(alpha, bestValue.getEvaluationScore());
                    if (alpha >= beta) {
                        break;
                    }
                }
            }
            else {
                // Fully expanded
                for (AlphaBetaTreeNode childNode : node.getChildren()) {
                    bestValue = bestValue.max(alphaBetaV5(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
                    alpha = Math.max(alpha, bestValue.getEvaluationScore());
                    if (alpha >= beta) {
                        break;
                    }
                }
            }
        }
        // Minimizing
        else {
            bestValue = new AlphaBetaTreeNode(null, Double.POSITIVE_INFINITY);

            if (node.getChildren().isEmpty()) {
                // Not expanded
                List<Move> moves = node.getAllPossibleMoves();
                while (!moves.isEmpty()) {
                    GameState childState = fetchNextBestChildState(moves, node.getGameState(), false);
                    double childScore = tree.computeEvaluationScore(childState);
                    AlphaBetaTreeNode childNode = new AlphaBetaTreeNode(node, childState, childScore);
                    tree.addNodeToLayer(childNode, childState.getDepth());

                    bestValue = bestValue.min(alphaBetaV5(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
                    beta = Math.min(beta, bestValue.getEvaluationScore());
                    if (beta <= alpha) {
                        break;
                    }
                }
            }
            else if (node.getChildren().size() < node.getAllPossibleMoves().size()) {
                // Partially expanded
                List<Move> moves = node.getAllPossibleMoves();
                while (!moves.isEmpty()) {
                    GameState childState = fetchNextBestChildState(moves, node.getGameState(), false);
                    AlphaBetaTreeNode childNode = node.fetchChildWithState(childState);
                    if (childNode == null) {
                        double childScore = tree.computeEvaluationScore(childState);
                        childNode = new AlphaBetaTreeNode(node, childState, childScore);
                        tree.addNodeToLayer(childNode, childState.getDepth());
                    }

                    bestValue = bestValue.min(alphaBetaV5(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
                    beta = Math.min(beta, bestValue.getEvaluationScore());
                    if (beta <= alpha) {
                        break;
                    }
                }
            }
            else {
                // Fully expanded
                for (AlphaBetaTreeNode childNode : node.getChildren()) {
                    bestValue = bestValue.min(alphaBetaV5(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
                    beta = Math.min(beta, bestValue.getEvaluationScore());
                    if (beta <= alpha) {
                        break;
                    }
                }
            }
        }
        return bestValue;
    }

    // V5
    // + different strategy for optimal ordering
    private AlphaBetaTreeNode alphaBetaV6(AlphaBetaTreeNode node, double alpha, double beta, int player, int depth) {
        nodesEncountered++;
        // Depth-1 winning node
        if (node.getGameState().hasWon(this) && node.getGameState().getDepth() == 1) {
            winning = node.getGameState();
        }
        // Leaf node
        if (depth == 0) {
            return node;
        }
        AlphaBetaTreeNode bestValue;
        // Maximizing
        if (player == 1) {
            bestValue = new AlphaBetaTreeNode(null, Double.NEGATIVE_INFINITY);

            if (node.getChildren().isEmpty()) {
                // Not expanded
                List<Move> moves = node.getAllPossibleMoves();
                List<GameState> childStates = enumerateChildStates(moves, node.getGameState());
                childStates = mergesort(childStates);
                Collections.reverse(childStates);

                for (GameState childState : childStates) {
                    if (fetchDuplicate(tree.getRoot(), childState) != null) {
                        duplicatesEncountered++;
                    }

                    double childScore = tree.computeEvaluationScore(childState);
                    AlphaBetaTreeNode childNode = new AlphaBetaTreeNode(node, childState, childScore);
                    tree.addNodeToLayer(childNode, childState.getDepth());

                    bestValue = bestValue.max(alphaBetaV6(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
                    alpha = Math.max(alpha, bestValue.getEvaluationScore());
                    if (alpha >= beta) {
                        break;
                    }
                }
            }
            else if (node.getChildren().size() < node.getAllPossibleMoves().size()) {
                // Partially expanded
                List<Move> moves = node.getAllPossibleMoves();
                List<GameState> childStates = enumerateChildStates(moves, node.getGameState());
                childStates = mergesort(childStates);
                Collections.reverse(childStates);

                for (GameState childState : childStates) {
                    if (fetchDuplicate(tree.getRoot(), childState) != null) {
                        duplicatesEncountered++;
                    }

                    AlphaBetaTreeNode childNode = node.fetchChildWithState(childState);
                    if (childNode == null) {
                        double childScore = tree.computeEvaluationScore(childState);
                        childNode = new AlphaBetaTreeNode(node, childState, childScore);
                        tree.addNodeToLayer(childNode, childState.getDepth());
                    }

                    bestValue = bestValue.max(alphaBetaV6(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
                    alpha = Math.max(alpha, bestValue.getEvaluationScore());
                    if (alpha >= beta) {
                        break;
                    }
                }
            }
            else {
                // Fully expanded
                for (AlphaBetaTreeNode childNode : node.getChildren()) {
                    bestValue = bestValue.max(alphaBetaV6(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
                    alpha = Math.max(alpha, bestValue.getEvaluationScore());
                    if (alpha >= beta) {
                        break;
                    }
                }
            }
        }
        // Minimizing
        else {
            bestValue = new AlphaBetaTreeNode(null, Double.POSITIVE_INFINITY);

            if (node.getChildren().isEmpty()) {
                // Not expanded
                List<Move> moves = node.getAllPossibleMoves();
                List<GameState> childStates = enumerateChildStates(moves, node.getGameState());
                childStates = mergesort(childStates);

                for (GameState childState : childStates) {
                    if (fetchDuplicate(tree.getRoot(), childState) != null) {
                        duplicatesEncountered++;
                    }

                    double childScore = tree.computeEvaluationScore(childState);
                    AlphaBetaTreeNode childNode = new AlphaBetaTreeNode(node, childState, childScore);
                    tree.addNodeToLayer(childNode, childState.getDepth());

                    bestValue = bestValue.min(alphaBetaV6(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
                    beta = Math.min(beta, bestValue.getEvaluationScore());
                    if (beta <= alpha) {
                        break;
                    }
                }
            }
            else if (node.getChildren().size() < node.getAllPossibleMoves().size()) {
                // Partially expanded
                List<Move> moves = node.getAllPossibleMoves();
                List<GameState> childStates = enumerateChildStates(moves, node.getGameState());
                childStates = mergesort(childStates);

                for (GameState childState : childStates) {
                    if (fetchDuplicate(tree.getRoot(), childState) != null) {
                        duplicatesEncountered++;
                    }

                    AlphaBetaTreeNode childNode = node.fetchChildWithState(childState);
                    if (childNode == null) {
                        double childScore = tree.computeEvaluationScore(childState);
                        childNode = new AlphaBetaTreeNode(node, childState, childScore);
                        tree.addNodeToLayer(childNode, childState.getDepth());
                    }

                    bestValue = bestValue.min(alphaBetaV6(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
                    beta = Math.min(beta, bestValue.getEvaluationScore());
                    if (beta <= alpha) {
                        break;
                    }
                }
            }
            else {
                // Fully expanded
                for (AlphaBetaTreeNode childNode : node.getChildren()) {
                    bestValue = bestValue.min(alphaBetaV6(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
                    beta = Math.min(beta, bestValue.getEvaluationScore());
                    if (beta <= alpha) {
                        break;
                    }
                }
            }
        }
        return bestValue;
    }

    // V6
    // + duplicate state prevention
    private AlphaBetaTreeNode alphaBetaV7(AlphaBetaTreeNode node, double alpha, double beta, int player, int depth) {
        nodesEncountered++;
        // Depth-1 winning node
        if (node.getGameState().hasWon(this) && node.getGameState().getDepth() == 1) {
            winning = node.getGameState();
        }
        // Leaf node
        if (depth == 0) {
            return node;
        }
        AlphaBetaTreeNode bestValue;
        // Maximizing
        if (player == 1) {
            bestValue = new AlphaBetaTreeNode(null, Double.NEGATIVE_INFINITY);

            if (node.getChildren().isEmpty()) {
                // Not expanded
                List<Move> moves = node.getAllPossibleMoves();
                List<GameState> childStates = enumerateChildStates(moves, node.getGameState());
                childStates = mergesort(childStates);
                Collections.reverse(childStates);

                for (GameState childState : childStates) {
                    AlphaBetaTreeNode duplicateNode = fetchDuplicate(tree.getRoot(), childState);
                    AlphaBetaTreeNode childNode;
                    if (duplicateNode == null) {
                        double childScore = tree.computeEvaluationScore(childState);
                        childNode = new AlphaBetaTreeNode(node, childState, childScore);
                        tree.addNodeToLayer(childNode, childState.getDepth());

                        bestValue = bestValue.max(alphaBetaV7(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
                        alpha = Math.max(alpha, bestValue.getEvaluationScore());
                        if (alpha >= beta) {
                            break;
                        }
                    }
                    else {
                        duplicatesEncountered++;
                        if (childState.getDepth() < duplicateNode.getGameState().getDepth()) {
                            double childScore = tree.computeEvaluationScore(childState);
                            childNode = new AlphaBetaTreeNode(node, childState, childScore);
                            tree.addNodeToLayer(childNode, childState.getDepth());
                            tree.removeNode(duplicateNode);
                            alphaBetaV7(tree.getRoot(), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1, currentDepth);

//                            bestValue = bestValue.max(alphaBetaV7(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
//                            alpha = Math.max(alpha, bestValue.getEvaluationScore());
//                            if (alpha >= beta) {
//                                break;
//                            }
                        }
                    }
                }
            }
            else if (node.getChildren().size() < node.getAllPossibleMoves().size()) {
                // Partially expanded
                List<Move> moves = node.getAllPossibleMoves();
                List<GameState> childStates = enumerateChildStates(moves, node.getGameState());
                childStates = mergesort(childStates);
                Collections.reverse(childStates);

                for (GameState childState : childStates) {
                    AlphaBetaTreeNode duplicateNode = fetchDuplicate(tree.getRoot(), childState);
                    AlphaBetaTreeNode childNode;
                    if (duplicateNode == null) {
                        double childScore = tree.computeEvaluationScore(childState);
                        childNode = new AlphaBetaTreeNode(node, childState, childScore);
                        tree.addNodeToLayer(childNode, childState.getDepth());

                        bestValue = bestValue.max(alphaBetaV7(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
                        alpha = Math.max(alpha, bestValue.getEvaluationScore());
                        if (alpha >= beta) {
                            break;
                        }
                    }
                    else {
                        duplicatesEncountered++;
                        if (node.isChildOf(duplicateNode)) {
                            bestValue = bestValue.max(alphaBetaV7(duplicateNode, alpha, beta, fetchNextPlayer(player), depth - 1));
                            alpha = Math.max(alpha, bestValue.getEvaluationScore());
                            if (alpha >= beta) {
                                break;
                            }
                        }
                        else if (childState.getDepth() < duplicateNode.getGameState().getDepth()) {
                            double childScore = tree.computeEvaluationScore(childState);
                            childNode = new AlphaBetaTreeNode(node, childState, childScore);
                            tree.addNodeToLayer(childNode, childState.getDepth());
                            tree.removeNode(duplicateNode);
                            alphaBetaV7(tree.getRoot(), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1, currentDepth);

//                            bestValue = bestValue.max(alphaBetaV7(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
//                            alpha = Math.max(alpha, bestValue.getEvaluationScore());
//                            if (alpha >= beta) {
//                                break;
//                            }
                        }
                    }
                }
            }
            else {
                // Fully expanded
                for (AlphaBetaTreeNode childNode : node.getChildren()) {
                    bestValue = bestValue.max(alphaBetaV7(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
                    alpha = Math.max(alpha, bestValue.getEvaluationScore());
                    if (alpha >= beta) {
                        break;
                    }
                }
            }
        }
        // Minimizing
        else {
            bestValue = new AlphaBetaTreeNode(null, Double.POSITIVE_INFINITY);

            if (node.getChildren().isEmpty()) {
                // Not expanded
                List<Move> moves = node.getAllPossibleMoves();
                List<GameState> childStates = enumerateChildStates(moves, node.getGameState());
                childStates = mergesort(childStates);

                for (GameState childState : childStates) {
                    AlphaBetaTreeNode duplicateNode = fetchDuplicate(tree.getRoot(), childState);
                    AlphaBetaTreeNode childNode;
                    if (duplicateNode == null) {
                        double childScore = tree.computeEvaluationScore(childState);
                        childNode = new AlphaBetaTreeNode(node, childState, childScore);
                        tree.addNodeToLayer(childNode, childState.getDepth());

                        bestValue = bestValue.min(alphaBetaV7(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
                        beta = Math.min(beta, bestValue.getEvaluationScore());
                        if (beta <= alpha) {
                            break;
                        }
                    }
                    else {
                        duplicatesEncountered++;
                        if (childState.getDepth() < duplicateNode.getGameState().getDepth()) {
                            double childScore = tree.computeEvaluationScore(childState);
                            childNode = new AlphaBetaTreeNode(node, childState, childScore);
                            tree.addNodeToLayer(childNode, childState.getDepth());
                            tree.removeNode(duplicateNode);
                            alphaBetaV7(tree.getRoot(), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1, currentDepth);

//                            bestValue = bestValue.min(alphaBetaV7(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
//                            beta = Math.min(beta, bestValue.getEvaluationScore());
//                            if (beta <= alpha) {
//                                break;
//                            }
                        }
                    }
                }
            }
            else if (node.getChildren().size() < node.getAllPossibleMoves().size()) {
                // Partially expanded
                List<Move> moves = node.getAllPossibleMoves();
                List<GameState> childStates = enumerateChildStates(moves, node.getGameState());
                childStates = mergesort(childStates);

                for (GameState childState : childStates) {
                    AlphaBetaTreeNode duplicateNode = fetchDuplicate(tree.getRoot(), childState);
                    AlphaBetaTreeNode childNode;
                    if (duplicateNode == null) {
                        double childScore = tree.computeEvaluationScore(childState);
                        childNode = new AlphaBetaTreeNode(node, childState, childScore);
                        tree.addNodeToLayer(childNode, childState.getDepth());

                        bestValue = bestValue.min(alphaBetaV7(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
                        beta = Math.min(beta, bestValue.getEvaluationScore());
                        if (beta <= alpha) {
                            break;
                        }
                    }
                    else {
                        duplicatesEncountered++;
                        if (node.isChildOf(duplicateNode)) {
                            bestValue = bestValue.min(alphaBetaV7(duplicateNode, alpha, beta, fetchNextPlayer(player), depth - 1));
                            beta = Math.min(beta, bestValue.getEvaluationScore());
                            if (beta <= alpha) {
                                break;
                            }
                        }
                        else if (childState.getDepth() < duplicateNode.getGameState().getDepth()) {
                            double childScore = tree.computeEvaluationScore(childState);
                            childNode = new AlphaBetaTreeNode(node, childState, childScore);
                            tree.addNodeToLayer(childNode, childState.getDepth());
                            tree.removeNode(duplicateNode);
                            alphaBetaV7(tree.getRoot(), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1, currentDepth);

//                            bestValue = bestValue.min(alphaBetaV7(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
//                            beta = Math.min(beta, bestValue.getEvaluationScore());
//                            if (beta <= alpha) {
//                                break;
//                            }
                        }
                    }
                }
            }
            else {
                // Fully expanded
                for (AlphaBetaTreeNode childNode : node.getChildren()) {
                    bestValue = bestValue.min(alphaBetaV7(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
                    beta = Math.min(beta, bestValue.getEvaluationScore());
                    if (beta <= alpha) {
                        break;
                    }
                }
            }
        }
        return bestValue;
    }

//
//    private AlphaBetaTreeNode alphaBetaV3(AlphaBetaTreeNode node, double alpha, double beta, int player, int depth) {
//        nodesEncountered++;
//        // Depth-1 winning node
//        if (node.getGameState().hasWon(this) && node.getGameState().getDepth() == 1) {
//            winning = node.getGameState();
//        }
//        // Leaf node
//        if (depth == 0) {
//            return node;
//        }
//        AlphaBetaTreeNode bestValue;
//        // Maximizing
//        if (player == 1) {
//            bestValue = new AlphaBetaTreeNode(null, Double.NEGATIVE_INFINITY);
//
//            if (node.getChildren().isEmpty()) {
//                // Not expanded
//                List<Move> moves = node.getAllPossibleMoves();
//
//                while(!moves.isEmpty()) {
//                    GameState childState = fetchNextBestChildState(moves, node.getGameState(), true);
//                    double childScore = tree.computeEvaluationScore(childState);
//
//                    AlphaBetaTreeNode childNode = new AlphaBetaTreeNode(node, childState, childScore);
//                    tree.addNodeToLayer(childNode, childState.getDepth());
//
//                    bestValue = bestValue.max(alphaBeta(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
//                    alpha = Math.max(alpha, bestValue.getEvaluationScore());
//                    if (alpha >= beta) {
//                        break;
//                    }
//                }
//            }
//            else if (node.getChildren().size() < node.getAllPossibleMoves().size()) {
//                // Partially expanded
//                List<Move> moves = node.getAllPossibleMoves();
//
//                while(!moves.isEmpty()) {
//                    GameState childState = fetchNextBestChildState(moves, node.getGameState(), true);
//
//                    AlphaBetaTreeNode childNode = new AlphaBetaTreeNode(null, Double.NEGATIVE_INFINITY);
//                    for (AlphaBetaTreeNode child : node.getChildren()) {
//                        if (child.getGameState().contentEquals(childState)) {
//                            childNode = child;
//                            break;
//                        }
//                    }
//
//                    if (childNode.getGameState() == null) {
//                        double childScore = tree.computeEvaluationScore(childState);
//                        childNode = new AlphaBetaTreeNode(node, childState, childScore);
//                        tree.addNodeToLayer(childNode, childState.getDepth());
//                    }
//
//                    bestValue = bestValue.max(alphaBeta(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
//                    alpha = Math.max(alpha, bestValue.getEvaluationScore());
//                    if (alpha >= beta) {
//                        break;
//                    }
//                }
//            }
//            else {
//                // Fully expanded
//                for (AlphaBetaTreeNode childNode : node.getChildren()) {
//                    bestValue = bestValue.max(alphaBeta(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
//                    alpha = Math.max(alpha, bestValue.getEvaluationScore());
//                    if (alpha >= beta) {
//                        break;
//                    }
//                }
//            }
//        }
//        // Minimizing
//        else {
//            bestValue = new AlphaBetaTreeNode(null, Double.POSITIVE_INFINITY);
//
//            if (node.getChildren().isEmpty()) {
//                // Not expanded
//                List<Move> moves = node.getAllPossibleMoves();
//
//                while(!moves.isEmpty()) {
//                    GameState childState = fetchNextBestChildState(moves, node.getGameState(), false);
//                    double childScore = tree.computeEvaluationScore(childState);
//
//                    AlphaBetaTreeNode childNode = new AlphaBetaTreeNode(node, childState, childScore);
//                    tree.addNodeToLayer(childNode, childState.getDepth());
//
//                    bestValue = bestValue.min(alphaBeta(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
//                    beta = Math.min(beta, bestValue.getEvaluationScore());
//                    if (beta <= alpha) {
//                        break;
//                    }
//                }
//            }
//            else if (node.getChildren().size() < node.getAllPossibleMoves().size()) {
//                // Partially expanded
//                List<Move> moves = node.getAllPossibleMoves();
//
//                while(!moves.isEmpty()) {
//                    GameState childState = fetchNextBestChildState(moves, node.getGameState(), false);
//
//                    AlphaBetaTreeNode childNode = new AlphaBetaTreeNode(null, Double.NEGATIVE_INFINITY);
//                    for (AlphaBetaTreeNode child : node.getChildren()) {
//                        if (child.getGameState().contentEquals(childState)) {
//                            childNode = child;
//                            break;
//                        }
//                    }
//
//                    if (childNode.getGameState() == null) {
//                        double childScore = tree.computeEvaluationScore(childState);
//                        childNode = new AlphaBetaTreeNode(node, childState, childScore);
//                        tree.addNodeToLayer(childNode, childState.getDepth());
//                    }
//
//                    bestValue = bestValue.min(alphaBeta(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
//                    beta = Math.min(beta, bestValue.getEvaluationScore());
//                    if (beta <= alpha) {
//                        break;
//                    }
//                }
//            }
//            else {
//                // Fully expanded
//                for (AlphaBetaTreeNode childNode : node.getChildren()) {
//                    bestValue = bestValue.min(alphaBeta(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
//                    beta = Math.min(beta, bestValue.getEvaluationScore());
//                    if (beta <= alpha) {
//                        break;
//                    }
//                }
//            }
//        }
//        return bestValue;
//    }
//
//    private AlphaBetaTreeNode alphaBetaV4(AlphaBetaTreeNode node, double alpha, double beta, int player, int depth) {
//        nodesEncountered++;
//        // Depth-1 winning node
//        if (node.getGameState().hasWon(this) && node.getGameState().getDepth() == 1) {
//            winning = node.getGameState();
//        }
//        // Leaf node
//        if (depth == 0) {
//            return node;
//        }
//        AlphaBetaTreeNode bestValue;
//        // Maximizing
//        if (player == 1) {
//            bestValue = new AlphaBetaTreeNode(null, Double.NEGATIVE_INFINITY);
//
//            if (node.getChildren().isEmpty()) {
//                // Not expanded
//                List<Move> moves = node.getAllPossibleMoves();
//
//                while(!moves.isEmpty()) {
//                    GameState childState = fetchNextBestChildState(moves, node.getGameState(), true);
//                    double childScore = tree.computeEvaluationScore(childState);
//
//                    AlphaBetaTreeNode childNode = new AlphaBetaTreeNode(node, childState, childScore);
//                    tree.addNodeToLayer(childNode, childState.getDepth());
//
//                    bestValue = bestValue.max(alphaBeta(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
//                    alpha = Math.max(alpha, bestValue.getEvaluationScore());
//                    if (alpha >= beta) {
//                        break;
//                    }
//                }
//            }
//            else if (node.getChildren().size() < node.getAllPossibleMoves().size()) {
//                // Partially expanded
//                List<Move> moves = node.getAllPossibleMoves();
//
//                while(!moves.isEmpty()) {
//                    GameState childState = fetchNextBestChildState(moves, node.getGameState(), true);
//
//                    AlphaBetaTreeNode childNode = new AlphaBetaTreeNode(null, Double.NEGATIVE_INFINITY);
//                    for (AlphaBetaTreeNode child : node.getChildren()) {
//                        if (child.getGameState().contentEquals(childState)) {
//                            childNode = child;
//                            break;
//                        }
//                    }
//
//                    if (childNode.getGameState() == null) {
//                        double childScore = tree.computeEvaluationScore(childState);
//                        childNode = new AlphaBetaTreeNode(node, childState, childScore);
//                        tree.addNodeToLayer(childNode, childState.getDepth());
//                    }
//
//                    bestValue = bestValue.max(alphaBeta(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
//                    alpha = Math.max(alpha, bestValue.getEvaluationScore());
//                    if (alpha >= beta) {
//                        break;
//                    }
//                }
//            }
//            else {
//                // Fully expanded
//                for (AlphaBetaTreeNode childNode : node.getChildren()) {
//                    bestValue = bestValue.max(alphaBeta(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
//                    alpha = Math.max(alpha, bestValue.getEvaluationScore());
//                    if (alpha >= beta) {
//                        break;
//                    }
//                }
//            }
//        }
//        // Minimizing
//        else {
//            bestValue = new AlphaBetaTreeNode(null, Double.POSITIVE_INFINITY);
//
//            if (node.getChildren().isEmpty()) {
//                // Not expanded
//                List<Move> moves = node.getAllPossibleMoves();
//
//                while(!moves.isEmpty()) {
//                    GameState childState = fetchNextBestChildState(moves, node.getGameState(), false);
//                    double childScore = tree.computeEvaluationScore(childState);
//
//                    AlphaBetaTreeNode childNode = new AlphaBetaTreeNode(node, childState, childScore);
//                    tree.addNodeToLayer(childNode, childState.getDepth());
//
//                    bestValue = bestValue.min(alphaBeta(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
//                    beta = Math.min(beta, bestValue.getEvaluationScore());
//                    if (beta <= alpha) {
//                        break;
//                    }
//                }
//            }
//            else if (node.getChildren().size() < node.getAllPossibleMoves().size()) {
//                // Partially expanded
//                List<Move> moves = node.getAllPossibleMoves();
//
//                while(!moves.isEmpty()) {
//                    GameState childState = fetchNextBestChildState(moves, node.getGameState(), false);
//
//                    AlphaBetaTreeNode childNode = new AlphaBetaTreeNode(null, Double.NEGATIVE_INFINITY);
//                    for (AlphaBetaTreeNode child : node.getChildren()) {
//                        if (child.getGameState().contentEquals(childState)) {
//                            childNode = child;
//                            break;
//                        }
//                    }
//
//                    if (childNode.getGameState() == null) {
//                        double childScore = tree.computeEvaluationScore(childState);
//                        childNode = new AlphaBetaTreeNode(node, childState, childScore);
//                        tree.addNodeToLayer(childNode, childState.getDepth());
//                    }
//
//                    bestValue = bestValue.min(alphaBeta(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
//                    beta = Math.min(beta, bestValue.getEvaluationScore());
//                    if (beta <= alpha) {
//                        break;
//                    }
//                }
//            }
//            else {
//                // Fully expanded
//                for (AlphaBetaTreeNode childNode : node.getChildren()) {
//                    bestValue = bestValue.min(alphaBeta(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
//                    beta = Math.min(beta, bestValue.getEvaluationScore());
//                    if (beta <= alpha) {
//                        break;
//                    }
//                }
//            }
//        }
//        return bestValue;
//    }

//    private AlphaBetaTreeNode alphaBeta(AlphaBetaTreeNode node, double alpha, double beta, int player, int depth) {
//        nodesEncountered++;
//        // Depth-1 winning node
//        if (node.getGameState().hasWon(this) && node.getGameState().getDepth() == 1) {
//            winning = node.getGameState();
//        }
//        // Leaf node
//        if (depth == 0) {
//            return node;
//        }
//        AlphaBetaTreeNode bestValue;
//        // Maximizing
//        if (player == 1) {
//            bestValue = new AlphaBetaTreeNode(null, Double.NEGATIVE_INFINITY);
//
//            if (node.getChildren().isEmpty()) {
//                // Not expanded
//                List<Move> moves = node.getAllPossibleMoves();
//
//                while(!moves.isEmpty()) {
//                    GameState childState = fetchNextBestChildState(moves, node.getGameState(), true);
//
//                    long starTime = System.currentTimeMillis();
//                    AlphaBetaTreeNode duplicateNode = fetchDuplicate(tree.getRoot(), childState);
//                    long endTime = System.currentTimeMillis();
//                    timeDuplicateChecking += (endTime - starTime);
//
//                    if (duplicateNode != null) {
//                        // Duplicate
//                        GameState duplicateState = duplicateNode.getGameState();
//                        if (childState.getDepth() < duplicateState.getDepth()) {
//                            // Shorter path
//                            tree.removeNode(duplicateNode);
//
//                            double childScore = tree.computeEvaluationScore(childState);
//
//                            AlphaBetaTreeNode childNode = new AlphaBetaTreeNode(node, childState, childScore);
//                            tree.addNodeToLayer(childNode, childState.getDepth());
//
//                            alphaBeta(tree.getRoot(), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1, i);
//
//                            bestValue = bestValue.max(alphaBeta(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
//                            alpha = Math.max(alpha, bestValue.getEvaluationScore());
//                            if (alpha >= beta) {
//                                break;
//                            }
//                        }
//                    }
//                    else {
//                        // No duplicate
//                        double childScore = tree.computeEvaluationScore(childState);
//
//                        AlphaBetaTreeNode childNode = new AlphaBetaTreeNode(node, childState, childScore);
//                        tree.addNodeToLayer(childNode, childState.getDepth());
//
//                        bestValue = bestValue.max(alphaBeta(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
//                        alpha = Math.max(alpha, bestValue.getEvaluationScore());
//                        if (alpha >= beta) {
//                            break;
//                        }
//                    }
//                }
//            }
//            else if (node.getChildren().size() < node.getAllPossibleMoves().size()) {
//                // Partially expanded
//                List<Move> moves = node.getAllPossibleMoves();
//
//                while(!moves.isEmpty()) {
//                    GameState childState = fetchNextBestChildState(moves, node.getGameState(), true);
//
//                    long starTime = System.currentTimeMillis();
//                    AlphaBetaTreeNode duplicateNode = fetchDuplicate(tree.getRoot(), childState);
//                    long endTime = System.currentTimeMillis();
//                    timeDuplicateChecking += (endTime - starTime);
//
//                    if (duplicateNode != null) {
//                        // Duplicate
//                        if (node.isChildOf(duplicateNode)) {
//                            bestValue = bestValue.max(alphaBeta(duplicateNode, alpha, beta, fetchNextPlayer(player), depth - 1));
//                            alpha = Math.max(alpha, bestValue.getEvaluationScore());
//                            if (alpha >= beta) {
//                                break;
//                            }
//                        }
//                        else {
//                            GameState duplicateState = duplicateNode.getGameState();
//                            if (childState.getDepth() < duplicateState.getDepth()) {
//                                // Shorter path
//                                tree.removeNode(duplicateNode);
//
//                                double childScore = tree.computeEvaluationScore(childState);
//
//                                AlphaBetaTreeNode childNode = new AlphaBetaTreeNode(node, childState, childScore);
//                                tree.addNodeToLayer(childNode, childState.getDepth());
//
//                                alphaBeta(tree.getRoot(), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1, i);
//
//                                bestValue = bestValue.max(alphaBeta(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
//                                alpha = Math.max(alpha, bestValue.getEvaluationScore());
//                                if (alpha >= beta) {
//                                    break;
//                                }
//                            }
//                        }
//                    }
//                    else {
//                        // No duplicate
//                        double childScore = tree.computeEvaluationScore(childState);
//
//                        AlphaBetaTreeNode childNode = new AlphaBetaTreeNode(node, childState, childScore);
//                        tree.addNodeToLayer(childNode, childState.getDepth());
//
//                        bestValue = bestValue.max(alphaBeta(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
//                        alpha = Math.max(alpha, bestValue.getEvaluationScore());
//                        if (alpha >= beta) {
//                            break;
//                        }
//                    }
//                }
//            }
//            else {
//                // Fully expanded
//                for (AlphaBetaTreeNode childNode : node.getChildren()) {
//                    bestValue = bestValue.max(alphaBeta(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
//                    alpha = Math.max(alpha, bestValue.getEvaluationScore());
//                    if (alpha >= beta) {
//                        break;
//                    }
//                }
//            }
//        }
//        // Minimizing
//        else {
//            bestValue = new AlphaBetaTreeNode(null, Double.POSITIVE_INFINITY);
//
//            if (node.getChildren().isEmpty()) {
//                // Not expanded
//                List<Move> moves = node.getAllPossibleMoves();
//
//                while(!moves.isEmpty()) {
//                    GameState childState = fetchNextBestChildState(moves, node.getGameState(), false);
//
//                    long starTime = System.currentTimeMillis();
//                    AlphaBetaTreeNode duplicateNode = fetchDuplicate(tree.getRoot(), childState);
//                    long endTime = System.currentTimeMillis();
//                    timeDuplicateChecking += (endTime - starTime);
//
//                    if (duplicateNode != null) {
//                        // Duplicate
//                        GameState duplicateState = duplicateNode.getGameState();
//                        if (childState.getDepth() < duplicateState.getDepth()) {
//                            // Shorter path
//                            tree.removeNode(duplicateNode);
//
//                            double childScore = tree.computeEvaluationScore(childState);
//
//                            AlphaBetaTreeNode childNode = new AlphaBetaTreeNode(node, childState, childScore);
//                            tree.addNodeToLayer(childNode, childState.getDepth());
//
//                            alphaBeta(tree.getRoot(), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1, i);
//
//                            bestValue = bestValue.min(alphaBeta(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
//                            beta = Math.min(beta, bestValue.getEvaluationScore());
//                            if (beta <= alpha) {
//                                break;
//                            }
//                        }
//                    }
//                    else {
//                        // No duplicate
//                        double childScore = tree.computeEvaluationScore(childState);
//
//                        AlphaBetaTreeNode childNode = new AlphaBetaTreeNode(node, childState, childScore);
//                        tree.addNodeToLayer(childNode, childState.getDepth());
//
//                        bestValue = bestValue.min(alphaBeta(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
//                        beta = Math.min(beta, bestValue.getEvaluationScore());
//                        if (beta <= alpha) {
//                            break;
//                        }
//                    }
//                }
//            }
//            else if (node.getChildren().size() < node.getAllPossibleMoves().size()) {
//                // Partially expanded
//                List<Move> moves = node.getAllPossibleMoves();
//
//                while(!moves.isEmpty()) {
//                    GameState childState = fetchNextBestChildState(moves, node.getGameState(), false);
//
//                    long starTime = System.currentTimeMillis();
//                    AlphaBetaTreeNode duplicateNode = fetchDuplicate(tree.getRoot(), childState);
//                    long endTime = System.currentTimeMillis();
//                    timeDuplicateChecking += (endTime - starTime);
//
//                    if (duplicateNode != null) {
//                        // Duplicate
//                        if (node.isChildOf(duplicateNode)) {
//                            bestValue = bestValue.min(alphaBeta(duplicateNode, alpha, beta, fetchNextPlayer(player), depth - 1));
//                            beta = Math.min(beta, bestValue.getEvaluationScore());
//                            if (beta <= alpha) {
//                                break;
//                            }
//                        }
//                        else {
//                            GameState duplicateState = duplicateNode.getGameState();
//                            if (childState.getDepth() < duplicateState.getDepth()) {
//                                // Shorter path
//                                tree.removeNode(duplicateNode);
//
//                                double childScore = tree.computeEvaluationScore(childState);
//
//                                AlphaBetaTreeNode childNode = new AlphaBetaTreeNode(node, childState, childScore);
//                                tree.addNodeToLayer(childNode, childState.getDepth());
//
//                                alphaBeta(tree.getRoot(), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1, i);
//
//                                bestValue = bestValue.min(alphaBeta(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
//                                beta = Math.min(beta, bestValue.getEvaluationScore());
//                                if (beta <= alpha) {
//                                    break;
//                                }
//                            }
//                        }
//                    }
//                    else {
//                        // No duplicate
//                        double childScore = tree.computeEvaluationScore(childState);
//
//                        AlphaBetaTreeNode childNode = new AlphaBetaTreeNode(node, childState, childScore);
//                        tree.addNodeToLayer(childNode, childState.getDepth());
//
//                        bestValue = bestValue.min(alphaBeta(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
//                        beta = Math.min(beta, bestValue.getEvaluationScore());
//                        if (beta <= alpha) {
//                            break;
//                        }
//                    }
//                }
//            }
//            else {
//                // Fully expanded
//                for (AlphaBetaTreeNode childNode : node.getChildren()) {
//                    bestValue = bestValue.min(alphaBeta(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
//                    beta = Math.min(beta, bestValue.getEvaluationScore());
//                    if (beta <= alpha) {
//                        break;
//                    }
//                }
//            }
//        }
//        return bestValue;
//    }

    private int fetchNextPlayer(int currentPlayer) {
        if (currentPlayer < numberOfPlayers) {
            return currentPlayer + 1;
        }
        else {
            return 1;
        }
    }

    private AlphaBetaTreeNode findNewRoot(GameState newState) {
        AlphaBetaTreeNode newRoot = new AlphaBetaTreeNode(null, Double.NEGATIVE_INFINITY);
        List<AlphaBetaTreeNode> layer = tree.getLayers().get(numberOfPlayers);
        for (AlphaBetaTreeNode node : layer) {
            if (node.getGameState().contentEquals(newState)) {
                newRoot = node;
                break;
            }
        }
        return newRoot;
    }

    private GameState fetchNextBestChildState(List<Move> moves, GameState currentState, boolean maximizing) {
        long startTime = System.currentTimeMillis();

        Move bestMove = null;
        GameState bestState = null;
        double bestScore;
        if (maximizing) {
            bestScore = Double.NEGATIVE_INFINITY;
        }
        else {
            bestScore = Double.POSITIVE_INFINITY;
        }

        for (Move move : moves) {
            GameState childState = currentState.getStateAfterMove(move);
            double childScore = tree.computeEvaluationScore(childState);
            if (maximizing) {
                if (childScore > bestScore) {
                    bestMove = move;
                    bestState = childState;
                    bestScore = childScore;
                }
            }
            else {
                if (childScore < bestScore) {
                    bestMove = move;
                    bestState = childState;
                    bestScore = childScore;
                }
            }
        }

        moves.remove(bestMove);

        long endTime = System.currentTimeMillis();
        timeOptimalOrder += (endTime - startTime);

        return bestState;
    }

    private List<GameState> enumerateChildStates(List<Move> moves, GameState currentState) {
        List<GameState> childStates = new ArrayList<>();
        for (Move move : moves) {
            GameState childState = currentState.getStateAfterMove(move);
            childStates.add(childState);
        }
        return childStates;
    }

    private void bubbleSort(List<GameState> childStates) {
        long startTime = System.currentTimeMillis();

        int n = childStates.size();
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (tree.computeEvaluationScore(childStates.get(j)) > tree.computeEvaluationScore(childStates.get(j + 1))) {
                    // swap arr[j+1] and arr[j]
                    GameState temp = childStates.get(j);
                    childStates.set(j, childStates.get(j + 1));
                    childStates.set(j + 1, temp);
                }
            }
        }

        long endTime = System.currentTimeMillis();
        timeOptimalOrder += (endTime - startTime);
    }

    private List<GameState> mergesort(List<GameState> list) {
        long startT = System.currentTimeMillis();
        if(list.size() > 1) {
            List<GameState>[] lists = partition(list);
            List<GameState> list1 = lists[0];
            List<GameState> list2 = lists[1];

            list1 = mergesort(list1);
            list2 = mergesort(list2);

            return merge(list1, list2);
        }
        else {
            return list;
        }
    }

    private List[] partition(List<GameState> list) {
        List[] lists = new ArrayList[2];
        List<GameState> list1 = new ArrayList<>();
        List<GameState> list2 = new ArrayList<>();

        int middle = list.size() / 2;
        for(int i = 0; i < list.size(); i++) {
            if(i < middle) {
                list1.add(list.get(i));
            }
            else {
                list2.add(list.get(i));
            }
        }

        lists[0] = list1;
        lists[1] = list2;

        return lists;
    }

    private List<GameState> merge(List<GameState> list1, List<GameState> list2) {
        List<GameState> list = new ArrayList<>();
        int index1 = 0;
        int index2 = 0;

        while(index1 < list1.size() && index2 < list2.size()) {
            if(tree.computeEvaluationScore(list1.get(index1)) <= tree.computeEvaluationScore(list2.get(index2))) {
                list.add(list1.get(index1));
                index1++;
            }
            else {
                list.add(list2.get(index2));
                index2++;
            }
        }

        while(index1 < list1.size()) {
            list.add(list1.get(index1));
            index1++;
        }

        while(index2 < list2.size()) {
            list.add(list2.get(index2));
            index2++;
        }

        return list;
    }

    private AlphaBetaTreeNode fetchDuplicate(AlphaBetaTreeNode node, GameState stateToCheck) {
        if (node.getGameState().contentEquals(stateToCheck)) {
            // Duplicate
            return node;
        }
        else {
            // No duplicate
            for (AlphaBetaTreeNode childNode : node.getChildren()) {
                return fetchDuplicate(childNode, stateToCheck);
            }
        }
        return null;
    }

    @Override
    public Move returnMove(Board gameBoard) {
        // Initialize
        if (initialCall) {
            initialCall = false;
            this.numberOfPlayers = gameBoard.getPlayerCount();
            AlphaBetaTreeNode rootNode = new AlphaBetaTreeNode(new GameState(gameBoard), Double.NEGATIVE_INFINITY);
            this.tree = new AlphaBetaTree(rootNode, 4, this);
            this.tree.evaluationFunction = evaluationFunction;
        }
        else {
            AlphaBetaTreeNode rootNode = findNewRoot(new GameState(gameBoard));
            tree = tree.createSubtree(rootNode, new GameState(gameBoard));
        }

        // Iterative Deepening AlphaBeta-pruning
        boolean debug = true;
        if (debug) {
            nodesEncountered = 0;
            duplicatesEncountered = 0;
            timeOptimalOrder = 0;
            timeDuplicateChecking = 0;
        }

        long timeAlphaBeta;
        AlphaBetaTreeNode bestValue = new AlphaBetaTreeNode(null, Double.NEGATIVE_INFINITY);
        long startTime = System.currentTimeMillis();
        for (currentDepth = 1; currentDepth <= tree.getMaxDepth(); currentDepth++) {
//            AlphaBetaTreeNode rootNode = new AlphaBetaTreeNode(tree.getRoot().getGameState(), Double.NEGATIVE_INFINITY);
//            tree = new AlphaBetaTree(rootNode, tree.getMaxDepth(), this);
            AlphaBetaTreeNode bestNode = alphaBetaV7(tree.getRoot(), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1, currentDepth);
            bestValue = bestValue.max(bestNode);
        }
        long endTime = System.currentTimeMillis();
//        long startTime = System.currentTimeMillis();
//        AlphaBetaTreeNode bestValue = alphaBetaV6(tree.getRoot(), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1, tree.getMaxDepth());
//        long endTime = System.currentTimeMillis();
        timeAlphaBeta = (endTime - startTime);

        if (debug) {
//            System.out.println("Encountered " + nodesEncountered + " nodes");
//            System.out.println("Encountered " + duplicatesEncountered + " duplicates");
//            System.out.println("AlphaBeta took " + timeAlphaBeta + " milliseconds");
//            System.out.println("OptimalOrder took " + timeOptimalOrder + " milliseconds");
//            System.out.println("DuplicateChecking took " + timeDuplicateChecking + " milliseconds");
        }

        // Extract move
        Move move;
        if (winning != null) {
            move = winning.getMoveSequence().get(0);
        }
        else {
            move = bestValue.getGameState().getMoveSequence().get(0);
        }

        return move;
    }

    @Override
    public String getTypeName() {
        return "Alpha-beta ["+evaluationFunction+"]";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public Player getNewInstance() {
        return new AlphaBeta(evaluationFunction);
    }

}