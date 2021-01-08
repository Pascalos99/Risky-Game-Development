package players.bots;

import gamerules.*;
import gamerules.evaluation_functions.SimpleGoalDistance;
import players.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class GreedyMST extends Player {
    private final EvaluationFunction evaluation;
    private final int treeSize;
    private final int depthTree;
    private final int time;
    private final int randomSize;
    private final int minimumEvaluation;

    public GreedyMST(){
        this(4,1,500,50,30,new SimpleGoalDistance());
    }

    public GreedyMST(int treeSize, int depth,int time,int randomSize,int minimumEvaluation,EvaluationFunction evaluation){
        this.treeSize = treeSize;
        this.depthTree = depth;
        this.time = time;
        this.randomSize = randomSize;
        this.evaluation = evaluation;
        this.minimumEvaluation = minimumEvaluation;
    }

    @Override
    public Move returnMove(Board gameBoard){
        long l1 = System.currentTimeMillis();
        int numberPlayers = gameBoard.getPlayers().size();
        HashMap<Double,GameTreeNode> eval= new HashMap<>();
        HashMap<GameTreeNode,Move> moves= new HashMap<>();
        GameState game = new GameState(gameBoard);
        GameTree tree = new GameTree(game);
        List<GameTreeNode> nodes = null;
        for(int i = 0;i < depthTree ; i++){
            nodes = tree.expand(i);
            tree.setCopyPruning(true);
            for(GameTreeNode node :nodes){
                if(i==0
                        && node.getGameState().hasWon(this)) return node.getGameState().lastMove();
                else if(node.getGameState().hasWon(this)) return node.getGameState().getMoveSequence().get(0);

            }
        }
        for(GameTreeNode node : nodes){
            long t1 = System.currentTimeMillis();
            double all = 0;
            int index = 0;
            while(time/nodes.size()>=System.currentTimeMillis()-t1 || index<minimumEvaluation){
                index++;
                GameTreeNode ne = new GameTreeNode(node,returnMove(node.getGameState(),node.getGameState().currentPlayer()));
                for(int i2=0;i2<(treeSize*numberPlayers)-1;i2++){
                    if(ne.getGameState().hasWinner()){
                        if(ne.getGameState().hasWon(this)) all += 100/i2;
                        break;
                    }
                    ne = new GameTreeNode(ne,returnMove(ne.getGameState(),ne.getGameState().currentPlayer()));
                }
                all += evaluation.apply(ne.getGameState(),this);
            }
            all /= index;
            eval.put(all,node);

        }
        GameTreeNode best= eval.get((Collections.max(eval.keySet())));
        List<Move> sequence = best.getGameState().getMoveSequence();
        if (sequence.size() == 0) return null;
        return sequence.get(0);
    }

    private Move returnMove(GameState board,Player player) {
        GameState gameState = board;
        HashMap<Double,Move> eval= new HashMap<>();
        List<Pawn> pawns = board.getAllPawnsOf(player);
        Collections.shuffle(pawns);
        for (Pawn pawn : pawns) {
            List<Move> moves = board.getAllPossibleMoves(pawn);
            for (Move move : moves) {
                Random rd = new Random();
                eval.put(evaluation.apply(new GameState(gameState, move), this) + rd.nextGaussian()*randomSize, move);
            }
        }
        return eval.get((Collections.max(eval.keySet())));
    }

    @Override
    public String getTypeName() {
        return "Monte carlo search tree greedy";
    }

    @Override
    public String getDescription() {
        return "A monte carlo algorithm who determine the score by using greedy move";
    }

    @Override
    public Player getNewInstance() {
        return new GreedyMST(treeSize, depthTree, time, randomSize, minimumEvaluation, evaluation);
    }

    public String toString() {
        return getName()+" ("+getTypeName()+")";
    }
}