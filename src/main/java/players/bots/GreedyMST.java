package players.bots;

import gamerules.*;
import gamerules.evaluation_functions.SimpleGoalDistance;
import players.Player;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import static gamerules.GameRules.SELECTED_GAMERULES;

public class GreedyMST extends Player {
    EvaluationFunction evaluation;
    private int sizeTree;

    public GreedyMST(){
        this(1,new SimpleGoalDistance());
    }

    public GreedyMST(int sizeTree,EvaluationFunction evaluation){
        this.sizeTree = sizeTree;
        this.evaluation = evaluation;
    }

    @Override
    public Move returnMove(Board gameBoard){
        HashMap<Double,GameTreeNode> eval= new HashMap<>();
        HashMap<GameTreeNode, Integer> moves= new HashMap<>();
        GameState game = new GameState(gameBoard);
        GameTree tree = new GameTree(game);
        List<GameTreeNode> nodes = null;

        for (int i=0; i<sizeTree; i++){
            nodes = tree.expand(i);
        }

        for(GameTreeNode node : nodes){
            moves.put(node,node.getGameState().lastMove().pawn.getPosition().getID());
        }
        for(GameTreeNode node : nodes){
            int index = 1;
            GameTreeNode ne = new GameTreeNode(node,returnMove(node.getGameState(),node.getGameState().currentPlayer()));
            while(!ne.getGameState().hasWinner()){
                if(ne.getGameState().hasWinner()) break;
                ne = new GameTreeNode(ne,returnMove(ne.getGameState(),ne.getGameState().currentPlayer()));
                index+=1;
                if(index>300) break;
            }
            if(ne.getGameState().hasWon(this)){
                eval.put((double)index,node);
            }
            else{
                eval.put((double)index+300000,node);
            }
        }
        GameTreeNode best= eval.get((Collections.min(eval.keySet())));
        List<Move> sequence = best.getGameState().getMoveSequence();
        Move move = new Move(gameBoard.getNode(moves.get(best)).getCurrentPawn(),sequence.get(0).target);
        return move;
    }

    private Move returnMove(GameState board,Player player) {
        GameState gameState = board;
        HashMap<Double,Move> eval= new HashMap<>();
        List<Pawn> pawns = board.getAllPawnsOf(player);
        Collections.shuffle(pawns);
        for (Pawn pawn : pawns) {
            List<Move> moves = board.getAllPossibleMoves(pawn);
            for (Move move : moves) {
                eval.put(evaluation.apply(new GameState(gameState, move), this), move);
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