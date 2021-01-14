package players.bots.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import players.bots.utils.DataLoader.Data;
import players.bots.utils.DataLoader.DataPoint;;

public class DataTrainer {
	
	private List<Data> loaded_data;
	
	public DataTrainer(String... data_names) {
		loaded_data = new ArrayList<>();
		for (int i=0; i < data_names.length; i++)
			loaded_data.addAll(List.of(DataLoader.loadData(data_names[i])));
	}
	
	public List<Supplier<double[][]>> getOrderedGameBatches(BoardRep boardRep) {
		List<Supplier<double[][]>> result = new ArrayList<>(loaded_data.size());
		for (Data data : loaded_data) {
			LinkedList<DataPoint> points = new LinkedList<>(data.getData());
			result.add(dataSupplier(points, boardRep));
		}
		Collections.shuffle(result);
		return result;
	}
	
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
	
	public Supplier<double[][]> getAllDataOrdered(BoardRep boardRep) {
		List<DataPoint> ordered = loaded_data.stream().flatMap(data -> data.getData().stream()).collect(Collectors.toList());
		return dataSupplier(new LinkedList<>(ordered), boardRep);
	}
	public Supplier<double[][]> getAllDataShuffled(BoardRep boardRep) {
		List<DataPoint> ordered = loaded_data.stream().flatMap(data -> data.getData().stream()).collect(Collectors.toList());
		Collections.shuffle(ordered);
		return dataSupplier(new LinkedList<>(ordered), boardRep);
	}
	
	private Supplier<double[][]> dataSupplier(Queue<DataPoint> points, BoardRep boardRep) {
		return () -> {
			DataPoint dp = points.poll();
			double[] input = dp.getMatrixUnrolled(boardRep);
			double[] output = new double[] { (dp.winning_player == dp.current_player)? 1:0 };
			return new double[][] {input, output};
		};
	}
	
}
