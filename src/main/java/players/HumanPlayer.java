package players;

import java.util.List;
import game.InputHandler;
import game.events.GameEvent;
import gamerules.Board;
import gamerules.BoardNode;
import gamerules.Move;
import gamerules.Pawn;
import players.bots.DeterministicReturn;

import static gamerules.GameRules.SELECTED_GAMERULES;

public class HumanPlayer extends Player implements DeterministicReturn {

	public static InputHandler GLOBAL_INPUT;

	@Override
    public Move returnMove(Board gameBoard){
		Pawn calculated_moves_for = null;
		List<Move> available_moves = null;

		Pawn pawn = null;
		BoardNode endNode = null;
		boolean hasPlay = false;
		
		BoardNode previousSelect = null, currentSelect = null;
		
		while (!hasPlay) {
			detect_change: {
				BoardNode select = GLOBAL_INPUT.getSelectedNode();
				if (select != currentSelect) {
					previousSelect = currentSelect;
					currentSelect = select;
				} else break detect_change;
				
				if (currentSelect == null || previousSelect == null || currentSelect.isOccupied() || previousSelect.isEmpty()) break detect_change;
				
				Pawn selected_pawn = previousSelect.getCurrentPawn();
				BoardNode selected_node = currentSelect;
				if (calculated_moves_for != selected_pawn) {
					available_moves = SELECTED_GAMERULES.getAllPossibleMoves(gameBoard, selected_pawn);
					calculated_moves_for = selected_pawn;
				}
				if (!available_moves.contains(new Move(selected_pawn, selected_node))) {
					new GameEvent.Warning("selected move is not valid");
					break detect_change;
				}
				pawn = selected_pawn;
				endNode = selected_node;
				hasPlay = true;
			}
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		GLOBAL_INPUT.setSelectedNode(null);

		if (pawn == null || endNode == null) return null;

		return new Move(pawn,endNode);
    }

	@Override
	public String getTypeName() {
		return "Human Player";
	}

	@Override
	public String getDescription() {
		return "Just a normal human being";
	}

	public String toString() {
		return getName()+" ("+getTypeName()+")";
	}

	@Override
	public Player getNewInstance() {
		return new HumanPlayer();
	}
}
