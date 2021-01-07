package test.java;

import org.junit.jupiter.api.Test;
import players.HumanPlayer;
import players.Player;
import players.bots.*;

import static org.junit.jupiter.api.Assertions.*;

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
        assertNotSame(pl, pl2);
    }

    @Test
    void testPlayerequal3(){
        Player pl = new HumanPlayer();
        Player pl2 = new RandomPlayer();
        assertNotSame(pl, pl2);
    }

    @Test
    void testPlayerequal4(){
        Player pl = new HumanPlayer();
        assertEquals(pl, pl);
    }

    @Test
    void testPlayerID1(){
        Player pl = new HumanPlayer();
        assertEquals(pl.getTypeName(),"Human Player");
    }
}