package gamerules;

import graphics.BoardGraphics;
import players.Player;
import java.util.*;

public class Board {

    private List<BoardNode> nodes;
    private GameRules gamerules;
    private Player [] players;
    private BoardGraphics graphics;

    Board(List<BoardNode> nodes, GameRules gamerules, Player [] players,BoardGraphics graphics) {
        this.nodes = nodes;
        this.gamerules = gamerules;
        this.players = players;
        this.graphics = graphics;
    }

    //TODO implement the method
    public int [] getIntegerRep(){
        return null;
    }

    public List<BoardNode> getAllnodes() {
        return Collections.unmodifiableList(nodes);
    }

    //TODO implement the method
    public boolean hasTurn(Player player){
        return false;
    }

    public Player[] getPlayers() {
        return players;
    }
}
