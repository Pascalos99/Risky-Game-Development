package test.java;


import org.junit.jupiter.api.Test;
import gamerules.*;
import players.*;
import players.bots.EvilPlayer;
import players.bots.NaivePlayer;
import static gamerules.GameRules.SELECTED_GAMERULES;
import static org.junit.jupiter.api.Assertions.*;

public class Gameloop {
    @Test
    void testLoopNaivePlayer(){
        Player pl = new NaivePlayer();
        Player pl2 = new NaivePlayer();
        Board game =new Board(GameRules.SELECTED_GAMERULES, null, pl, pl2);
        assertFalse(SELECTED_GAMERULES.hasWon(game, pl) || SELECTED_GAMERULES.hasWon(game, pl2));
        while (game.noWinners()){
            assertTrue(game.noWinners());
            game.nextPlayer();
            game.forceRequestMoveAndContinue();
        }
        assertTrue(SELECTED_GAMERULES.hasWon(game, game.currentPlayer()));
    }

    @Test
    void testLoopEvilPlayer(){
        Player pl = new EvilPlayer();
        Player pl2 = new EvilPlayer();
        Board game =new Board(GameRules.SELECTED_GAMERULES, null, pl, pl2);
        assertFalse(SELECTED_GAMERULES.hasWon(game, pl) || SELECTED_GAMERULES.hasWon(game, pl2));
        while (game.noWinners()){
            assertTrue(game.noWinners());
            game.nextPlayer();
            game.forceRequestMoveAndContinue();
        }
        assertTrue(SELECTED_GAMERULES.hasWon(game, pl) || SELECTED_GAMERULES.hasWon(game, pl2));
    }
}
