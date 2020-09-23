package gamerules;

import java.util.*;
import java.util.stream.Collectors;

import players.Player;

public class DefaultGameRules implements GameRules {

	@Override
	public boolean allowMove(Board board, Pawn pawn, BoardNode target) {
		if (pawn.getPosition().getNeighbors().contains(target)) return true;
		return (getAllPossibleMoves(board,pawn).contains(new Move(pawn, target)));
	}
	
	@Override
	public List<Move> getAllPossibleMoves(Board board, Pawn pawn) {
		// implement by Arthur brutforce method
		Set<Move> moves = new HashSet<Move>();
		for (BoardNode neighbor : pawn.getPosition().getNeighbors()){
			if(neighbor.isEmpty()) moves.add(new Move(pawn,neighbor));
		}
		recursiveMove(pawn.getPosition(),moves,pawn);
		return moves.stream().collect(Collectors.toList());
	}

	// full field a set with the possiblility
	private void recursiveMove(BoardNode place, Set<Move> movesfinale, Pawn pawn){
		List<BoardNode> moves = new ArrayList<BoardNode>();
		for (BoardNode neighbor : place.getNeighbors()){
			for (BoardNode neighbor2 : place.getNeighbors()) {
				if (neighbor2.isOccupied()) moves.add(neighbor2);
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
	
	public String toString() {
		return "Default Gamerules";
	}

	@Override
	public boolean hasWon(Board board, Player Player) {
		// TODO implement method
		return false;
	}

}
