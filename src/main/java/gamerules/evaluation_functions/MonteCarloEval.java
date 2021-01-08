package gamerules.evaluation_functions;

import gamerules.*;
import players.Player;
import players.bots.utils.NeuralNetwork;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static players.bots.utils.NeuralNetwork.*;

public class MonteCarloEval implements EvaluationFunction {

    private final double maxTime;
    private final double minNumberEvaluation;
    private final int treeSize;
    private final int randomSize;
    private NeuralNetwork ann;

    public MonteCarloEval() {
        this(8, 1,1,15);
    }

    public MonteCarloEval(double maxTime, double minNumberEvaluation,int treeSize,int randomSize) {
        this.maxTime = maxTime;
        this.minNumberEvaluation = minNumberEvaluation;
        this.treeSize = treeSize;
        this.randomSize = randomSize;
        try {
            this.ann = NeuralNetwork.readFromFile(new File(networkPath+"FirstTrain.network"))[0].clone();
        } catch (IOException e) {
            e.printStackTrace();
        };
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
            for (Move move : moves) {
                if (!blackList.contains(move.target_node)) {
                    Random rd = new Random();
                    eval.put(ann.forwardProp(new GameState(board, move).getMatrix(player))[0] + rd.nextGaussian() * randomSize, move);
                }
            }
        }
        return eval.get((Collections.max(eval.keySet())));
    }
    
    public String toString() {
    	return "MCEval";
    }
}
