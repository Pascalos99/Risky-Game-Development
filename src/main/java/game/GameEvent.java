package game;

import java.util.PriorityQueue;

public abstract class GameEvent implements Comparable<GameEvent> {

	public static volatile PriorityQueue<GameEvent> gameEvents = new PriorityQueue<>();
	
	/**
	 * @return {@code true} if there are events to be handled
	 */
	public static boolean hasPending() {
		return gameEvents.size() > 0;
	}
	
	/**
	 * Gets the next event to be declared and declares it
	 * @return the event with highest priority, or {@code null} if there are no events pending
	 */
	public static GameEvent getNext() {
		return gameEvents.poll();
	}
	
	private String message;
	private Urgency urgency;
	
	private long time_of_creation;
	
	/**
	 * creates a new event with the given name and a timestamp. This event is automatically added to the {@link #gameEvents} queue.
	 * @param urgency the urgency of the event (determines order in the queue and gives extra information to the receiver)
	 * @param message a message that explains the meaning of this event
	 */
	public GameEvent(Urgency urgency, String message) {
		this.message = message;
		this.urgency = urgency;
		time_of_creation = System.currentTimeMillis();
		gameEvents.add(this);
	}
	
	public GameEvent(String message) {
		this(Urgency.Note, message);
	}
	
	public Urgency getUrgency() {
		return urgency;
	}
	
	public int compareTo(GameEvent e) {
		if (e == null) return -1;
		if (urgency == e.urgency) return (int) (time_of_creation - e.time_of_creation);
		return e.urgency.urgencyValue - urgency.urgencyValue;
	}
	
	public String toString() {
		return urgency+message;
	}
	
	public String getMessage() {
		return message;
	}
	
	/**
	 * This is only necessary if the gameEvent object was not acquired through {@link #getNext()}
	 * @return {@code false} if this event has already been handled
	 */
	public boolean declare() {
		return gameEvents.remove(this);
	}
	
	/**
	 * re-adds an already declared event to the event queue for it to be re-evaluated.
	 * @return {@code false} if the event wasn't declared when this method was called
	 */
	public boolean reinstall() {
		return gameEvents.add(this);
	}

	/**
	 * This {@link GameEvent} prompts the action of the given message being displayed on-screen somewhere [low-priority]
	 */
	public static class Note extends GameEvent {

		public Note(String message) {
			super(message);
		}
	}
	
	/**
	 * This {@link GameEvent} prompts the action of the given message being displayed on-screen somewhere [mid-priority]
	 */
	public static class Warning extends GameEvent {

		public Warning(String message) {
			super(Urgency.Warning, message);
		}
	}
	
	/**
	 * This {@link GameEvent} prompts the action of the given message being displayed on-screen somewhere [high-priority]
	 */
	public static class Error extends GameEvent {

		public Error(String message) {
			super(Urgency.Error, message);
		}
	}
	
	
}
