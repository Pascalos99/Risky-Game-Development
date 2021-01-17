package game.tests;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import game.GameSetup;
import gamerules.Board;
import gamerules.GameState;
import gamerules.evaluation_functions.NeuralNetworkEval;
import gamerules.evaluation_functions.NormalizedSGD;
import gamerules.evaluation_functions.PaperEval;
import graphics.sample.AssetFinder;
import players.Player;
import players.bots.AlphaBeta;
import players.bots.DeterministicReturn;
import players.bots.EvilPlayer;
import players.bots.GreedyMST;
import players.bots.NaivePlayer;
import players.bots.utils.DataLoader;
import players.bots.utils.DataLoader.Data;
import players.bots.utils.DataLoader.DataPoint;

import static players.bots.utils.EveryoneShouldHaveMachineLearning.loadNetwork;

public class BotTester {
	
	public static final String testing_path = AssetFinder.assetsPath+"testing_data"+File.separator;
	
	public static void main(String[] args) {
		BotTester test = new BotTester(70, 5, 1000);
		Player[] players = {
				new AlphaBeta(),
				//new AlphaBeta(new PaperEval()),
				new AlphaBeta(new NeuralNetworkEval(loadNetwork("DL-working test"))),
				new GreedyMST(),
				//new GreedyMST(new PaperEval()),
				new GreedyMST(new NeuralNetworkEval(loadNetwork("DL-working test"))),
				new NaivePlayer(),
				new NaivePlayer(new PaperEval()),
				new NaivePlayer(new NeuralNetworkEval(loadNetwork("DL-working test"))),
				new EvilPlayer()
		};
		test.runFullCrossOverTesting(true, true, true, players);
	}

	private static final Object waitingObject = new Object();
    private static final Object threadWait = new Object();
    private int tests_completed;
    private int number_of_tests;
    private static final int LOGICAL_THREADS = Runtime.getRuntime().availableProcessors();
    private int current_threads = 0;

    private List<Integer> occupied;
    
    public static String store_data = "complete_testing";
    private int max_turn_count;
    private int test_nr_stoch;
    private int test_nr_deter;
    
    public BotTester(int test_number_stochastic, int test_number_deterministic, int max_turn_count) {
    	this.max_turn_count = max_turn_count;
    	test_nr_stoch = test_number_stochastic;
    	test_nr_deter = test_number_deterministic;
    }

