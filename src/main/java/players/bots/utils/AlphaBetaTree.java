package players.bots.utils;

import gamerules.EvaluationFunction;
import gamerules.GameState;
import gamerules.Move;
import gamerules.evaluation_functions.PaperEval;
import gamerules.evaluation_functions.SimpleGoalDistance;
import players.bots.AlphaBeta;

import java.util.ArrayList;
import java.util.List;

public class AlphaBetaTree {

    private final AlphaBetaTreeNode root;
    private final EvaluationFunction evaluationFunction;
    private final int maxDepth;
    private final AlphaBeta maximizingPlayer;
    private final List<List<AlphaBetaTreeNode>> layers;

    public AlphaBetaTree(AlphaBetaTreeNode root, int maxDepth, AlphaBeta maximizingPlayer) {
        this.root = root;
        this.evaluationFunction = new SimpleGoalDistance();
//        this.evaluationFunction = new PaperEval();
        this.maxDepth = maxDepth;
        this.maximizingPlayer = maximizingPlayer;
        this.layers = new ArrayList<>();
        for (int i = 0; i <= maxDepth; i++) {
            layers.add(new ArrayList<>());
        }
        this.layers.get(0).add(root);
    }

    public AlphaBetaTree(AlphaBetaTreeNode root, EvaluationFunction evaluationFunction, int maxDepth, AlphaBeta maximizingPlayer, List<List<AlphaBetaTreeNode>> layers) {
        this.root = root;
        this.evaluationFunction = evaluationFunction;
        this.maxDepth = maxDepth;
        this.maximizingPlayer = maximizingPlayer;
        this.layers = layers;
    }

    public double computeEvaluationScore(GameState gameState) {
        return evaluationFunction.eval(gameState, maximizingPlayer);
    }

    public void expandTree(AlphaBetaTreeNode parentNode, int depth) {
        if (depth != maxDepth) {
            for (Move move : parentNode.getAllPossibleMoves()) {
                GameState childState = parentNode.getGameState().getStateAfterMove(move);
                AlphaBetaTreeNode childNode = new AlphaBetaTreeNode(parentNode, childState, computeEvaluationScore(childState));
                parentNode.addChild(childNode);
                expandTree(childNode, depth + 1);
            }
        }
    }

    public AlphaBetaTree createSubtree(AlphaBetaTreeNode newRoot, GameState newState) {
        AlphaBetaTreeNode rootNode = new AlphaBetaTreeNode(newState, newRoot.getChildren());
        List<List<AlphaBetaTreeNode>> newLayers = new ArrayList<>();
        for (int i = 0; i <= maxDepth; i++) {
            newLayers.add(new ArrayList<>());
        }
        newLayers.get(0).add(rootNode);
        for (AlphaBetaTreeNode childNode : rootNode.getChildren()) {
            childNode.getGameState().setParent(newState);
            buildLayers(childNode, newLayers, 1, newState);
        }
        return new AlphaBetaTree(rootNode, evaluationFunction, maxDepth, maximizingPlayer, newLayers);
    }

    public void buildLayers(AlphaBetaTreeNode node, List<List<AlphaBetaTreeNode>> newLayers, int depth, GameState rootState) {
        newLayers.get(depth).add(node);
        node.getGameState().setRoot(rootState);
        node.getGameState().setDepth(depth);
        for (AlphaBetaTreeNode childNode : node.getChildren()) {
            buildLayers(childNode, newLayers, depth + 1, rootState);
        }
    }

    public void removeNode(AlphaBetaTreeNode node) {
        node.getParent().removeChild(node);
        node.setParent(null);
        layers.get(node.getGameState().getDepth()).remove(node);
    }

    public AlphaBetaTreeNode getRoot() {
        return root;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public void addNodeToLayer(AlphaBetaTreeNode node, int depth) {
        layers.get(depth).add(node);
    }

    public List<List<AlphaBetaTreeNode>> getLayers() {
        return layers;
    }

    public EvaluationFunction getEvaluationFunction() {
        return evaluationFunction;
    }

}