package game.events;

public enum Urgency {
	Note(0, "Note"), Warning(1, "Warning"), Error(2, "Error");
	
	public final int urgencyValue;
	public final String name;
	
	Urgency(int value, String name) {
		urgencyValue = value;
		this.name = name;
	}
	
	public String toString() {
		return "["+name+"]: ";
	}
}
