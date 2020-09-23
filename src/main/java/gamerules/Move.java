package gamerules;

public class Move {
	
	public Move(Pawn pawn, BoardNode target) {
		this.pawn = pawn;
		this.target = target;
	}
	
	public final Pawn pawn;
	public final BoardNode target;
	
	public boolean equals(Object o) {
		if (!(o instanceof Move)) return false;
		Move m = (Move)o;
		return m.pawn.equals(pawn) && m.target.equals(target);
	}
	
}
