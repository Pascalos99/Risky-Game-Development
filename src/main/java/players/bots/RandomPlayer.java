package players.bots;

import static gamerules.GameRules.SELECTED_GAMERULES;

import java.util.Collections;
import java.util.List;

import gamerules.Board;
import gamerules.GameState;
import gamerules.Move;
import gamerules.Pawn;
import players.Player;

public class RandomPlayer extends Player {
	
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


	public Move returnMove(GameState board) {
		List<Pawn> pawns = board.getAllPawnsOf(this);
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
		return "Random Player";
	}

	@Override
	public String getDescription() {
		return "A bot that randomly picks a move";
	}

	@Override
	public Player getNewInstance() {
		return new RandomPlayer();
	}
	
	public String toString() {
		return getName()+" ("+getTypeName()+")";
	}

}
