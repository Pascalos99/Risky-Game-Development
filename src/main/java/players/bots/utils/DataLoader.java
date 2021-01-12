package players.bots.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gamerules.GameState;
import graphics.sample.AssetFinder;

public class DataLoader {
	
	public static final String datapath = AssetFinder.assetsPath+"training_data"+File.separator;
	
	public static void saveData(String name, GameState...all_states_of_game) {
		
	}
	
	public static void saveData(String name, Data...data) {
		
	}
	
	public static class Data {
		private List<DataPoint> data;
		private List<DataPoint> random_access_data;
		private boolean data_modified;
		
		public Data(int initialCapacity) {
			data = new ArrayList<>(initialCapacity);
			data_modified = true;
		}
		public Data() {
			this(10);
		}
		
		public static Data compress(Data...data) {
			Data total = new Data();
			for (int i=0; i < data.length; i++) total.addData(data[i]);
			return total;
		}
		
		public List<DataPoint> getData() {
			if (data_modified) {
				random_access_data = Collections.unmodifiableList(data);
				data_modified = false;
			} return random_access_data;
		}
		
		public void addData(DataPoint dataPoint) {
			synchronized(this) {
				data.add(dataPoint);
				data_modified = true;
			}
		}
		public void addData(Data data) {
			addDatapoints(data.data);
		}
		public void addDatapoints(List<DataPoint> data) {
			for (DataPoint dp : data) addData(dp);
		}
		public void addData(List<Data> data) {
			for (Data d : data) addData(d);
		}
		public void addData(int winning_player, GameState...gameStates) {
			for (int i=0; i < gameStates.length; i++)
				addData(new DataPoint(gameStates[i], winning_player));
		}
		/** All the data added will have an UNKNOWN player win value, unless one of the given states is the final state (with a winner),
		 * in that case, all datapoints added will have the same player win value (the win-value of the last encountered state with a winning player) */
		public void addData(GameState...gameStates) {
			Data temp = new Data();
			int win_value = DataPoint.UNKNOWN;
			for (int i=0; i < gameStates.length; i++) {
				DataPoint dp = new DataPoint(gameStates[i]);
				temp.addData(dp);
				if (dp.winning_player != DataPoint.UNKNOWN) win_value = dp.winning_player;
			}
			temp.setAllWinStatesTo(win_value);
			addData(temp);
		}
		/** The data added will have an UNKNOWN player win value, unless the given state is the final state (with a winner) */
		public void addTempData(GameState state) {
			addData(new DataPoint(state));
		}
		
		public void setAllWinStatesTo(int player_won) {
			for (DataPoint dp : data) dp.winning_player = player_won;
		}
		
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (DataPoint dp : data) {
				sb.append(dp);
				sb.append("\n");
			}
			return sb.toString();
		}
		
		public static Data parseData(String str) {
			String[] points = str.strip().split("[\\s\\h\\v]*\n[\\s\\h\\v]*");
			Data result = new Data();
			for (int i=0; i < points.length; i++)
				result.addData(DataPoint.parseDataPoint(points[i]));
			return result;
		}
		
	}
	
	public static class DataPoint {
		public static final int UNKNOWN = -1;
		
		public final byte[] gamestate;
		public final int current_player;
		public int winning_player;
		
		private Map<BoardRep, double[][][]> matrixReps = null;
		
		/** @param datapoint_stored an array containing: {@code [currentplayer, winningplayer, gamestate]} in that order */
		public DataPoint(byte[] datapoint_stored) {
			current_player = datapoint_stored[0];
			winning_player = datapoint_stored[1];
			gamestate = Arrays.copyOfRange(datapoint_stored, 2, datapoint_stored.length);
		}
		public DataPoint(byte[] gamestate, int current_player, int winning_player) {
			this.gamestate = Arrays.copyOf(gamestate, gamestate.length);
			this.current_player = current_player;
			this.winning_player = winning_player;
		}
		public DataPoint(GameState gamestate, int winning_player) {
			this.gamestate = gamestate.getIntegerRepresentation();
			this.current_player = gamestate.currentPlayerID();
			this.winning_player = winning_player;
		}
		/** Will set winning_player info to UNKNOWN unless current player won in this gamestate */
		public DataPoint(GameState gamestate) {
			this(gamestate, getIfWon(gamestate));
		}
		/**
		 * @param gamestate the state to evaluate
		 * @return {@code -1} if no player won in this state, the current player ID otherwise
		 */
		private static int getIfWon(GameState gamestate) {
			if (!gamestate.hasWinner()) return UNKNOWN;
			return gamestate.currentPlayerID();
		}
		
		public byte[] getArray() {
			byte[] result = new byte[gamestate.length + 2];
			result[0] = (byte) current_player;
			result[1] = (byte) winning_player;
			for (int i=2; i < result.length; i++)
				result[i] = gamestate[i-2];
			return result;
		}
		
		public double[][][] getMatrix(BoardRep boardRep) {
			if (matrixReps == null) matrixReps = new HashMap<>();
			double[][][] result = matrixReps.get(boardRep);
			if (result == null) result = matrixReps.put(boardRep, GameState.getMatrix3d(current_player, gamestate, boardRep));
			return result;
		}
		public double[] getMatrixUnrolled(BoardRep boardRep) {
			return Utils.unrollMatrix(getMatrix(boardRep));
		}
		
		public String toString() {
			byte[] bytearray = getArray();
			double[] array = new double[bytearray.length];
			for (int i=0; i < array.length; i++) array[i] = bytearray[i];
			return Utils.arrayToString(array, 0);
		}
		public static DataPoint parseDataPoint(String str) {
			double[] temp = Utils.extractVector(Utils.parseMatrix(str));
			byte[] result = new byte[temp.length];
			for (int i=0; i < temp.length; i++) result[i] = (byte)temp[i];
			return new DataPoint(result);
		}
	}
	
}
