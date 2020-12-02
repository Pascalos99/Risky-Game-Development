package players.bots;

import gamerules.Board;
import gamerules.GameRules;
import players.Player;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TestBot {
    public static void main(String[] args) {
        initializeTest();
    }

    private static void initializeTest(){
        int numberTest = 10;
        HashMap<Player,Player> matches = new HashMap<>();
        List<String> results = new ArrayList<>();
        List<Player> bots = new ArrayList<>();
        bots.add(new GreedyMST());
        bots.add(new NaivePlayer());
        bots.add(new EvilPlayer());
        bots.add(new AlphaBeta());
        //test 2 players
        for(Player player: bots){
            for(Player player2: bots){
                if(player != player2 && matches.get(player) != player2){
                    matches.put(player, player2);
                    matches.put(player2, player);
                    HashMap<String, Integer> result = new HashMap<>();
                    result.put(player.getTypeName(),0);
                    result.put(player2.getTypeName(),0);
                    for(int i=0; i <numberTest;i++){
                        String winner = simulate(new Player[]{player.getNewInstance(),player2.getNewInstance()});
                        if(winner != null) result.replace(winner,result.get(winner)+1);
                        else i--;
                    }
                    results.add(result.toString());
                }
            }
        }
        // test 4 players
        for(Player player: bots){
            for(Player player2: bots){
                if(player != player2 && matches.get(player) != player2){
                    matches.put(player, player2);
                    matches.put(player2, player);
                    HashMap<String, Integer> result = new HashMap<>();
                    result.put(player.getTypeName(),0);
                    result.put(player2.getTypeName(),0);
                    for(int i=0; i <numberTest;i++){
                        String winner = simulate(new Player[]{player.getNewInstance(),player2.getNewInstance(),player.getNewInstance(),player2.getNewInstance()});
                        if(winner != null) result.replace(winner,result.get(winner)+1);
                        else i--;
                    }
                    results.add(result.toString());
                }
            }
        }

        // test 4 players
        for(Player player: bots) {
            for (Player player2 : bots) {
                if (player != player2 && matches.get(player) != player2) {
                    matches.put(player, player2);
                    matches.put(player2, player);
                    HashMap<String, Integer> result = new HashMap<>();
                    result.put(player.getTypeName(), 0);
                    result.put(player2.getTypeName(), 0);
                    for (int i = 0; i < numberTest; i++) {
                        String winner = simulate(new Player[]{player.getNewInstance(), player2.getNewInstance(), player.getNewInstance(), player2.getNewInstance(),player.getNewInstance(), player2.getNewInstance()});
                        if (winner != null) result.replace(winner, result.get(winner) + 1);
                        else i--;
                    }
                    results.add(result.toString());
                }
            }
        }
        print(results);
    }

    private static String simulate(Player...players){
        Board game =new Board(GameRules.SELECTED_GAMERULES, null, players);
        int index = 0;
        while (game.noWinners()){
            game.nextPlayer();
            game.forceRequestMoveAndContinue();
            if(index++>200) return null;
        }
        return game.currentPlayer().getTypeName();
    }

    private static void print(List<String> result){
        try {
            FileWriter myWriter = new FileWriter("result.txt");
            for(String s: result){
                myWriter.write(s);
                myWriter.write("\n");
            }
            myWriter.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
}
