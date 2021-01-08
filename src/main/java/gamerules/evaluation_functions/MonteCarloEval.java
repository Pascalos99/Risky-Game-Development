package gamerules.evaluation_functions;

import gamerules.*;
import players.Player;
import players.bots.utils.NeuralNetwork;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static players.bots.utils.NeuralNetwork.SIGMOID;
import static players.bots.utils.NeuralNetwork.dSILU;

public class MonteCarloEval implements EvaluationFunction {

    private final double maxTime;
    private final double minNumberEvaluation;
    private final int treeSize;
    private final int randomSize;
    private final NeuralNetwork ann;

    public MonteCarloEval() {
        this(8, 10,3,15);
    }

    public MonteCarloEval(double maxTime, double minNumberEvaluation,int treeSize,int randomSize) {
        this.maxTime = maxTime;
        this.minNumberEvaluation = minNumberEvaluation;
        this.treeSize = treeSize;
        this.randomSize = randomSize;
        // The final structure of the ANN
        int[] structure = {162, 81, 20, 20, 1};
        // The different activation function use in the ANN
        NeuralNetwork.Activation[] activations = {dSILU, dSILU, dSILU, SIGMOID};
        this.ann = new NeuralNetwork(structure, activations);
        // the different weight
        double[][][] weights = {};
//        this.ann.loadWeights(weights);
        // for being able to test it
        this.ann.initializeRandomWeights(-1,1);

    }
    @Override
    public Double apply(GameState gameState, Player player) {
        List<Integer> blackList = player.getOtherPlayersBase(gameState.getOriginalBoard());
        if(blackList.contains(gameState.lastMove().target_node) || blackList.contains(gameState.lastMove().start_node))
            return -Double.MAX_VALUE;
        double score = 0;
        int numberPlayers = gameState.getOriginalBoard().getPlayerCount();
        GameTree tree = new GameTree(gameState);
        GameTreeNode node = tree.getRoot();
        long t1 = System.currentTimeMillis();
        int index = 0;
        while(maxTime >= System.currentTimeMillis()-t1 || index < minNumberEvaluation){
            index++;
            GameTreeNode ne = new GameTreeNode(node,returnMove(node.getGameState(),node.getGameState().currentPlayer()));
            for(int i2=0;i2<(treeSize*numberPlayers)-1;i2++){
                if(ne.getGameState().hasWinner()){
                    if(ne.getGameState().hasWon(player)) score += 100.0/i2;
                    break;
                }
                ne = new GameTreeNode(ne,returnMove(ne.getGameState(),ne.getGameState().currentPlayer()));
            }
            score += ann.forwardProp(ne.getGameState().getMatrix(player))[0];
            System.out.println(score);
        }
        return score;
    }

    private Move returnMove(GameState board,Player player) {
        List<Integer> blackList = player.getOtherPlayersBase(board.getOriginalBoard());
        HashMap<Double,Move> eval= new HashMap<>();
        List<Pawn> pawns = board.getAllPawnsOf(player);
        Collections.shuffle(pawns);
        for (Pawn pawn : pawns) {
            List<Move> moves = board.getAllPossibleMoves(pawn);
            for (Move move : moves){
                if(!blackList.contains(move.target_node))
                    eval.put(ann.forwardProp(new GameState(board,move).getMatrix(player))[0] + (Math.random()*randomSize) - (randomSize/2),move);
            }
        }
        return eval.get((Collections.max(eval.keySet())));
    }
    
    public String toString() {
    	return "MCEval";
    }
}
