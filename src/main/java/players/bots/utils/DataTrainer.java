package players.bots.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import players.bots.utils.DataLoader.Data;
import players.bots.utils.DataLoader.DataPoint;
import players.bots.utils.DeepLearning.DLThread;

import static players.bots.utils.EveryoneShouldHaveMachineLearning.*;
import static players.bots.utils.NeuralNetwork.*;

public class DataTrainer {
	
	private List<Data> loaded_data;
	
    private long info_time_ms; // 300000l   = 5 minutes
    private long save_time_ms; // 900000l   = 15 minutes
    private long stop_time_ms; // 21600000l = 6 hours
	
    public DLThread runDeepLearning(String network_name, NeuralNetworkSettings ANNsettings, GradientDescentSettings GDsettings) {
		NeuralNetwork ann;
		if (ANNsettings == null)
			ann = loadNetwork(network_name);
		else {
			if (exists(network_name)) {
				ann = loadNetwork(network_name);
				if (!ANNsettings.matches(ann))
					throw new NetworkIOException("Loaded network does not match requested network settings");
			} else
				ann = ANNsettings.getRandomWeights(-1, 1);
		}
		
		GradientDescent GD = GDsettings.get(ann);
		System.out.println("preparing data...");
		List<Supplier<double[][]>> problems = List.of(getAllDataOrdered(BoardRep.Original));
		System.out.println("running gradient descent...");
		return DeepLearning.runDeepLearning(GD, problems, stop_time_ms, save_time_ms, info_time_ms, network_name);
    }
    
	public static void main(String[] args) {
		String network_name = "DL-datatrained";
		String[] data_names = { "complete_testing" };
		System.out.println("loading data...");
		DataTrainer DT = new DataTrainer(30000l, 5000l, 1000l, data_names);
		DT.runDeepLearning(network_name, null, new GradientDescentSettings(0.35).setDynamicLR(true, 1.25, 25).setExploration(true, 0.5, 5, 0.01));
	}
	
	public DataTrainer() {
		this(new String[0]);
	}
	public DataTrainer(String... data_names) {
		this(21600000l, 900000l, 300000l, data_names);
	}
	public DataTrainer(long stop_time, long save_time, long info_time, String... data_names) {
		this.stop_time_ms = stop_time;
		this.save_time_ms = save_time;
		this.info_time_ms = info_time;
		loaded_data = new ArrayList<>();
		loadData(data_names);
	}
	
	public void loadData(String... data_names) {
		for (int i=0; i < data_names.length; i++) {
			if (!DataLoader.exists(data_names[i])) throw new RuntimeException("Could not load file \""+data_names[i]+".training_data\"");
			loaded_data.addAll(List.of(DataLoader.loadData(data_names[i])));
		}
	}
	
	/**
	 * Compresses all data in this DataTrainer into a single Data object
	 * @return the uncompressed data that used to be stored in this DataTrainer
	 */
	public List<Data> compress() {
		Data[] data = new Data[loaded_data.size()];
		for (int i=0; i < data.length; i++) data[i] = loaded_data.get(i);
		List<Data> new_data = List.of(Data.compress(data));
		List<Data> old_data = loaded_data;
		loaded_data = new_data;
		return old_data;
	}
	
	/**
	 * The result can throw {@linkplain NoMoreDataException}
	 * @param boardRep
	 * @return
	 */
	public List<Supplier<double[][]>> getOrderedGameBatches(BoardRep boardRep) {
		List<Supplier<double[][]>> result = new ArrayList<>(loaded_data.size());
		for (Data data : loaded_data) {
			LinkedList<DataPoint> points = new LinkedList<>(data.getData());
			result.add(dataSupplier(points, boardRep));
		}
		Collections.shuffle(result);
		return result;
	}
	
	/**
	 * The result can throw {@linkplain NoMoreDataException}
	 * @param boardRep
	 * @return
	 */
	public List<Supplier<double[][]>> getShuffledGameBatches(BoardRep boardRep) {
		List<Supplier<double[][]>> result = new ArrayList<>(loaded_data.size());
		for (Data data : loaded_data) {
			LinkedList<DataPoint> points = new LinkedList<>(data.getData());
			Collections.shuffle(points);
			result.add(dataSupplier(points, boardRep));
		}
		Collections.shuffle(result);
		return result;
	}
	
	/**
	 * The result can throw {@linkplain NoMoreDataException}
	 * @param boardRep
	 * @return
	 */
	public Supplier<double[][]> getAllDataOrdered(BoardRep boardRep) {
		List<Data> shuffled = new ArrayList<>(loaded_data);
		Collections.shuffle(shuffled);
		List<DataPoint> ordered = shuffled.stream().flatMap(data -> data.getData().stream()).collect(Collectors.toList());
		return dataSupplier(new LinkedList<>(ordered), boardRep);
	}
	/**
	 * The result can throw {@linkplain NoMoreDataException}
	 * @param boardRep
	 * @return
	 */
	public Supplier<double[][]> getAllDataShuffled(BoardRep boardRep) {
		List<DataPoint> ordered = loaded_data.stream().flatMap(data -> data.getData().stream()).collect(Collectors.toList());
		Collections.shuffle(ordered);
		return dataSupplier(new LinkedList<>(ordered), boardRep);
	}
	
	/**
	 * The result can throw {@linkplain NoMoreDataException}
	 * @param points
	 * @param boardRep
	 * @return
	 */
	private Supplier<double[][]> dataSupplier(Queue<DataPoint> points, BoardRep boardRep) {
		return () -> {
			DataPoint dp = points.poll();
			if (dp == null) throw new NoMoreDataException();
			double[] input = dp.getMatrixUnrolled(boardRep);
			double win = (dp.winning_player == dp.current_player)? 1:-1;
			double output = (win*win+win)/2;
			if (dp.turn_count != DataPoint.UNKNOWN) output = 0.5 + win * 0.5 * ((dp.turn_index+1d) / dp.turn_count);
			return new double[][] {input, new double[] {output} };
		};
	}
	
	public class NoMoreDataException extends RuntimeException {
		private static final long serialVersionUID = 1L;
		public NoMoreDataException() {
			super("The data queue has ran out of data to process");
		}
	}
	
}
