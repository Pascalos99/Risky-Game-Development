package players.bots;

import gamerules.*;
import gamerules.evaluation_functions.SimpleGoalDistance;
import players.Player;

import java.util.*;

public class AStar extends Player {

    HashMap<BoardNode,Double> ListOfParents;
    Move best;
    private final EvaluationFunction evaluation;

    public AStar(){
        ListOfParents = new HashMap<>();
        best = null;
        this.evaluation = new SimpleGoalDistance();
    }

    @Override
    public Move returnMove(Board gameBoard) {

        GameState root = new GameState(gameBoard);
        GameTree tree = new GameTree(root);
        List<GameTreeNode> nodes = tree.expand(0);
        HashMap<Double,GameTreeNode> unexploredNode = new HashMap<>();
        for(GameTreeNode node : nodes){
            double score = (evaluation.apply(node.getGameState(),this) + node.getDepth())*-1;
            unexploredNode.put(score,node);
        }
        long t1 = System.currentTimeMillis();
        while(500 >= System.currentTimeMillis()-t1){
            Double minValueInMap =(Collections.min(unexploredNode.keySet()));
            GameTreeNode best = unexploredNode.get(minValueInMap);
            unexploredNode.remove(minValueInMap);
            nodes = tree.expand(best);
            if(best.getGameState().hasWon(this)) break;
            for(GameTreeNode node : nodes){
                double score = (evaluation.apply(node.getGameState(),this) + node.getDepth())*-1;
                unexploredNode.put(score,node);
            }
        }
        double minValueInMap =(Collections.min(unexploredNode.keySet()));
        GameTreeNode best = unexploredNode.get(minValueInMap);
        return best.getGameState().getMoveSequence().get(0);
    }

    @Override
    public String getTypeName() {
        return "AStar";
    }

    @Override
    public String getDescription() {
        return " ";
    }

    @Override
    public Player getNewInstance() {
        return new AStar();
    }

    public double heuristic(BoardNode node1 ,BoardNode node2){
        return Math.abs(node1.getID() - node2.getID());
    }

}
