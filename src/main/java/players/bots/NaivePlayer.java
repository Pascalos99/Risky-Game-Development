package players.bots;

import gamerules.Board;
import gamerules.BoardNode;
import gamerules.Move;
import gamerules.Pawn;
import players.Player;

import java.util.Collections;
import java.util.List;

import static gamerules.GameRules.SELECTED_GAMERULES;

public class NaivePlayer extends Player {
    private static int random_count = 0;
    private int ID;

    public NaivePlayer() {
        ID = ++random_count;
    }

    @Override
    public Move returnMove(Board gameBoard) {
        List<Pawn> pawns = gameBoard.getAllPawnsOf(this);
        for (int i = 0; i <pawns.size(); i++){
            Pawn pawn = pawns.get(i);
            if (pawn.getPosition().getOwner() == this.getEnemy(gameBoard)){
                pawns.remove(i);
                i--;
            }
        }
        Move move = null;
        int min = 14;
        Pawn pawn = pawns.get((int) (Math.random()*pawns.size()));
        while(SELECTED_GAMERULES.getAllPossibleMoves(pawn).size()<1){
            pawn = pawns.get((int) (Math.random()*pawns.size()));
        }
        List<Move> moves = SELECTED_GAMERULES.getAllPossibleMoves(pawn);
        for (Move move1 : moves) {
            int distance = distanceTarget(gameBoard, move1.target);
            if (distance <= min) {
                move = move1;
                min = distance;
            }
        }

        return move;
    }

    private int distanceTarget(Board board, BoardNode position){
        List<BoardNode> tagets = board.getGoal(this);
        if(tagets.contains(position)) return 0;
        Dijkstra dj = new Dijkstra();
        return (dj.getDistances(tagets.get((int)Math.random()*tagets.size()),position));
    }

    @Override
    public String getTypeName() {
        return "Naive bot";
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String toString(){
        return getTypeName() +ID;
    }
}