    public void runTestingGames(Player[]... games_players) {
    	long startTime = System.nanoTime();
        occupied = new ArrayList<>();
        number_of_tests = games_players.length;
        for(int game=0; game < games_players.length; game++){
        	int id = game;
        	for (int i=0; i < games_players.length; i++)
        		if (!occupied.contains(i)) {
        			id = i;
        			break;
        		}
            TestThread gameThread = new TestThread(id, games_players[game], max_turn_count, store_data);
            occupied.add(id);
            System.out.println("running threads: "+occupied);
            gameThread.start();
            current_threads++;
            if(current_threads==LOGICAL_THREADS) {
                synchronized (threadWait) {
                    try {
                        threadWait.wait();
                    } catch (InterruptedException e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
        }

        synchronized (waitingObject){
            try{
                waitingObject.wait();
            }
            catch(InterruptedException e){
                System.out.println(e.getMessage());
            }
        }
        long finishTime = System.nanoTime();
        System.out.println("Time taken:"+(double) (finishTime-startTime)/1e9+" s");
    }

    public void runFullCrossOverTesting(boolean _1v1, boolean _2v2, boolean _3v3, Player... players) {
    	
    	class PlayerMatch {
    		public Player p1, p2;
    		public PlayerMatch(Player _p1, Player _p2) {
    			p1 = _p1; p2 = _p2;
    		}
    		@Override
    		public boolean equals(Object o) {
    			if (!(o instanceof PlayerMatch)) return false;
    			PlayerMatch a = (PlayerMatch) o;
    			return a.p1 == p1 && a.p2 == p2;
    		}
    	}
    	
    	List<Player[]> gamePlayers = new ArrayList<>();
    	List<PlayerMatch> matches = new ArrayList<>();
    	List<Integer> match_counts = new ArrayList<>();
    	//Map<PlayerMatch, Integer> match_counts = new HashMap<>();
    	boolean match_made = true;
    	while (match_made) {
    		match_made = false;
    		for (int pc = 1; pc <= 3; pc ++) {
    			if (!_1v1 && pc == 1) continue;
    			if (!_2v2 && pc == 2) continue;
    			if (!_3v3 && pc == 3) continue;
		    	for (Player p1 : players) {
		    		for (Player p2 : players) {
		    			if (p1 == p2) continue;
		    			PlayerMatch pm = new PlayerMatch(p1, p2);
		    			if (matches.indexOf(pm) < 0) {
		    				matches.add(pm);
		    				match_counts.add(0);
		    			}
		    			Integer count = match_counts.get(matches.indexOf(pm));
		    			if (count == null) count = 0;
		    			int limit = test_nr_stoch;
		    			if ((p1 instanceof DeterministicReturn) && (p2 instanceof DeterministicReturn))
		    				limit = test_nr_deter;
		    			if (count < limit) {
		    				System.out.println("setting up match "+p1.getTypeName()+" vs "+p2.getTypeName()+" "+(count+2));
		    				gamePlayers.add(getXvX(p1, p2, pc));
		    				match_counts.set(matches.indexOf(pm), count + 2);
		    				match_made = true;
		    			}}}}}
    	runTestingGames(gamePlayers.toArray(new Player[gamePlayers.size()][]));
    }

    private Player[] getXvX(Player p1, Player p2, int x) {
    	return switch (x) {
        	case 1 -> get1v1(p1, p2);
        	case 2 -> get2v2(p1, p2);
        	case 3 -> get3v3(p1, p2);
        	default -> null;
    	};
    }
    private Player[] get1v1(Player p1, Player p2){
    	return new Player[]
    		{ p1, p2 };}
    private Player[] get2v2(Player p1, Player p2){
    	return new Player[]
    		{ p1, p2, p1, p2 };}
    private Player[] get3v3(Player p1, Player p2){
    	return new Player[]
    		{ p1, p2, p1, p2, p1, p2 };}
    
    public class TestThread extends Thread {
        
        private Player[] players;
        private int[] wins;
        private int ties = 0;
        protected int iterations;

    	public TestThread(int state_id, Player[] players, int max_turns, String data_name) {
    		this.state_id = state_id;
    		this.max_turns = max_turns;
    		this.data_name = data_name;
    		this.players = players;
    		this.wins = new int[players.length];
    	}
    	private int max_turns;
    	private String data_name;

    	public Player[] getPlayers() {
    		return players;
    	}
    	
    	public int state_id;

        @Override
        public void run() {
            try {
                // secure dummy board safety:
                synchronized (TestThread.class) {
                    GameState.thread_dummy_ids.put(Thread.currentThread(), state_id);
                }
                GameSetup gameSetup = new GameSetup();
                for (int i = 0; i < players.length; i++) {
                    gameSetup.addPlayer(players[i], TestingBots.playerNames.get(i), TestingBots.pieceColors[i]);
                }
                Board gameBoard = gameSetup.getBoard();
                List<DataPoint> data = new ArrayList<>();
                iterations = 0;
                data.add(new DataPoint(new GameState(gameBoard)));
                long start = System.nanoTime();
                while (gameBoard.noWinners() && ++iterations <= max_turns) {
                	gameBoard.forceRequestMoveAndContinue();
                    data.add(new DataPoint(new GameState(gameBoard)));
                }
                long end = System.nanoTime();
                if (!gameBoard.noWinners())
                    wins[gameBoard.currentPlayerID()]++;
                else ties++;
                tests_completed++;

                int winning_player = gameBoard.currentPlayerID();
                if (gameBoard.noWinners()) winning_player = -1;
                storeData(data, winning_player);
                int win_p = (winning_player == -1)? 0 : 1 - (winning_player % 2);
                storeTestData(data_name, players[0], players[1], win_p, players.length, end - start);

                occupied.remove(Integer.valueOf(state_id));
            }
            catch(Exception e){
                System.out.println ("Exception "+e+" is caught");
                System.out.println (e.getMessage());
                e.printStackTrace();
                String xvx = "1v1";
                if (players.length == 4) xvx = "2v2";
                if (players.length == 6) xvx = "3v3";
                System.out.println("skipped match "+players[0].getTypeName()+" v "+players[1].getTypeName()+" ("+xvx+")");
                tests_completed++;
            }
            synchronized (waitingObject){
                if(tests_completed == number_of_tests){
                    waitingObject.notify();
                }
            }
            synchronized (threadWait){
                current_threads--;
                threadWait.notify();
            }
            Thread.currentThread().interrupt();
        }
        
        public Map<Player, Integer> getResults() {
        	Map<Player, Integer> results = new HashMap<>();
        	results.put(Player.NONE, ties);
        	for (int i=0; i < players.length; i++) {
        		Integer prev = results.get(players[i]);
        		if (prev == null) prev = 0;
        		results.put(players[i], prev + wins[i]);
        	}
        	return results;
        }

        private void storeData(List<DataPoint> data, int winning_player){
        	Data resulting_data = new Data();
            for (DataPoint datum : data) {
                datum.winning_player = winning_player;
                resulting_data.addData(datum);
            }
        	DataLoader.saveData(data_name, resulting_data);
        }
    }
	
    public static synchronized void storeTestData(String file_name, Player p1, Player p2, int winning_player, int nr_of_players, long time) {
    	File folder = new File(testing_path);
    	File file = new File(testing_path + file_name + ".csv");
    	folder.mkdirs();
    	if (!file.exists()) {
    		try {
				file.createNewFile();
				Files.writeString(file.toPath(), "Player-1; Player-2; Winner; Number of Players; Time (ns)\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    	String result = p1.getTypeName()+";"+p2.getTypeName()+";"+winning_player+";"+nr_of_players+";"+time+"\n";
    	try {
			Files.writeString(file.toPath(), result, StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
}
