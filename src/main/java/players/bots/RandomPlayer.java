package players.bots;

import static gamerules.GameRules.SELECTED_GAMERULES;

import java.util.Collections;
import java.util.List;

import gamerules.Board;
import gamerules.Move;
import gamerules.Pawn;
import players.Player;

public class RandomPlayer extends Player {

	private static int random_count = 0;
	private int ID;
	
	public RandomPlayer() {
		ID = ++random_count;
	}
	
	@Override
	public Move returnMove(Board board) {
		List<Pawn> pawns = board.getAllPawnsOf(this);
    	Collections.shuffle(pawns);
    	for (Pawn pawn : pawns) {
    		List<Move> moves = SELECTED_GAMERULES.getAllPossibleMoves(pawn);
    		if (moves.size() > 0) {
    			Collections.shuffle(moves);
    			return moves.get(0);
    		}
    	}
    	return null;
	}

	@Override
	public String getTypeName() {
		return "Random Bot";
	}

	@Override
	public String getDescription() {
		return "A bot that randomly picks a move";
	}
	
	public String toString() {
		return "RandomBot-"+ID;
	}

}
