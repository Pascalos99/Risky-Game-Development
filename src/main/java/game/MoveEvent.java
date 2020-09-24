package game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import gamerules.Move;

/**
 * An event that declares the request for moves being displayed on the screen, only one MoveEvent is allowed to exist at the same time, creating a new MoveEvent while
 * another one is still in the queue will replace the old event
 */
public class MoveEvent extends GameEvent {

	private volatile List<Move> moves;
	
	public MoveEvent(Collection<Move> moves) {
		super(Urgency.Note, "display the given moves on the board");
		synchronized(this) {
			this.moves = new ArrayList<>(moves);
			List<MoveEvent> toRemove = new LinkedList<MoveEvent>();
			for (GameEvent e : GameEvent.gameEvents)
				if (e instanceof MoveEvent && e != this) toRemove.add((MoveEvent) e);
			for (MoveEvent e : toRemove) GameEvent.gameEvents.remove(e);
		}
	}
	
	public synchronized List<Move> getMoves() {
		return moves;
	}

}
