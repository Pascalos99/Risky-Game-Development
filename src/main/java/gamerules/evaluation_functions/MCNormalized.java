package gamerules.evaluation_functions;

import gamerules.*;
import players.Player;
import players.bots.utils.NodeDistanceCalc;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class MCNormalized implements EvaluationFunction {
    private double max = 30;
    private double min = 139;
    private double sum = -1.01;
    private final EvaluationFunction evaluation;
    private final int treeSize;
    private final int depthTree;
    private final int time;
    private final int randomSize;
    private final int minimumEvaluation;
    private static BigInteger gameStateID;
    private static HashMap<Double, GameTreeNode> eval;

    public MCNormalized(){
        this(new SimpleGoalDistance());
    }
    public MCNormalized(EvaluationFunction evaluation){
        this(4,1,500,50,10,evaluation);
    }

    public MCNormalized(int treeSize, int depth,int time,int randomSize,int minimumEvaluation,EvaluationFunction evaluation){
        this.treeSize = treeSize;
        this.depthTree = depth;
        this.time = time;
        this.randomSize = randomSize;
        this.minimumEvaluation = minimumEvaluation;
        this.evaluation = evaluation;
        eval= new HashMap<>();
        gameStateID = null;
    }

    @Override
    public Double apply(GameState gameState, Player player) {
        if(gameStateID == null || !gameStateID.equals(gameState.getPrevious().gameStateID())){
            gameStateID = gameState.getPrevious().gameStateID();
            int numberPlayers = (int) (Math.log10(gameState.getIntegerRepresentation().length) + 1);
            eval.clear();
            GameState game = gameState.getPrevious();
            GameTree tree = new GameTree(game);
            List<GameTreeNode> nodes = null;
            for(int i = 0;i < depthTree ; i++){
                nodes = tree.expand(i);
                tree.setCopyPruning(true);
                for(GameTreeNode node :nodes){
                    if(i==0
                            && node.getGameState().hasWon(player)) eval.put(Integer.MAX_VALUE + 0.1,node);

                }
            }
            for(GameTreeNode node : nodes){
                if(eval.containsKey(Integer.MAX_VALUE + 0.1)
                        && eval.get(Integer.MAX_VALUE + 0.1) == node) continue;
                long t1 = System.currentTimeMillis();
                double all = 0;
                int index = 0;
                while(time/nodes.size()>=System.currentTimeMillis()-t1 || index<minimumEvaluation){
                    index++;
                    GameTreeNode ne = new GameTreeNode(node,returnMove(node.getGameState(),node.getGameState().currentPlayer()));
                    for(int i2=0;i2<(treeSize*numberPlayers)-1;i2++){
                        if(ne.getGameState().hasWinner()){
                            if(ne.getGameState().hasWon(player)) all += 100/i2;
                            break;
                        }
                        ne = new GameTreeNode(ne,returnMove(ne.getGameState(),ne.getGameState().currentPlayer()));
                    }
                    all += evaluation.apply(ne.getGameState(),player);
                }
                all /= index;
                eval.put(all,node);
                if(node.getGameState().lastMove().equals(gameState.lastMove())) sum = all;
            }
            max = Collections.max(eval.keySet());
            min = Collections.min(eval.keySet());

        }
        else{
            sum = getKeyFromValue(gameState.lastMove());
        }
        if(sum == -1.01) return 0.;
        sum -= min;
        sum /= (max - min);
//        System.out.println(sum);
        return sum;
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
                eval.put(evaluation.apply(new GameState(gameState, move), player) + (Math.random() * randomSize) - (randomSize/2), move);
            }
        }
        return eval.get((Collections.max(eval.keySet())));
    }

    public static double getKeyFromValue(Move value) {
        for (Double o : eval.keySet()) {
            if (eval.get(o).getGameState().lastMove().equals(value)){
                return o;
            }
        }
        return -1.01;
    }
}
