package main.java.gamerules;

public class Move {
	
	public Move(Pawn pawn, BoardNode target) {
		this.pawn = pawn;
		this.target = target;
	}
	
	public final Pawn pawn;
	public final BoardNode target;
	
}
