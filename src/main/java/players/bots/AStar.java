package players.bots;

import gamerules.*;
import players.Player;

import java.util.*;

public class AStar extends Player {

    HashMap<BoardNode,Double> ListOfParents;
    Move best;

    public AStar(){
        ListOfParents = new HashMap<>();
        best = null;
    }

    @Override
    public Move returnMove(Board gameBoard) {

        GameState root = new GameState(gameBoard);
        List<Pawn> pawns = root.getAllPawnsOf(this);

        Set<BoardNode> exploredNode = new HashSet<BoardNode>();
        PriorityQueue<BoardNode> unexploredNode = new PriorityQueue<>();

        int nearest = Integer.MAX_VALUE;
        Pawn pawn = null;
        BoardNode goalnode = null;

        for(Pawn p:pawns) {

            int pawnId = p.getPosition().getID();
            int closest = Integer.MAX_VALUE;
            BoardNode goal = null;
            for (BoardNode b : this.getGoalNodes(gameBoard)) {

                if (Math.abs(pawnId - b.getID()) < closest) {
                    closest = Math.abs(pawnId - b.getID());
                    goal = b;
                }
            }
            if(closest<nearest) {
                nearest = closest;
                pawn = p;
                goalnode = goal;
            }
        }

        pawn.getPosition().setgScore(0);
        unexploredNode.add(pawn.getPosition());
        boolean found = false;

        while(!unexploredNode.isEmpty() && !found) {
            BoardNode currentNode = unexploredNode.poll();
            exploredNode.add(currentNode);

            if (currentNode == goalnode)
                found = true;

            for (BoardNode boardNode : currentNode.getNeighbours()) {

                if (boardNode.isOccupied()) {
                    continue;
                }

//                if (boardNode.getfScore() == 0)
//                    boardNode.setfScore(Double.MAX_VALUE);

                double cost = 1;
                double tempGScore = currentNode.getgScore() + cost;
                double tempFScore = heuristic(boardNode, goalnode) + tempGScore;

                if (exploredNode.contains(boardNode) && tempFScore >= boardNode.getfScore()) {
                    continue;
                } else if (unexploredNode.contains(boardNode) || tempFScore < boardNode.getfScore()) {

                    boardNode.setParent(currentNode);
                    boardNode.setgScore(tempGScore);
                    boardNode.setfScore(tempFScore);

                    if (unexploredNode.contains(boardNode))
                        unexploredNode.remove(boardNode);

                    unexploredNode.add(boardNode);
                }
            }
        }

        List<BoardNode> pathList = new ArrayList<>();
        for (BoardNode node = goalnode; node != null; node = node.getParent()) {
            pathList.add(node);
        }
        Collections.reverse(pathList);

        for(Move m : root.getAllPossibleMoves(pawn))
        {
            if(m.target_node == pathList.get(1).getID())
                best = m;
        }
        return best;
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
