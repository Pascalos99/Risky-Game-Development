package test.java;

import org.junit.jupiter.api.Test;
import players.HumanPlayer;
import players.Player;
import players.bots.*;

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
    void testPlayerequal4(){
        Player pl = new HumanPlayer();
        assertEquals(true,pl.equals(pl)) ;
    }

    @Test
    void testPlayerID1(){
        Player pl = new HumanPlayer();
        assertEquals(pl.getTypeName(),"Human Player");
    }
}