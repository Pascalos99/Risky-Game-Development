package gamerules;

import java.util.*;

import players.Player;

public class DefaultGameRules implements GameRules {

	@Override
	public boolean allowMove(Pawn pawn, BoardNode target) {
		if (pawn.getPosition().getNeighbours().contains(target)) return true;
		return (getAllPossibleMoves(pawn).contains(new Move(pawn, target)));
	}
	
	@Override
	public List<Move> getAllPossibleMoves(Pawn pawn) {
		//System.out.println("get all moves");
		HashSet<Move> moves = new HashSet<>();
		HashSet<BoardNode> visited = new HashSet<>(121);
		visited.add(pawn.getPosition());
		recurseMoves(pawn, pawn.getPosition(), moves, visited);
		ArrayList<Move> result = new ArrayList<>(moves.size());
		for (Move m : moves) result.add(m);
		return result;
	}
	
	public void recurseMoves(Pawn pawn, BoardNode previous, Set<Move> moves, Set<BoardNode> visited) {
		Set<BoardNode> neighbours = previous.getNeighbours();
		for (BoardNode neighbour : neighbours) {
			if (visited.contains(neighbour)) continue;
			visited.add(neighbour);
			if (neighbour.isEmpty()) {
				if (pawn == previous.getCurrentPawn()) moves.add(new Move(pawn, neighbour));
			} else {
				BoardNode[] jumps = new BoardNode[3]; int i = 0;
				BoardNode[] common_neighbours = new BoardNode[2]; int cn = 0;
				for (BoardNode potential_jump : neighbour.getNeighbours()) {
					if (potential_jump == previous) continue;
					try {
						if (previous.isNeighbour(potential_jump)) { common_neighbours[cn++] = potential_jump; continue; }
						jumps[i++] = potential_jump;
					} catch(java.lang.ArrayIndexOutOfBoundsException e) {
						e.printStackTrace();
						System.out.println("error while jumping from "+previous+" over "+neighbour+"\nThis is most likey caused by"
								+ " incorrect entries in AdjacencyMap.java, if you find any while testing, please correct them");
					}
				} // three left over, of which only one is a valid jump
				BoardNode jump_to = null;
				
				for (int a=0; a < i; a++) {
					if (jumps[a] == null) break;
					jump_to = jumps[a];
					for (int b=0; b < cn; b++) {
						if (common_neighbours[b] != null && jump_to.isNeighbour(common_neighbours[b])) { jump_to = null; break; }
					}
					if (jump_to != null) break;
				}
				
				if (jump_to == null) continue;
				
				if (jump_to.isEmpty()) {
					if (jump_to.equals(pawn.getPosition())) { moves.add(new Move(pawn, pawn.getPosition())); continue; }
					if (visited.contains(jump_to)) continue;
					moves.add(new Move(pawn, jump_to));
					recurseMoves(pawn, jump_to, moves, visited);
				}
			}
		}
	}
	
	// @Arthur, I had problems understanding your code and I saw it didn't work properly while testing, so I tried rewriting your idea from scratch,
	//  Most of it is still the same, but this version runs 100% accurate (after very rigurous testing), it took A LOT of tests...
	// - Pascal
	
	/*@Override
	public List<Move> getAllPossibleMoves(Pawn pawn) {
		// implement by Arthur brutforce method
		Set<Move> moves = new HashSet<Move>();
		for (BoardNode neighbour : pawn.getPosition().getNeighbours()){
			if(neighbour.isEmpty()) moves.add(new Move(pawn,neighbour));
		}
		recursiveMove(pawn.getPosition(),moves,pawn);
		return moves.stream().collect(Collectors.toList());
	}

	// full field a set with the possiblility
	private void recursiveMove(BoardNode place, Set<Move> movesfinale, Pawn pawn){
		List<BoardNode> moves = new ArrayList<BoardNode>();
		for (BoardNode neighbour : place.getNeighbours()){
			for (BoardNode neighbour2 : place.getNeighbours()) {
				if (neighbour2.isOccupied()) moves.add(neighbour2);
			}
		}
		for(BoardNode nextNode : place.getNeighbours()) {
			if(nextNode.getCurrentPawn() != null) {
				for(BoardNode nextNode2 : place.getNeighbours()){
					if(!moves.contains(nextNode2) && !movesfinale.contains(nextNode2)){
						movesfinale.add(new Move(pawn,nextNode2));
						recursiveMove(nextNode2,movesfinale,pawn);
						break;
					}
				}
			}
		}
	}*/
	
	public String toString() {
		return "Default Gamerules";
	}

	@Override
	public boolean hasWon(Board board, Player Player) {
		// TODO implement method
		return false;
	}

}
