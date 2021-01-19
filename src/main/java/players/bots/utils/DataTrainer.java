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
import static players.bots.utils.EveryoneShouldHaveMachineLearning.*;
import static players.bots.utils.NeuralNetwork.*;

public class DataTrainer {
	
	private List<Data> loaded_data;
	
    public static long info_time_ms = 1000l;//300000l / 3; // 5 minutes / 3 = 100 seconds
    public static long save_time_ms = 5000l;//900000l / 3; // 15 minutes / 3 = 5 minutes
    public static long stop_time_ms = 30000l;//21600000l / 6; // 6 hours / 6 = 1 hour
	
	public static void main(String[] args) {
		String network_name = "DL-datatrained";
		String data_name = "complete_testing";
		if (!DataLoader.exists(data_name)) throw new RuntimeException("Could not load file \""+data_name+".training_data\"");
		NeuralNetwork ann;
		if (!exists(network_name)) {
			ann = new NeuralNetwork(new int[] {
					162, 40, 1
			}, new boolean[] {
					true, true
			}, new Activation[] {
					SILU, SIGMOID
			});
		} else { ann = loadNetwork(network_name); }
		GradientDescent GD = new GradientDescent(ann, HALF_SQUARE_ERROR, 0.35);
		GD.setDynamicLR(true, 1.25, 25);
		GD.setExploration(true, 0.5, 5, null);
		DataTrainer DT = new DataTrainer(data_name);
		List<Supplier<double[][]>> problems = DT.getOrderedGameBatches(BoardRep.Original);
		DeepLearning.runDeepLearning(GD, problems, stop_time_ms, save_time_ms, info_time_ms, network_name);
	}
	
	public DataTrainer(String... data_names) {
		loaded_data = new ArrayList<>();
		for (int i=0; i < data_names.length; i++)
			loaded_data.addAll(List.of(DataLoader.loadData(data_names[i])));
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
		List<DataPoint> ordered = loaded_data.stream().flatMap(data -> data.getData().stream()).collect(Collectors.toList());
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
			double[] output = new double[] { (dp.winning_player == dp.current_player)? 1:0 };
			return new double[][] {input, output};
		};
	}
	
	public class NoMoreDataException extends RuntimeException {
		public NoMoreDataException() {
			super("The data queue has ran out of data to process");
		}
	}
	
}
