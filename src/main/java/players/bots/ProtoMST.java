package players.bots;

import gamerules.*;
import players.Player;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import static gamerules.GameRules.SELECTED_GAMERULES;

public class ProtoMST extends Player {

    @Override
    public Move returnMove(Board gameBoard){
        List<Pawn> pawns = gameBoard.getAllPawnsOf(this);
        List<Move> moves = new ArrayList<Move>();
        for(Pawn pawn : pawns){
            moves.addAll(SELECTED_GAMERULES.getAllPossibleMoves(pawn));
        }
        Collections.shuffle(moves);
        HashMap<Double,Move> eval = eval(moves,gameBoard);
        return eval.get((Collections.max(eval.keySet())));
    }

    private HashMap<Double,Move> eval(List<Move> moves,Board basegame){
        HashMap<Double,Move> eval = new HashMap<Double,Move>();
        for(Move m : moves){
            long t1 = System.currentTimeMillis();
            List<Player> players = new ArrayList<Player>();
            for(int i=0;i<basegame.getPlayers().size();i++){
                players.add(new NaivePlayer());
            }
            Player player = basegame.currentPlayer();
            GameState game = new GameState(basegame);
            game = new GameState(game,m);
            System.out.println("yes");
            RandomPlayer pls = new RandomPlayer();
            while ((3000/moves.size())>=System.currentTimeMillis()-t1){
                game = new GameState(game,pls.returnMove(game));
            }
            eval.put(game.currentScore(player),m);
        }
        return eval;
    }

    @Override
    public String getTypeName() {
        return "Proto1";
    }

    @Override
    public String getDescription() {
        return "Fuck other Player";
    }

    @Override
    public Player getNewInstance() {
        return new ProtoMST();
    }

    public String toString() {
        return getName()+" ("+getTypeName()+")";
    }
}
