package players;

import gamerules.Board;
import gamerules.BoardNode;
import gamerules.Move;
import gamerules.Pawn;
import graphics.ExampleGame;
import graphics.Store;
import players.bots.DeterministicReturn;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collections;
import java.util.List;

import static gamerules.GameRules.SELECTED_GAMERULES;

public class HumanPlayer extends Player
	implements DeterministicReturn { // as of right now at least <-- remove this line when HumanPlayer is properly implemented

	private static int human_count = 0;
	private int ID;

	public HumanPlayer() {
		ID = ++human_count;
		setName("Human-"+ID);
	}
	
	@Override
    public Move returnMove(Board gameBoard){
		Pawn pawn = null;
		BoardNode endNode = null;
		Store.activate = true;
		boolean hasPlay = false;
		while(!hasPlay){
			if(Store.activateFromHuman){
				BoardNode node = Store.node;
				Store.activateFromHuman = false;
				if(pawn==null &&
						node.getCurrentPawn()!=null &&
						!node.isEmpty()&&
						node.getOwner().equals(this)){
					System.out.println("dd");
					pawn = node.getCurrentPawn();
				}
				else if (pawn != null ) {
					if(SELECTED_GAMERULES.allowMove(pawn,node)){
						endNode = node;
						hasPlay = true;
					}
					else if(pawn == node.getCurrentPawn()) pawn =null;
				}
			}
			try {
				Thread.sleep(100);
			}catch (InterruptedException e){

			}
		}
		Store.activate = false;
		Store.activateFromHuman = false;
		return new Move(pawn,endNode);
    }

	private void waitClick(){
		try {
			Thread.sleep(1000);
		}catch (InterruptedException e){

		}
	}

	@Override
	public String getTypeName() {
		return "Human";
	}

	@Override
	public String getDescription() {
		return "Just a normal human being";
	}
    
    public String toString() {
    	if (getName().matches("Human-.*")) return "Human-"+ ID;
    	else return super.toString();
    }
}