package gamerules;

import players.Player;

public class Pawn {
    
	private Player owner;
    private BoardNode position;
    
    /**
     * 
     * @param owner owner of this pawn, may not be null
     * @param position position of this pawn, can temporarily be null
     */
    public Pawn(Player owner, BoardNode position) {
    	if (owner == null) throw new AssertionError("Pawn owner may not be null");
    	this.owner = owner;
    	this.position = position;
    }
    
    /**
     * Move the pawn to the given position, returns false if the target is already occupied
     * Also sets the occupying pawn of this node's current position to null while setting the occupying pawn of the target node to {@code this}
     * @param target
     * @return {@code true} if the state of this Pawn changed as a result of this call
     */
    protected boolean setPosition(BoardNode target){
    	if (target.isOccupied() && target.getCurrentPawn() != this) return false;
    	if (position != null) position.removePawn();
    	position = target;
    	position.addPawn(this);
    	return true;
    }

    public Player getOwner() {
        return owner;
    }

    public BoardNode getPosition() {
        return position;
    }
    
    public String toString() {
    	String s = "(Pawn of "+owner;
    	if (position == null) return s + ")";
    	else return s + " at "+position.getID()+")";
    }
}
