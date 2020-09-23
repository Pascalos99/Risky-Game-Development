package gamerules;

import java.util.ArrayList;
import java.util.List;
import players.Player;

public class DefaultGameRules implements GameRules {

	@Override
	public boolean allowMove(Board board, Pawn pawn, BoardNode target) {
		// implement by Arthur
		if(target.getOwner()!=null) return false;
		if(target.getNeighbors().contains(pawn.getPosition())) return true;
		int sum = 0;
		BoardNode midleNode = null;
		for(BoardNode nextNode : target.getNeighbors()) {
			if (nextNode.getNeighbors().contains(target)){
				sum+=1;
				midleNode = nextNode;
			}
		}
		if(sum==1
			&& midleNode.getOwner()!=null)return true;
		return false;
	}

	@Override
	public List<Move> getAllPossibleMoves(Board board, Pawn pawn) {
		// implement by Arthur brutforce method
		List<Move> moves = new ArrayList();
		for(BoardNode node : board.getAllnodes()) {
			if(allowMove(board, pawn, node)) moves.add(new Move(pawn,node));
		}
		return moves;
	}

	@Override
	public boolean hasWon(Board board, Player Player) {
		// TODO implement method
		return false;
	}

}
