package players;

import java.util.List;

import game.InputHandler;
import game.events.GameEvent;
import game.events.MoveEvent;
import gamerules.Board;
import gamerules.BoardNode;
import gamerules.Move;
import gamerules.Pawn;
import players.bots.DeterministicReturn;

import static gamerules.GameRules.SELECTED_GAMERULES;

public class HumanPlayer extends Player implements DeterministicReturn {

	public static InputHandler GLOBAL_INPUT;

	private static int human_count = 0;
	private int ID;

	public HumanPlayer() {
		ID = human_count++;
		setName("Human-"+ID);
	}

	@Override
    public Move returnMove(Board gameBoard){
		Pawn calculated_moves_for = null;
		List<Move> available_moves = null;
		GameEvent note = null;

		Pawn pawn = null;
		BoardNode endNode = null;
		boolean hasPlay = false;
		
		/*
		while(!hasPlay){
			if(Store.activateFromHuman){
				BoardNode node = Store.node;
				Store.activateFromHuman = false;
				if(pawn==null &&
					node.getCurrentPawn()!=null &&
					!node.isEmpty()&&
					node.getOwner().equals(this)){
					System.out.println("dd");
					pawn = node.getCurrentPawn();
		*/
		
		while (!hasPlay) {
			BoardNode select;
			while ((select = GLOBAL_INPUT.getSelectedNode()) == null);
			if ((pawn = select.getCurrentPawn()) == null || pawn.getOwner() != this) {
				GLOBAL_INPUT.setHighlighted(false);
				if (note == null) note = new GameEvent.Note("select a node of your color to continue");
			}
			else {
				note = null;
				if (calculated_moves_for != pawn) {
					available_moves = SELECTED_GAMERULES.getAllPossibleMoves(pawn);
					new MoveEvent(available_moves);
					calculated_moves_for = pawn;
				}
				GLOBAL_INPUT.setHighlighted(true);
				while (endNode == null) {
					BoardNode select2 = GLOBAL_INPUT.getSelectedNode();
					if (select2 == null || select2.isOccupied()) break;
					if (!available_moves.contains(new Move(pawn, select2))) {
						// move is not valid
						new GameEvent.Warning("selected move is not valid");
						GLOBAL_INPUT.setSelectedNode(select);
						GLOBAL_INPUT.setHighlighted(true);
					} else {
						// move is valid
						endNode = select2;
						hasPlay = true;
					}
				}
			}
			/* try {
				Thread.sleep(100);
			}catch (InterruptedException e){

			} */
		}

		GLOBAL_INPUT.setSelectedNode(null);

		if (pawn == null || endNode == null) return null;

		return new Move(pawn,endNode);
    }

	@Override
	public String getTypeName() {
		return "Human";
	}

	@Override
	public String getDescription() {
		return "Just a normal human being";
	}

    public String toString() {
    	return "Human-"+ ID;
    }

	@Override
	public Player getNewInstance() {
		return new HumanPlayer();
	}
}
