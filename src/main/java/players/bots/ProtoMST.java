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

public class ProtoMST extends Player {
    private EvaluationFunction evaluation;
    private int treeSize;
    private int depthTree;
    private int time;

    public ProtoMST(int treeSize, int depth,int time,EvaluationFunction evaluation){
        this.treeSize = treeSize;
        this.depthTree = depth;
        this.time = time;
        this.evaluation = evaluation;
    }

    public ProtoMST(){
        this(10,2,10000,new SimpleGoalDistanceImprove());
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
        List<Pawn> pawns = board.getAllPawnsOf(player);
        Collections.shuffle(pawns);
        for (Pawn pawn : pawns) {
            List<Move> moves = board.getAllPossibleMoves(pawn);
            if (moves.size() > 0) {
                Collections.shuffle(moves);
                return moves.get(0);
            }
        }
        return null;
    }

    @Override
    public String getTypeName() {
        return "Proto1";
    }

    @Override
    public String getDescription() {
        return "Fuck other Player";
    }

    @Override
    public Player getNewInstance() {
        return new ProtoMST();
    }

    public String toString() {
        return getName()+" ("+getTypeName()+")";
    }
}