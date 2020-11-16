package players.bots;

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
		while (System.currentTimeMillis() - start < limit_in_ms) {
			tree.expandDeepest();
			// TODO
		}
		GameTreeNode best = tree.getHighestEval(this);
		System.out.println("after "+(System.currentTimeMillis()-start)+" ms; tree achieved a depth of "+tree.maxDepth()+
				" and found a best move with value "+tree.evaluate(best));
		return best.getGameState().getMoveSequence().get(0);
	}

	@Override
	public String getTypeName() {
		return "Limited Breadth First Search";
	}

	@Override
	public String getDescription() {
		return "Uses Limited Breadth First Search to iterate through the GameTree based on the default heuristic";
	}

	@Override
	public Player getNewInstance() {
		return new LBFSPlayer(limit_in_ms);
	}
	
}
