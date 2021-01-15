package players.bots;

import gamerules.*;
import players.Player;
import players.bots.utils.AlphaBetaTree;
import players.bots.utils.AlphaBetaTreeNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AlphaBeta extends Player {

    private GameState winning;
    private int numberOfPlayers;
    private AlphaBetaTree tree;

    // Bare bones
    // + duplicate child prevention
    // + optimal ordering (mergesort)
    // + subtrees
    private AlphaBetaTreeNode alphaBeta(AlphaBetaTreeNode node, double alpha, double beta, int player, int depth) {
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
                List<GameState> childStates = enumerateChildStates(moves, node.getGameState());
                childStates = mergesort(childStates);
                Collections.reverse(childStates);

                for (GameState childState : childStates) {
                    AlphaBetaTreeNode childNode = node.fetchChildWithState(childState);
                    if (childNode == null) {
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
                List<GameState> childStates = enumerateChildStates(moves, node.getGameState());
                childStates = mergesort(childStates);

                for (GameState childState : childStates) {
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
                List<GameState> childStates = enumerateChildStates(moves, node.getGameState());
                childStates = mergesort(childStates);

                for (GameState childState : childStates) {
                    AlphaBetaTreeNode childNode = node.fetchChildWithState(childState);
                    if (childNode == null) {
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

    private int fetchNextPlayer(int currentPlayer) {
        if (currentPlayer < numberOfPlayers) {
            return currentPlayer + 1;
        }
        else {
            return 1;
        }
    }

    private AlphaBetaTreeNode findNewRoot(GameState newState) {
        AlphaBetaTreeNode newRoot = new AlphaBetaTreeNode(newState, Double.NEGATIVE_INFINITY);
        if (tree.getMaxDepth() > numberOfPlayers) {
            List<AlphaBetaTreeNode> layer = tree.getLayers().get(numberOfPlayers);
            for (AlphaBetaTreeNode node : layer) {
                if (node.getGameState().contentEquals(newState)) {
                    newRoot = node;
                    break;
                }
            }
        }
        return newRoot;
    }

    private List<GameState> enumerateChildStates(List<Move> moves, GameState currentState) {
        List<GameState> childStates = new ArrayList<>();
        for (Move move : moves) {
            GameState childState = currentState.getStateAfterMove(move);
            childStates.add(childState);
        }
        return childStates;
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

    @Override
    public Move returnMove(Board gameBoard) {
        // Initialize
        if (tree == null) {
            this.numberOfPlayers = gameBoard.getPlayerCount();
            AlphaBetaTreeNode rootNode = new AlphaBetaTreeNode(new GameState(gameBoard), Double.NEGATIVE_INFINITY);
            this.tree = new AlphaBetaTree(rootNode, 4, this);
        }
        else {
            AlphaBetaTreeNode rootNode = findNewRoot(new GameState(gameBoard));
            tree = tree.createSubtree(rootNode, new GameState(gameBoard));
        }

        // Iterative Deepening AlphaBeta-pruning
        AlphaBetaTreeNode bestValue = new AlphaBetaTreeNode(null, Double.NEGATIVE_INFINITY);
        for (int depth = 1; depth <= tree.getMaxDepth(); depth++) {
            AlphaBetaTreeNode bestNode = alphaBeta(tree.getRoot(), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1, depth);
            bestValue = bestValue.max(bestNode);
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