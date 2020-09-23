package gamerules;

import players.Player;

public class Pawn {
    private Player owner;
    private BoardNode position;
    
    /**
     * Move the pawn to the given position, returns false if the target is already occupied
     * Also sets the occupying pawn of this node's current position to null while setting the occupying pawn of the target node to {@code this}
     * @param target
     * @return
     */
    protected boolean setPosition(BoardNode target){
    	if (target.getCurrentPawn() != null) return false;
    	position.removePawn();
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
}
