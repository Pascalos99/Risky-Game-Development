package players.bots;

import java.util.List;
import java.util.function.Predicate;

import gamerules.Board;
import gamerules.GameState;
import gamerules.GameTree;
import gamerules.GameTreeNode;
import gamerules.Move;
import players.Player;

public class LBFSPlayer extends Player implements DeterministicReturn {

	long limit_in_ms;
	
	public LBFSPlayer(long limit_in_ms) {
		this.limit_in_ms = limit_in_ms;
	}
	
	@Override
	public Move returnMove(Board gameBoard) {
		GameState root = new GameState(gameBoard);
		GameTree tree = new GameTree(root);
		long start = System.currentTimeMillis();
		tree.setMaxExpansionTime(limit_in_ms);
		while (!tree.limitReached()) {
			tree.expandDeepest(tree.preFilterBarrierEval(0.6, true, null));
		}
		GameTreeNode best = tree.getHighestEval(this);
		System.out.println("after "+(System.currentTimeMillis()-start)+" ms; tree achieved a depth of "+tree.maxDepth()+
				" and found a best move with value "+tree.evaluate(best));
		
		List<Move> sequence = best.getGameState().getMoveSequence();
		if (sequence.size() == 0) return null;
		return sequence.get(0);
	}

	@Override
	public String getTypeName() {
		return "Limited Breadth First Search";
	}

	@Override
	public String getDescription() {
		return "Uses Time-Limited Breadth First Search to iterate through the GameTree based on the default heuristic while only "
				+"expanding moves that have a top-60% evaluation value";
	}

	@Override
	public Player getNewInstance() {
		return new LBFSPlayer(limit_in_ms);
	}
	
	public String toString() {
		return "LBFS[l="+limit_in_ms+"]";
	}
	
}
