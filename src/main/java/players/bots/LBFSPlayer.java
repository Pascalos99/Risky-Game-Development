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
	double barrier;
	
	public LBFSPlayer() {
		this(500,0.8);
	}
	
	public LBFSPlayer(long limit_in_ms, double barrier) {
		this.limit_in_ms = limit_in_ms;
		this.barrier = barrier;
	}
	
	@Override
	public Move returnMove(Board gameBoard) {
		GameState root = new GameState(gameBoard);
		GameTree tree = new GameTree(root);
		long start = System.currentTimeMillis();
		tree.setMaxExpansionTime(limit_in_ms);
		while (!tree.limitReached()) {
			tree.expandDeepest(tree.preFilterBarrierEval(barrier, true, null));
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
				+String.format("expanding moves that have a top-%.1f%% evaluation value", barrier*100);
	}

	@Override
	public Player getNewInstance() {
		return new LBFSPlayer(limit_in_ms, barrier);
	}
	
	public String toString() {
		return "LBFS[l="+limit_in_ms+"]";
	}
	
}
