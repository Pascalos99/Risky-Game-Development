package game;

public enum Urgency {
	Note(0), Warning(1), Error(2);
	
	public final int urgencyValue;
	
	Urgency(int value) {
		urgencyValue = value;
	}
}
