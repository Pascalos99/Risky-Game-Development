package players.bots;

import gamerules.*;
import gamerules.evaluation_functions.SimpleGoalDistance;
import players.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import static gamerules.GameRules.SELECTED_GAMERULES;

public class NaivePlayer extends Player {

    EvaluationFunction evaluation;

    public NaivePlayer(){
        this(new SimpleGoalDistance());
    }

    public NaivePlayer(EvaluationFunction evaluation){
        this.evaluation = evaluation;
    }

    @Override
    public Move returnMove(Board gameBoard) {
        GameState gameState = new GameState(gameBoard);
        HashMap<Double,Move> eval= new HashMap<>();
        List<Pawn> pawns = gameBoard.getAllPawnsOf(this);
        Collections.shuffle(pawns);
        for (Pawn pawn : pawns) {
            List<Move> moves = SELECTED_GAMERULES.getAllPossibleMoves(pawn);
            for (Move move : moves) {
                double score = evaluation.apply(new GameState(gameState, move),this);
                if(eval.containsKey(score)){
                   score += Math.random() - 0.5;
                }
                eval.put(score, move);
            }
        }
        return eval.get((Collections.max(eval.keySet())));
    }

    @Override
    public String getTypeName() {
        return "Naive Player";
    }

    @Override
    public String getDescription() {
        return "A bot that take randomly a pawn and find the move who will get him closer to the goal";
    }

	@Override
	public Player getNewInstance() {
		return new NaivePlayer();
	}
	
	public String toString() {
		return getName()+" ("+getTypeName()+")";
	}
}
