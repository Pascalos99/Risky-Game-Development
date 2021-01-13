package players.bots;

import gamerules.*;
import gamerules.evaluation_functions.PaperEval;
import gamerules.evaluation_functions.SimpleGoalDistance;
import players.Player;
import players.bots.utils.AlphaBetaTree;
import players.bots.utils.AlphaBetaTreeNode;

import java.util.List;

public class AlphaBeta extends Player {

    private boolean initialCall = true;
    private GameState winning;
    private int numberOfPlayers;
    private AlphaBetaTree tree;
    private final boolean debug = false;
    private int nodes;
    private long timeAlphaBeta;
    private long timeOptimalOrder;
    private long timeDuplicateChecking;
    private int i;

    private AlphaBetaTreeNode alphaBeta(AlphaBetaTreeNode node, double alpha, double beta, int player, int depth) {
        nodes++;
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

                while(!moves.isEmpty()) {
                    GameState childState = fetchNextBestChildState(moves, node.getGameState(), true);
                    double childScore = tree.computeEvaluationScore(childState);

                    AlphaBetaTreeNode childNode = new AlphaBetaTreeNode(node, childState, childScore);
                    tree.addNodeToLayer(childNode, childState.getDepth());

                    bestValue = bestValue.max(alphaBeta(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
                    alpha = Math.max(alpha, bestValue.getEvaluationScore());
                    if (alpha >= beta) {
                        break;
                    }
                }
            }
            else if (node.getChildren().size() < node.getAllPossibleMoves().size()) {
                // Partially expanded
                List<Move> moves = node.getAllPossibleMoves();

                while(!moves.isEmpty()) {
                    GameState childState = fetchNextBestChildState(moves, node.getGameState(), true);

                    AlphaBetaTreeNode childNode = new AlphaBetaTreeNode(null, Double.NEGATIVE_INFINITY);
                    for (AlphaBetaTreeNode child : node.getChildren()) {
                        if (child.getGameState().contentEquals(childState)) {
                            childNode = child;
                            break;
                        }
                    }

                    if (childNode.getGameState() == null) {
                        double childScore = tree.computeEvaluationScore(childState);
                        childNode = new AlphaBetaTreeNode(node, childState, childScore);
                        tree.addNodeToLayer(childNode, childState.getDepth());
                    }

                    bestValue = bestValue.max(alphaBeta(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
                    alpha = Math.max(alpha, bestValue.getEvaluationScore());
                    if (alpha >= beta) {
                        break;
                    }
                }
            }
            else {
                // Fully expanded
                for (AlphaBetaTreeNode childNode : node.getChildren()) {
                    bestValue = bestValue.max(alphaBeta(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
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

                while(!moves.isEmpty()) {
                    GameState childState = fetchNextBestChildState(moves, node.getGameState(), false);
                    double childScore = tree.computeEvaluationScore(childState);

                    AlphaBetaTreeNode childNode = new AlphaBetaTreeNode(node, childState, childScore);
                    tree.addNodeToLayer(childNode, childState.getDepth());

                    bestValue = bestValue.min(alphaBeta(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
                    beta = Math.min(beta, bestValue.getEvaluationScore());
                    if (beta <= alpha) {
                        break;
                    }
                }
            }
            else if (node.getChildren().size() < node.getAllPossibleMoves().size()) {
                // Partially expanded
                List<Move> moves = node.getAllPossibleMoves();

                while(!moves.isEmpty()) {
                    GameState childState = fetchNextBestChildState(moves, node.getGameState(), false);

                    AlphaBetaTreeNode childNode = new AlphaBetaTreeNode(null, Double.NEGATIVE_INFINITY);
                    for (AlphaBetaTreeNode child : node.getChildren()) {
                        if (child.getGameState().contentEquals(childState)) {
                            childNode = child;
                            break;
                        }
                    }

                    if (childNode.getGameState() == null) {
                        double childScore = tree.computeEvaluationScore(childState);
                        childNode = new AlphaBetaTreeNode(node, childState, childScore);
                        tree.addNodeToLayer(childNode, childState.getDepth());
                    }

                    bestValue = bestValue.min(alphaBeta(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
                    beta = Math.min(beta, bestValue.getEvaluationScore());
                    if (beta <= alpha) {
                        break;
                    }
                }
            }
            else {
                // Fully expanded
                for (AlphaBetaTreeNode childNode : node.getChildren()) {
                    bestValue = bestValue.min(alphaBeta(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
                    beta = Math.min(beta, bestValue.getEvaluationScore());
                    if (beta <= alpha) {
                        break;
                    }
                }
            }
        }
        return bestValue;
    }

//    private AlphaBetaTreeNode alphaBeta(AlphaBetaTreeNode node, double alpha, double beta, int player, int depth) {
//        nodes++;
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

//        for (int i = 0; i < moves.size(); i++) {
//            int randomIndex = (int) (Math.random() * moves.size());
//            Move move = moves.get(randomIndex);
//            GameState childState = currentState.getStateAfterMove(move);
//            double childScore = tree.computeEvaluationScore(childState);
//            if (maximizing) {
//                if (childScore > bestScore) {
//                    bestMove = move;
//                    bestState = childState;
//                    bestScore = childScore;
//                }
//            }
//            else {
//                if (childScore < bestScore) {
//                    bestMove = move;
//                    bestState = childState;
//                    bestScore = childScore;
//                }
//            }
//        }

        moves.remove(bestMove);

        long endTime = System.currentTimeMillis();
        timeOptimalOrder += (endTime - startTime);

        return bestState;
    }

    private AlphaBetaTreeNode fetchDuplicate(AlphaBetaTreeNode node, GameState stateToCheck) {
        GameState state = node.getGameState();
        if (state.contentEquals(stateToCheck)) {
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
            this.tree = new AlphaBetaTree(rootNode, 1, 13, 4, this);
        }
        else {
            AlphaBetaTreeNode rootNode = findNewRoot(new GameState(gameBoard));
            tree = tree.createSubtree(rootNode, new GameState(gameBoard));
        }

        // Iterative Deepening AlphaBeta-pruning
        if (debug) {
            nodes = 0;
            timeAlphaBeta = 0;
            timeOptimalOrder = 0;
            timeDuplicateChecking = 0;
        }

        AlphaBetaTreeNode bestValue = new AlphaBetaTreeNode(null, Double.NEGATIVE_INFINITY);
        long startTime = System.currentTimeMillis();
        for (i = 1; i <= tree.getMaxDepth(); i++) {
            AlphaBetaTreeNode bestNode = alphaBeta(tree.getRoot(), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1, i);
            bestValue = bestValue.max(bestNode);
        }
        long endTime = System.currentTimeMillis();
        timeAlphaBeta = (endTime - startTime);

        if (debug) {
            System.out.println("Encountered " + nodes + " nodes");
            System.out.println("AlphaBeta took " + timeAlphaBeta + " milliseconds");
            System.out.println("OptimalOrder took " + timeOptimalOrder + " milliseconds");
            System.out.println("DuplicateChecking took " + timeDuplicateChecking + " milliseconds");
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