package players.bots;

import gamerules.Board;
import gamerules.BoardNode;
import gamerules.Move;
import gamerules.Pawn;
import players.Player;
import players.bots.utils.Dijkstra;

import java.util.Collections;
import java.util.List;
import static gamerules.GameRules.SELECTED_GAMERULES;

public class NaivePlayer extends Player {

    @Override
    public Move returnMove(Board gameBoard) {
        //long t1 = System.currentTimeMillis();
        List<Pawn> pawns = gameBoard.getAllPawnsOf(this);
        Move move = null;
        int min = 14;
        Pawn pawn = pawns.get((int) (Math.random()*pawns.size()));
        while(SELECTED_GAMERULES.getAllPossibleMoves(pawn).size()<=0){
            pawn = pawns.get((int) (Math.random()*pawns.size()));
        }
        List<Move> moves = SELECTED_GAMERULES.getAllPossibleMoves(pawn);
        Collections.shuffle(moves);
        for (Move move1 : moves) {
            if(move1.target.getOwner() == getEnemy(gameBoard)) return move1;
            int distance = distanceTarget(gameBoard, move1.target,pawn.getPosition());
            if (distance <= min) {
                move = move1;
                min = distance;
            }
        }
        return move;
    }

    private int distanceTarget(Board board, BoardNode position,BoardNode currentPosition){
        List<BoardNode> tagets = board.getGoal(this);
        if(tagets.contains(currentPosition) && ! tagets.contains(position)) return Integer.MAX_VALUE;
        if(tagets.contains(position)) return 0;
        int distance= (Dijkstra.getDistance(tagets.get((int)Math.random()*tagets.size()),position));
        return distance;
    }

    @Override
    public String getTypeName() {
        return "Naive Player";
    }

    @Override
    public String getDescription() {
        return "A bot that take randomly a pawn and find the move who will get him closer to the goal";
    }

	@Override
	public Player getNewInstance() {
		return new NaivePlayer();
	}
	
	public String toString() {
		return getName()+" ("+getTypeName()+")";
	}
}
