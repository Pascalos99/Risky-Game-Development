package players.bots;

import gamerules.*;
import gamerules.evaluation_functions.SimpleGoalDistance;
import players.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ProtoMST extends Player {
    private final EvaluationFunction evaluation;
    private final int treeSize;
    private final int depthTree;
    private final int time;
    private final int minimumEvaluation;

    public ProtoMST(int treeSize, int depth,int time,int minimumEvaluation,EvaluationFunction evaluation){
        this.treeSize = treeSize;
        this.depthTree = depth;
        this.time = time;
        this.evaluation = evaluation;
        this.minimumEvaluation = minimumEvaluation;
    }

    public ProtoMST(){
        this(5,1,5000,500,new SimpleGoalDistance());
    }

    @Override
    public Move returnMove(Board gameBoard){
        HashMap<Double,GameTreeNode> eval= new HashMap<>();
        GameState game = new GameState(gameBoard);
        GameTree tree = new GameTree(game);
        List<GameTreeNode> nodes = null;
        for(int i = 0;i < depthTree ; i++){
            nodes = tree.expand(i);
            for(GameTreeNode node :nodes){
                if(i==0
                   && node.getGameState().hasWon(this)) return node.getGameState().lastMove();
                //else if(node.getGameState().hasWon(this)) return node.getGameState().getMoveSequence().get(0);
            }
        }
        for(GameTreeNode node : nodes){
            long t1 = System.currentTimeMillis();
            double all = 0;
            int index = 0;
            while(time/nodes.size()>=System.currentTimeMillis()-t1 || index<minimumEvaluation){
                index++;
                GameTreeNode ne = new GameTreeNode(node,returnMove(node.getGameState(),node.getGameState().currentPlayer()));
                for(int i2=0;i2<treeSize*gameBoard.getPlayers().size();i2++){
                    if(ne.getGameState().hasWinner()) break;
                    ne = new GameTreeNode(ne,returnMove(ne.getGameState(),ne.getGameState().currentPlayer()));
                }
                all += evaluation.eval(ne.getGameState(),this);
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
        return "Monte carlo search tree random";
    }

    @Override
    public String getDescription() {
        return "A monte carlo algorithm who determine the score by using random move";
    }

    @Override
    public Player getNewInstance() {
        return new ProtoMST();
    }

    public String toString() {
        return getName()+" ("+getTypeName()+")";
    }
}