package game;

import java.util.function.Function;

public abstract class GameEvent implements Comparable<GameEvent> {

	private volatile GameEvent nextEvent = null;
	private volatile GameEvent prevEvent = null;
	
	private volatile boolean isInList = true;
	
	private static volatile GameEvent root = new GameEvent(false) {};
	
	private static volatile GameEvent head = new GameEvent(false) {};
	
	static {
		root.nextEvent = null;
		root.prevEvent = head;
		head.nextEvent = root;
		head.prevEvent = null;
	}
	
	/**
	 * @return {@code true} if there are events to be handled
	 */
	public synchronized static boolean hasPending() {
		return head.nextEvent != root;
	}
	
	/**
	 * Gets the next event to be declared and declares it
	 * @return the event with highest priority, or {@code null} if there are no events pending
	 */
	public synchronized static GameEvent getNext() {
		if (!hasPending()) return null;
		GameEvent next = head.nextEvent;
		head.nextEvent = next.nextEvent;
		next.nextEvent.prevEvent = head;
		next.isInList = false;
		return next;
	}
	
	private String message;
	private Urgency urgency;
	
	private long time_of_creation;
	
	private GameEvent(boolean isNormal) {
		if (isNormal) {
			synchronized(this) {
				this.nextEvent = root;
				this.prevEvent = root.prevEvent;
				root.prevEvent.nextEvent = this;
				root.prevEvent = this;
			}
		} else {
			urgency = Urgency.Error;
			message = "this is a service node of GameEvent, if you got this from nextEvent(), something went terribly wrong";
		}
	}
	
	/**
	 * creates a new event with the given name and a timestamp. This event is automatically added to the {@link #gameEvents} queue.
	 * @param urgency the urgency of the event (determines order in the queue and gives extra information to the receiver)
	 * @param message a message that explains the meaning of this event
	 */
	public GameEvent(Urgency urgency, String message) {
		this(true);
		this.message = message;
		this.urgency = urgency;
		time_of_creation = System.currentTimeMillis();
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
	 * @param selection a function that returns {@code true} for events to be deleted
	 */
	public static synchronized void deleteEvents(Function<GameEvent, Boolean> selection) {
		GameEvent current = head;
		while ((current = current.nextEvent) != root) {
			if (selection.apply(current)) {
				// we need to delete this event
				GameEvent previous = current.prevEvent;
				GameEvent next = current.nextEvent;
				previous.nextEvent = next;
				next.prevEvent = previous;
				current.isInList = false;
			}
		}
	}
	
	/**
	 * This is only necessary if the gameEvent object was not acquired through {@link #getNext()}
	 * @return {@code false} if this event has already been handled
	 */
	public boolean declare() {
		if (!isInList) return false;
		GameEvent previous = prevEvent;
		GameEvent next = nextEvent;
		previous.nextEvent = next;
		next.prevEvent = previous;
		isInList = false;
		return true;
	}
	
	/**
	 * re-adds an already declared event to the event queue for it to be re-evaluated.
	 * @return {@code false} if the event wasn't declared when this method was called
	 */
	public boolean reinstall() {
		if (isInList) return false;
		this.nextEvent = root;
		this.prevEvent = root.prevEvent;
		root.prevEvent.nextEvent = this;
		root.prevEvent = this;
		isInList = true;
		return true;
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
