package gamerules;

import java.util.*;

import players.Player;

public class DefaultGameRules extends GameRules {

	@Override
	public boolean allowMove(Pawn pawn, BoardNode target) {
		if (pawn.getPosition().getNeighbours().contains(target)) return true;
		return (getAllPossibleMoves(pawn).contains(new Move(pawn, target)));
	}
	/*
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
				
				// this part is for jumping; here we are jumping from *previous* over *neighbour* and seek to find a valid node for *jump_to*
				
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
	*/

	//@Pascale I don't have find the mistake inside your methode However I think that my method doesn't contains this bug

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
	}

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

}
