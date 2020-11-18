package players.bots;

import gamerules.*;
import players.Player;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import static gamerules.GameRules.SELECTED_GAMERULES;

public class ProtoMST extends Player {

    @Override
    public Move returnMove(Board gameBoard){
        List<BoardNode> tragets = gameBoard.getGoal(this);
        HashMap<Double,GameTreeNode> eval= new HashMap<>();
        HashMap<GameTreeNode,Move> moves= new HashMap<>();
        GameState game = new GameState(gameBoard);
        GameTree tree = new GameTree(game);
        List<GameTreeNode> nodes = tree.expand(0);
        for(GameTreeNode node :nodes){
            moves.put(node,node.getGameState().lastMove());
        }
        for(GameTreeNode node : nodes){
            long t1 = System.currentTimeMillis();
            while(500/nodes.size()>=System.currentTimeMillis()-t1){
                GameTreeNode ne = new GameTreeNode(node,returnMove(node.getGameState(),node.getGameState().currentPlayer()));
                for(int i2=0;i2<10;i2++){
                    if(!tragets.contains(ne.getGameState().lastMove().getStart(gameBoard))){
                        if(tragets.contains(ne.getGameState().lastMove().getTarget(gameBoard))){
                            System.out.println("ddd");
                            break;
                        }
                    }
                    ne = new GameTreeNode(ne,returnMove(ne.getGameState(),ne.getGameState().currentPlayer()));

                }
                if (eval.containsKey(ne.getGameState().currentScore(this))) eval.put(ne.getGameState().currentScore(this) + Math.random()-0.5,node);
                else eval.put(ne.getGameState().currentScore(this),node);
            }
        }
        GameTreeNode best= eval.get((Collections.max(eval.keySet())));
        Move move = moves.get(best);
        return move;

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