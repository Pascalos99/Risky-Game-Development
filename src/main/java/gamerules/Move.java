package gamerules;

import players.Player;

public class Move {
	
	public Move(BoardNode start, BoardNode target) {
		start_node = (byte)start.getID();
		target_node = (byte)target.getID();
		pawn_owner = start.getCurrentPawn().getOwner();
		playerID = -1;
	}
	
	public Move(byte startID, byte targetID, int playerID) {
		start_node = startID;
		target_node = targetID;
		pawn_owner = null;
		this.playerID = playerID;
	}
	
	public Move(Pawn pawn, BoardNode target) {
		this(pawn.getPosition(), target);
	}
	
	public final int start_node;
	public final int target_node;
	
	private final Player pawn_owner;
	private final int playerID;
	
	public Player getOwner(Board board) {
		if (pawn_owner != null) return pawn_owner;
		return board.getPlayer(playerID);
	}
	
	/**
	 * @param board
	 * @return index of the player owner of this move's pawn
	 */
	public int getPlayerIndex(Board board) {
		if (pawn_owner != null) return board.getPlayerID(pawn_owner);
		return playerID;
	}
	
	/**
	 * @return index of the player owner of this move's pawn; but will return {@code -1}
	 *   if the constructor used was not {@link #Move(byte, byte, int)}
	 */
	protected int getPlayerIndex() {
		return playerID;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Move)) return false;
		Move m = (Move)o;
		return m.start_node == start_node && m.target_node == target_node && m.pawn_owner == pawn_owner;
	}

	/**
	 * @param board the board on which to check validity
	 * @return {@code true} if this move is valid in the specified board.
	 */
	public boolean isValid(Board board) {
		return GameRules.SELECTED_GAMERULES.allowMove(board, board.getNode(start_node).getCurrentPawn(), board.getNode(target_node));
	}
	
	/**
	 * This method assumes the move is valid and executes this move on the specified board.
	 * @param board some board to execute this on
	 * @return {@code true} if the move could be executed
	 */
	protected boolean execute(Board board) {
		return board.getNode(target_node).addPawn(board.getNode(start_node).getCurrentPawn());
	}
	
	/**
	 * This method assumes the reverse of this move is valid and executes this move in reverse on the specified board.
	 * @param board some board to execute this on
	 * @return {@code true} if the reverse move could be executed
	 */
	protected boolean reverse(Board board) {
		return board.getNode(start_node).addPawn(board.getNode(target_node).getCurrentPawn());
	}
	
	public double calculateScore(Board board) {
		return board.moveScore(this);
	}
	
	public String toString() {
		return start_node+"->"+target_node;
	}
	
	public BoardNode getStart(Board board) {
		return board.getNode(start_node);
	}
	public BoardNode getTarget(Board board) {
		return board.getNode(target_node);
	}
	/**
	 * Assumes this Move has not yet been executed and the starting position still contains the same pawn.
	 */
	public Pawn getPawn(Board board) {
		return board.getNode(start_node).getCurrentPawn();
	}
	
}
