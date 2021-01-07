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
        int numberTest = 16;
        HashMap<Player,Player> matches = new HashMap<>();
        StringBuilder results = new StringBuilder();
        List<Player> bots = new ArrayList<>();
        bots.add(new GreedyMST());
        bots.add(new NaivePlayer());
        bots.add(new EvilPlayer());
        bots.add(new AlphaBeta());
        results.append("\n" + "1 vs 1");
        for(Player player: bots){
            for(Player player2: bots){
                if(player != player2){
                    HashMap<String, Integer> result = new HashMap<>();
                    result.put(player.getTypeName(),0);
                    result.put(player2.getTypeName(),0);
                    for(int i=0; i <numberTest;i++){
                        String winner = simulate(player.getNewInstance(),player2.getNewInstance());
                        if(winner != null) result.replace(winner,result.get(winner)+1);
                        else i--;
                    }
                    results.append(transform(result, numberTest));
                }
                results.append("\n");
            }
        }
        print(results.toString());
        System.out.println(results);
        results.append("\n" + "2 vs 2");
        // test 4 players
        for(Player player: bots){
            for(Player player2: bots){
                if(player != player2){
                    HashMap<String, Integer> result = new HashMap<>();
                    result.put(player.getTypeName(),0);
                    result.put(player2.getTypeName(),0);
                    for(int i=0; i <numberTest;i++){
                        String winner = simulate(player.getNewInstance(),player2.getNewInstance(),player.getNewInstance(),player2.getNewInstance());
                        if(winner != null) result.replace(winner,result.get(winner)+1);
                        else i--;
                    }
                    results.append(transform(result, numberTest));
                }
                results.append("\n");
            }
        }
        System.out.println(results);
        print(results.toString());
        results.append("\n" + "3 vs 3");
        // test 4 players
        for(Player player: bots) {
            for (Player player2 : bots) {
                if (player != player2 ){
                    HashMap<String, Integer> result = new HashMap<>();
                    result.put(player.getTypeName(), 0);
                    result.put(player2.getTypeName(), 0);
                    for (int i = 0; i < numberTest; i++) {
                        String winner = simulate(player.getNewInstance(), player2.getNewInstance(), player.getNewInstance(), player2.getNewInstance(),player.getNewInstance(), player2.getNewInstance());
                        if (winner != null) result.replace(winner, result.get(winner) + 1);
                        else i--;
                    }
                    results.append(transform(result, numberTest));
                }
                results.append("\n");
            }
        }
        System.out.println(results);
        print(results.toString());
    }

    private static String simulate(Player...players){
        Board game =new Board(GameRules.SELECTED_GAMERULES, null, players);
        int index = 0;
        while (game.noWinners()){
            game.nextPlayer();
            game.forceRequestMoveAndContinue();
            if(index++ > 2000) return null;
        }
        return game.currentPlayer().getTypeName();
    }

    private static String transform(HashMap<String, Integer> result, int number){
        String end = "";
        for (String key : result.keySet()){
            end +="\n";
            end += key +" haswon " + ((double)result.get(key)/number)*100+"%";
        }
        return end;
    }

    private static void print(String result){
        try {
            FileWriter myWriter = new FileWriter("result.txt");
            myWriter.write(result);
            myWriter.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
}
