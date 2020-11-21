package players.bots;

import gamerules.*;
import gamerules.evaluation_functions.SimpleGoalDistance;
import gamerules.evaluation_functions.SimpleGoalDistanceImprove;
import players.Player;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import static gamerules.GameRules.SELECTED_GAMERULES;

public class GreedyMST extends Player {
    private EvaluationFunction evaluation;
    private int treeSize;
    private int depthTree;
    private int time;
    private int randomSize;

    public GreedyMST(){
        this(10,2,10000,10,new SimpleGoalDistance());
    }

    public GreedyMST(int treeSize, int depth,int time,int randomSize,EvaluationFunction evaluation){
        this.treeSize = treeSize;
        this.depthTree = depth;
        this.time = time;
        this.randomSize = randomSize;
        this.evaluation = evaluation;
    }

    @Override
    public Move returnMove(Board gameBoard){
        HashMap<Double,GameTreeNode> eval= new HashMap<>();
        HashMap<GameTreeNode,Move> moves= new HashMap<>();
        GameState game = new GameState(gameBoard);
        GameTree tree = new GameTree(game);
        List<GameTreeNode> nodes = null;
        for(int i = 0;i < depthTree ; i++){
            nodes = tree.expand(0);
            for(GameTreeNode node :nodes){
                moves.put(node,node.getGameState().lastMove());
                if(i==0
                        && node.getGameState().hasWon(this)) return node.getGameState().lastMove();
            }
        }
        for(GameTreeNode node : nodes){
            long t1 = System.currentTimeMillis();
            double all = 0;
            int index = 0;
            while(time/nodes.size()>=System.currentTimeMillis()-t1){
                index++;
                GameTreeNode ne = new GameTreeNode(node,returnMove(node.getGameState(),node.getGameState().currentPlayer()));
                for(int i2=0;i2<treeSize;i2++){
                    if(ne.getGameState().hasWinner()) break;
                    ne = new GameTreeNode(ne,returnMove(ne.getGameState(),ne.getGameState().currentPlayer()));
                }
                all += ne.getGameState().currentScore(this);
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
                eval.put(evaluation.apply(new GameState(gameState, move), this) + (Math.random() * randomSize) - (randomSize/2), move);
            }
        }
        return eval.get((Collections.max(eval.keySet())));
    }

    @Override
    public String getTypeName() {
        return "Proto2";
    }

    @Override
    public String getDescription() {
        return "Fuck other Player or not";
    }

    @Override
    public Player getNewInstance() {
        return new GreedyMST();
    }

    public String toString() {
        return getName()+" ("+getTypeName()+")";
    }
}