package gamerules;

public class Move {
	
	public Move(Pawn pawn, BoardNode target) {
		this.pawn = pawn;
		this.target = target;
	}
	
	public final Pawn pawn;
	public final BoardNode target;

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Move)) return false;
		Move m = (Move)o;
		return m.pawn.equals(pawn) && m.target.equals(target);
	}
	
	public boolean isValid() {
		return GameRules.SELECTED_GAMERULES.allowMove(pawn, target);
	}
	
	/**
	 * This method assumes the move is valid
	 * @return {@code true} if the move could be executed
	 */
	protected boolean execute() {
		return target.addPawn(pawn);
	}
	
	public String toString() {
		return pawn.getPosition().getID()+"->"+target.getID();
	}
	
}
