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

    private GameState winning;
    private int numberOfPlayers;
    private AlphaBetaTree tree;
    private final EvaluationFunction evaluationFunction;

    public AlphaBeta() {
        // If no evaluation function is specified, default to SimpleGoalDistance
        this(new SimpleGoalDistance());
    }

    public AlphaBeta(EvaluationFunction evaluationFunction) {
    	this.evaluationFunction = evaluationFunction;
    }
    
    // Bare bones
    // + duplicate child prevention
    // + subtrees
    // + optimal ordering (mergesort)
    private AlphaBetaTreeNode alphaBeta(AlphaBetaTreeNode node, double alpha, double beta, int player, int depth) {
        // Depth-1 winning node
        if (node.getGameState().hasWon(this) && node.getGameState().getDepth() == 1) {
            winning = node.getGameState();
        }

        // Leaf node
        if (depth == 0) {
            return node;
        }

        // Initialize bestValue
        AlphaBetaTreeNode bestValue;
        if (player == 1) {
            bestValue = new AlphaBetaTreeNode(null, Double.NEGATIVE_INFINITY);
        }
        else {
            bestValue = new AlphaBetaTreeNode(null, Double.POSITIVE_INFINITY);
        }

        // Determine state of the current node
        if (node.getChildren().isEmpty()) {
            // Not expanded
            List<GameState> childStates = fetchChildStatesInOptimalOrder(node, player);

            for (GameState childState : childStates) {
                AlphaBetaTreeNode childNode = tree.createAndAddNode(node, childState);

                if (player == 1) {
                    bestValue = bestValue.max(alphaBeta(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
                    alpha = Math.max(alpha, bestValue.getEvaluationScore());
                }
                else {
                    bestValue = bestValue.min(alphaBeta(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
                    beta = Math.min(beta, bestValue.getEvaluationScore());
                }
                if (alpha >= beta) {
                    break;
                }
            }
        }
        else if (node.getChildren().size() < node.getAllPossibleMoves().size()) {
            // Partially expanded
            List<GameState> childStates = fetchChildStatesInOptimalOrder(node, player);

            for (GameState childState : childStates) {
                AlphaBetaTreeNode childNode = node.fetchChildWithState(childState);
                if (childNode == null) {
                    childNode = tree.createAndAddNode(node, childState);
                }

                if (player == 1) {
                    bestValue = bestValue.max(alphaBeta(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
                    alpha = Math.max(alpha, bestValue.getEvaluationScore());
                }
                else {
                    bestValue = bestValue.min(alphaBeta(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
                    beta = Math.min(beta, bestValue.getEvaluationScore());
                }
                if (alpha >= beta) {
                    break;
                }
            }
        }
        else {
            // Fully expanded
            for (AlphaBetaTreeNode childNode : node.getChildren()) {
                if (player == 1) {
                    bestValue = bestValue.max(alphaBeta(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
                    alpha = Math.max(alpha, bestValue.getEvaluationScore());
                }
                else {
                    bestValue = bestValue.min(alphaBeta(childNode, alpha, beta, fetchNextPlayer(player), depth - 1));
                    beta = Math.min(beta, bestValue.getEvaluationScore());
                }
                if (alpha >= beta) {
                    break;
                }
            }
        }

        return bestValue;
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

    private List<GameState> fetchChildStatesInOptimalOrder(AlphaBetaTreeNode node, int player) {
        List<Move> moves = node.getAllPossibleMoves();

        GameState currentState = node.getGameState();
        List<GameState> childStates = new ArrayList<>();
        for (Move move : moves) {
            GameState childState = currentState.getStateAfterMove(move);
            childStates.add(childState);
        }

        childStates = mergesort(childStates);
        if (player == 1) {
            Collections.reverse(childStates);
        }

        return childStates;
    }

    private int fetchNextPlayer(int currentPlayer) {
        if (currentPlayer < numberOfPlayers) {
            return currentPlayer + 1;
        }
        else {
            return 1;
        }
    }

    private List<GameState> mergesort(List<GameState> list) {
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
        if (tree == null) {
            // Initialize instance fields
            this.numberOfPlayers = gameBoard.getPlayerCount();
            AlphaBetaTreeNode rootNode = new AlphaBetaTreeNode(new GameState(gameBoard), Double.NEGATIVE_INFINITY);
            this.tree = new AlphaBetaTree(rootNode, evaluationFunction, 4, this);
        }
        else {
            // Create subtree
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
            // Winning move
            move = winning.getMoveSequence().get(0);
        }
        else {
            // Non-winning, best move
            move = bestValue.getGameState().getMoveSequence().get(0);
        }

        return move;
    }

    @Override
    public String getTypeName() {
        return "AlphaBeta [" + evaluationFunction + "]";
    }

    @Override
    public String getDescription() {
        return "AlphaBeta-pruning";
    }

    @Override
    public Player getNewInstance() {
        return new AlphaBeta(evaluationFunction);
    }

}