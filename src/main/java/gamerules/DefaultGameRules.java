package gamerules;

import java.util.*;
import java.util.stream.Collectors;

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
	
	public List<Move> getAllPossibleMoves(byte[] integer_rep, int pawn_pos_ID) {
		byte start = (byte) pawn_pos_ID;
		int pawns = GameState.PLAYER_PAWNS;
		int playerCount = integer_rep.length / pawns;
		int player;
		test: {
			for (int i=0; i < playerCount; i++) 
				if (Arrays.binarySearch(integer_rep, i*pawns, (i+1)*pawns, start) > 0) {
					player = i; break test;
				}
			throw new RuntimeException("given pawn position does not contain any pawn");
		}
		byte[] occupiedNodes = Arrays.copyOf(integer_rep, integer_rep.length);
		Arrays.sort(occupiedNodes);
		HashSet<Byte> visitedNodes = new HashSet<>();
		visitedNodes.add(start);
		// TODO complete method
		return visitedNodes.stream().map(target -> new Move(start, target, player)).collect(Collectors.toList());
	}

	public String toString() {
		return "Default Gamerules";
	}

	@Override
	public boolean hasWon(Board board, Player player) {
		for (Pawn pawn : board.getAllPawnsOf(player))
			if (pawn.getPosition().getOwner() != player.getEnemy(board)) return false;
		return true;
	}

	@Override
	public String getName() {
		return "default gamerules";
	}

}
