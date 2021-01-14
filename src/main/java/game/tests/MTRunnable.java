package game.tests;

import java.util.ArrayList;
import java.util.List;

import game.GameSetup;
import gamerules.Board;
import gamerules.GameState;
import players.bots.utils.DataLoader;
import players.bots.utils.DataLoader.Data;
import players.bots.utils.DataLoader.DataPoint;

public class MTRunnable implements Runnable {
    public static double timeTaken = 0;
    public static double runs = 0;

	public MTRunnable(Integer state_id) {
		this.state_id = state_id;
	}

	private static int last_state_id = 0;
	public Integer state_id;

	public static int max_turns = 2000;
	public static String data_name = "new_testing";

    @Override
    public void run() {
        try {
            //For checking if the number of threads ran is correct
            // System.out.println("Firing Thread"+Thread.currentThread().getId());
            // secure dummy board safety:
            synchronized (MTRunnable.class) {
                GameState.thread_dummy_ids.put(Thread.currentThread(),
                        (state_id == null) ? ++last_state_id : state_id);
            }
            long startTime = System.nanoTime();
            GameSetup gameSetup = new GameSetup();
            for (int i = 0; i < TestingBots.players.length; i++) {
                gameSetup.addPlayer(TestingBots.str2p(TestingBots.players[i]), TestingBots.playerNames.get(i), TestingBots.pieceColors[i]);
            }
            Board gameBoard = gameSetup.getBoard();
            List<DataPoint> data = new ArrayList<>();
            int iter = 0;
            data.add(new DataPoint(new GameState(gameBoard)));
            while (gameBoard.noWinners() && ++iter <= max_turns) {
                long finishTime = System.nanoTime();
                timeTaken = (finishTime - startTime);
                runs += 1;
                while (gameBoard.noWinners()) {
                    gameBoard.forceRequestMoveAndContinue();
                    data.add(new DataPoint(new GameState(gameBoard)));
                }
                if (gameBoard.getWinner() != null)
                    TestingBots.wins[TestingBots.playerNames.indexOf(gameBoard.getWinner().getName())]++;
                else TestingBots.ties++;
                // Printing who won the single game
                // System.out.println(gameBoard.getWinner().getName());
                TestingBots.tests_completed++;

                int winning_player = gameBoard.currentPlayerID();
                if (gameBoard.noWinners()) winning_player = -1;
                storeData(data, winning_player);

                TestingBots.occupied.remove(Integer.valueOf(state_id));
                //For checking if the number of threads ran is correct
                //System.out.println("Finished thread"+Thread.currentThread().getId());
            }
        }
        catch(Exception e){
            System.out.println ("Exception "+e+" is caught");
            System.out.println (e.getMessage());
            e.printStackTrace();
        }
        synchronized (TestingBots.waitingObject){
            if(TestingBots.tests_completed == TestingBots.TESTS_NUMBER){
                TestingBots.waitingObject.notify();
            }
        }
        synchronized (TestingBots.threadWait){
            TestingBots.current_threads--;
            TestingBots.threadWait.notify();
        }
        Thread.currentThread().interrupt();
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
