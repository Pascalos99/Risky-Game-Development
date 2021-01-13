package players.bots;

import gamerules.*;
import gamerules.evaluation_functions.NormalizedSGD;
import gamerules.evaluation_functions.SimpleGoalDistance;
import players.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import static gamerules.GameRules.SELECTED_GAMERULES;

public class NaivePlayer extends Player {

    EvaluationFunction evaluation;

    public NaivePlayer(){
        this(new NormalizedSGD());
    }

    public NaivePlayer(EvaluationFunction evaluation){
        this.evaluation = evaluation;
    }
    
    private Move last_move = null;

    @Override
    public Move returnMove(Board gameBoard) {
        GameState gameState = new GameState(gameBoard);
        HashMap<Double,Move> eval= new HashMap<>();
        List<Pawn> pawns = gameBoard.getAllPawnsOf(this);
        List<Integer> blackList = this.getOtherPlayersBase(gameBoard);
        Collections.shuffle(pawns);
        for (Pawn pawn : pawns) {
            List<Move> moves = SELECTED_GAMERULES.getAllPossibleMoves(gameBoard, pawn);
            for (Move move : moves) {
                if(!blackList.contains(move.target_node)){
                    double score = evaluation.apply(new GameState(gameState, move),this);
//                    score += Math.random()*0.001;
                    eval.put(score, move);
                }
            }
        }
        Double max = Collections.max(eval.keySet());
        Move move = eval.get(max);
        if (last_move != null && move.equals(last_move.reverse())) {
        	eval.remove(max);
        	move = eval.get(Collections.max(eval.keySet()));
        }
        last_move = move;
        return move;
    }

    @Override
    public String getTypeName() {
        return "Naive Player ["+evaluation+"]";
    }

    @Override
    public String getDescription() {
        return "A bot that take randomly a pawn and find the move who will get him closer to the goal";
    }

	@Override
	public Player getNewInstance() {
		return new NaivePlayer(evaluation);
	}
	
	public String toString() {
		return getName()+" ("+getTypeName()+")";
	}
}
