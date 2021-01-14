package players.bots.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
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
	
	public static void main(String[] args) {
		Data[] load = loadData("new_testing");
		System.out.println(load.length);
		int[] lengths = new int[load.length];
		for (int i=0; i < lengths.length; i++) lengths[i] = load[i].getData().size();
		System.out.println(Arrays.toString(lengths));
	}
	
	public static void saveData(String name, GameState...all_states_of_game) {
		Data data = new Data();
		data.addData(all_states_of_game);
		saveData(name, data);
	}
	
	/**
	 * appends the given data to the file
	 * @param name the file to save to (just the name, path and extension not needed)
	 * @param data the data to append to the file
	 */
	public static synchronized void saveData(String name, Data...data) {
		File folder = new File(datapath);
		folder.mkdirs();
		File data_file = new File(datapath + name + ".training_data");
		if (!data_file.exists())
			try {
				data_file.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		StringBuilder output = new StringBuilder();
		for (int i=0; i < data.length; i++)
			output.append(data[i]+";\n");
		try {
			Files.writeString(data_file.toPath(), output.toString(), StandardOpenOption.APPEND);
		} catch (IOException e) {
			System.out.println("something went wrong when writing data");
			e.printStackTrace();
		}
	}
	
	public static Data[] loadData(String name) {
		File folder = new File(datapath);
		folder.mkdirs();
		File data_file = new File(datapath + name + ".training_data");
		if (!data_file.exists()) throw new RuntimeException("could not find data \""+name+"\"");
		try {
			String data = Files.readString(data_file.toPath()).strip().replaceAll("//.*\n", "\n").replaceAll("//.*", "");
			String[] data_parts = data.split(";");
			Data[] result = new Data[data_parts.length];
			for (int i=0; i < result.length; i++)
				result[i] = Data.parseData(data_parts[i]);
			return result;
		} catch (IOException e) {
			System.out.println("something went wrong when reading data");
			e.printStackTrace();
		}
		return new Data[0];
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
		public void addData(DataPoint...dataPoints) {
			for (int i=0; i < dataPoints.length; i++) addData(dataPoints[i]);
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
			for (int i=0; i < data.size(); i++) {
				sb.append(data.get(i));
				if (i < data.size()-1) sb.append("\n");
			}
			return sb.toString();
		}
		
		public static Data parseData(String str) {
			String[] points = str.strip().split("[\\s\\h\\v]*\n[\\s\\h\\v]*");
			Data result = new Data(points.length);
			for (int i=0; i < points.length; i++)
				result.addData(DataPoint.parseDataPoint(points[i]));
			return result;
		}
		
	}
	
	public static class DataPoint {
		public static final int UNKNOWN = -1;
		
		/**
		 * The gamestate of the game at this moment in the game (before the move of currentplayer is executed)
		 */
		public final byte[] gamestate;
		/**
		 * The current player who is about to make a move (move has not yet been made)
		 */
		public final int current_player;
		/**
		 * The player who ended up winning this game
		 */
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
