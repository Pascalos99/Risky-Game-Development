package test.java;

import gamerules.*;
import org.junit.jupiter.api.Test;
import players.Player;
import players.bots.NaivePlayer;

import java.util.*;

import static gamerules.GameRules.SELECTED_GAMERULES;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class RulesTest {

    @Test
    void testDefaultRules(){
        Player pl = new NaivePlayer();
        Player pl2 = new NaivePlayer();
        Board game =new Board(GameRules.SELECTED_GAMERULES, null, pl, pl2);
        while (game.noWinners()){
            game.nextPlayer();
            List<Move> moves = new ArrayList<Move>();
            List<Move> movesrules = new ArrayList<Move>();
            for(Pawn pawn :game.getAllPawnsOf(game.currentPlayer())){
                moves.addAll(getAllPossibleMoves(pawn));
                movesrules.addAll(SELECTED_GAMERULES.getAllPossibleMoves(pawn));
            }
            for(Move move :movesrules){
                assertEquals(moves.contains(move),true);
            }
            game.forceRequestMoveAndContinue();
        }
    }

    //Methode Who return all allow moves. Comes from old version of the code
    List<Move> getAllPossibleMoves(Pawn pawn) {
        List<Move> possibleMoves = new ArrayList<Move>();
        for (BoardNode node : pawn.getPosition().getNeighbours()) {
            if (node.isEmpty()) possibleMoves.add(new Move(pawn, node));
        }
        recursiveMove(pawn.getPosition(), possibleMoves, pawn);

        return possibleMoves;
    }
    // full field a set with the possiblility
    void recursiveMove(BoardNode place, List<Move> movesfinale, Pawn pawn){
        BoardNode [] jumps = new BoardNode [place.getNeighbours().size()];
        List<BoardNode> two = new ArrayList<BoardNode>();
        int  y = 0;
        for(BoardNode node : place.getNeighbours()){
            jumps[y++] = node;
        }
        for(BoardNode node : place.getNeighbours()){
            for(BoardNode node2 : node.getNeighbours()){
                two.add(node2);
            }
        }
        List<Move> moves = new ArrayList<Move>();
        for (int i = 0; i <jumps.length; i++){
            if(jumps[i].isOccupied()){
                for(BoardNode node : jumps[i].getNeighbours()){
                    if(contains(two,node)==1){
                        if(!movesfinale.contains(new Move(pawn,node))){
                            moves.add(new Move(pawn,node));
                        }
                    }
                }
            }
        }
        movesfinale.addAll(moves);
        for(Move m : moves){
            recursiveMove(m.target, movesfinale, pawn);
        }
    }

    int contains (List<BoardNode> nodes, BoardNode target){
        if(target.isOccupied()) return 10;
        int total = 0;
        for(BoardNode node : nodes){
            if(node==target){
                total++;
            }
        }
        return total;
    }
}
