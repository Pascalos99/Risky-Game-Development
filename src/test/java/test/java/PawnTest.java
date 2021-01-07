package test.java;

import org.junit.jupiter.api.Test;
import gamerules.*;
import players.*;

import static org.junit.jupiter.api.Assertions.*;

public class PawnTest {
    private Player playerExample1 = new HumanPlayer();
    private Player playerExample2 = new HumanPlayer();
    private BoardNode node1 = new BoardNode(1,null,null);
    private Pawn p1 = new Pawn(playerExample1,null);
    private Pawn p2 = new Pawn(playerExample1,node1);


    @Test
    void testequal(){
        assertNotSame(p1, p2);
        assertTrue(p1 == p1);
    }

    @Test
    void testPosition1(){
        assertNull(p1.getPosition());
        assertEquals(p2.getPosition(),node1);
    }
    @Test
    void testTostring(){
        assertEquals(p1.toString(),"(Pawn of  (Human Player))");
    }
}
