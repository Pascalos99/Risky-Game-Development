package gamerules;

import java.util.*;
import java.util.stream.Collectors;

import players.Player;

public class DefaultGameRules implements GameRules {

	@Override
	public boolean allowMove(Board board, Pawn pawn, BoardNode target) {
		return (getAllPossibleMoves(board,pawn).contains(target));
	}

	@Override
	public List<Move> getAllPossibleMoves(Board board, Pawn pawn) {
		// implement by Arthur brutforce method
		Set<Move> moves = new HashSet<Move>();
		for (BoardNode neighbours : pawn.getPosition().getNeighbors()){
			if(neighbours.getCurrentPawn()!=null) moves.add(new Move(pawn,neighbours));
		}
		recursiveMove(pawn.getPosition(),moves,pawn);
		return moves.stream().collect(Collectors.toList());
	}

	// full field a set with the possiblility
	private void recursiveMove(BoardNode place, Set<Move> movesfinale, Pawn pawn){
		List<BoardNode> moves = new ArrayList<BoardNode>();
		for (BoardNode neighbour : place.getNeighbors()){
			for (BoardNode neighbour2 : place.getNeighbors()) {
				if (neighbour2.getCurrentPawn() != null) moves.add(neighbour2);
			}
		}
		for(BoardNode nextNode : place.getNeighbors()) {
			if(nextNode.getCurrentPawn() != null) {
				for(BoardNode nextNode2 : place.getNeighbors()){
					if(!moves.contains(nextNode2) && !movesfinale.contains(nextNode2)){
						movesfinale.add(new Move(pawn,nextNode2));
						recursiveMove(nextNode2,movesfinale,pawn);
						break;
					}
				}
			}
		}
	}

	@Override
	public boolean hasWon(Board board, Player Player) {
		// TODO implement method
		return false;
	}

}
