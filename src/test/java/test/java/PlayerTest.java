package test.java;

import org.junit.jupiter.api.Test;
import players.HumanPlayer;
import players.Player;
import players.bots.RandomPlayer;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PlayerTest {
    @Test
    void testPlayerequal1(){
        Player pl = new HumanPlayer();
        assertEquals(true,pl==pl) ;
    }

    @Test
    void testPlayerequal2(){
        Player pl = new HumanPlayer();
        Player pl2 = new HumanPlayer();
        assertEquals(true,pl!=pl2) ;
    }

    @Test
    void testPlayerequal3(){
        Player pl = new HumanPlayer();
        Player pl2 = new RandomPlayer();
        assertEquals(true,pl!=pl2) ;
    }

    @Test
    void testPlayerID1(){
        Player pl = new HumanPlayer();
        assertEquals(pl.toString().equals("Human-1"),true);
    }

    @Test
    void testPlayerID2(){
        Player pl = new HumanPlayer();
        Player pl2 = new HumanPlayer();
        assertEquals(pl2.toString().equals("Human-2"),true);
    }

    @Test
    void testPlayerID3(){
        //TODO CORRECT THE BOT CLASS (IMPLEMENT THE TO STRING AND THE ID)
        Player pl = new HumanPlayer();
        Player pl2 = new RandomPlayer();
        assertEquals(pl2.toString().equals("Bot-2"),true);
    }
}