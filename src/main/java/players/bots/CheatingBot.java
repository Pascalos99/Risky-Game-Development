package players.bots;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import gamerules.Board;
import gamerules.BoardNode;
import gamerules.GameRules;
import gamerules.Move;
import gamerules.Pawn;
import players.Player;

public class CheatingBot extends Player implements DeterministicReturn {

	@Override
	public Move returnMove(Board gameBoard) {
		
		if (new Random().nextDouble() < 0.7) return null;
		
		GameRules old_rules = GameRules.SELECTED_GAMERULES;
		Player me = this;
		
		GameRules.SELECTED_GAMERULES = new GameRules() {

			@Override
			public boolean allowMove(Pawn pawn, BoardNode target) {
				return true;
			}

			@Override
			public List<Move> getAllPossibleMoves(Pawn pawn) {
				ArrayList<Move> allmoves = new ArrayList<>();
				for (BoardNode node : gameBoard.getAllnodes())
					if (node.isEmpty() && node.getOwner() == me.getEnemy(gameBoard)) allmoves.add(new Move(pawn, node));
				return allmoves;
			}

			@Override
			public boolean hasWon(Board board, Player player) {
				return old_rules.hasWon(board, player);
			}
			
		};
		long starttime = System.currentTimeMillis();
		Thread timer = new Thread(new Runnable() {

			@Override
			public void run() {
				while (System.currentTimeMillis() - starttime < 200);
				GameRules.SELECTED_GAMERULES = old_rules;
			}
			
		});
		Move result = null;
		List<Pawn> pawns = gameBoard.getAllPawnsOf(this);
		Collections.shuffle(pawns);
		for (Pawn pawn : pawns) {
			if (pawn.getPosition().getOwner() == this.getEnemy(gameBoard)) continue;
			List<Move> available_moves = GameRules.SELECTED_GAMERULES.getAllPossibleMoves(pawn);
			if (available_moves.size() > 0) result = available_moves.get(0);
		}
		
		timer.start();
		
		return result;
	}

	@Override
	public String getTypeName() {
		return "Cheating bot";
	}

	@Override
	public String getDescription() {
		return "This bot likes to cheat";
	}

}
