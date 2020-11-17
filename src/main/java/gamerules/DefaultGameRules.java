package gamerules;

import java.util.*;

import players.Player;

public class DefaultGameRules extends GameRules {

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
		List<BoardNode> neighbours = previous.getNeighbours();
		for (BoardNode neighbour : neighbours) {
			if (neighbour.isEmpty() && pawn.equals(previous.getCurrentPawn())) moves.add(new Move(pawn, neighbour));
			if (visited.contains(neighbour)) continue;
			visited.add(neighbour);
			if (neighbour.isOccupied()) {
				
				List<BoardNode> jumpingNodes = neighbour.getNeighbours();
				int direction = previous.getDirectionOf(neighbour);
				BoardNode jump_to = null;
				for (BoardNode jumpable : jumpingNodes)
					if (neighbour.getDirectionOf(jumpable) == direction) {
						jump_to = jumpable;
						break;
					}
				
				// now *jump_to* is the node that we want to jump to, if it's null, that means we are at the edge of the board and there is
				//  nowhere to jump to, in that case, we skip *neighbour* and go to the next node.
				
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
	

	//@Pascale I don't have find the mistake inside your methode However I think that my method doesn't contains this bug

	/*
	@Override
	public List<Move> getAllPossibleMoves(Pawn pawn) {
		List<Move> possibleMoves = new ArrayList<Move>();
		for (BoardNode node : pawn.getPosition().getNeighbours()) {
			if (node.isEmpty()) possibleMoves.add(new Move(pawn, node));
		}
		recursiveMove(pawn.getPosition(), possibleMoves, pawn);

		return possibleMoves;
	}
	// full field a set with the possiblility
	private void recursiveMove(BoardNode place, List<Move> movesfinale, Pawn pawn){
		BoardNode [] jumps = new BoardNode [place.getNeighbours().size()];
		List<BoardNode> two = new ArrayList<BoardNode>();
		int  y = 0;
		for(BoardNode node : place.getNeighbours()){
			jumps[y++] = node;
		}
		for(BoardNode node : place.getNeighbours()){
			for(BoardNode node2 : node.getNeighbours()){
				two.add(node2);
			}
		}
		List<Move> moves = new ArrayList<Move>();
		for (int i = 0; i <jumps.length; i++){
			if(jumps[i].isOccupied()){
				for(BoardNode node : jumps[i].getNeighbours()){
					if(contains(two,node)==1){
						if(!movesfinale.contains(new Move(pawn,node))){
							moves.add(new Move(pawn,node));
						}
					}
				}
			}
		}
		movesfinale.addAll(moves);
		for(Move m : moves){
			recursiveMove(m.target,movesfinale,pawn);
		}
	}

	public int contains (List<BoardNode> nodes, BoardNode target){
		if(target.isOccupied()) return 10;
		int total = 0;
		for(BoardNode node : nodes){
			if(node==target){
				total++;
			}
		}
		return total;
	}*/

	public String toString() {
		return "Default Gamerules";
	}

	@Override
	public boolean hasWon(Board board, Player player) {
		for (Pawn pawn : board.getAllPawnsOf(player))
			if (pawn.getPosition().getOwner() != player.getEnemy(board)) return false;
		return true;
		
		// used player.getEnemy instead of board.getGoal(player) as it is more efficient
		
		/*
		List<Pawn> allPawns= board.getAllPawnsOf(player);
		List<BoardNode> possiblePositions = board.getGoal(player);
		for (Pawn pawn : allPawns) {
			if(!possiblePositions.contains(pawn.getPosition())) return false;
		}
		return true; */
	}

	@Override
	public String getName() {
		return "default gamerules";
	}

}
