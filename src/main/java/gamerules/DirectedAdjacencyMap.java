package gamerules;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DirectedAdjacencyMap {
	
	public static final byte NULL = -1;
	
	public static final int INDEX_TOPLEFT = 0;
	public static final int INDEX_TOPRIGHT = 1;
	public static final int INDEX_LEFT = 2;
	public static final int INDEX_RIGHT = 3;
	public static final int INDEX_BOTTOMLEFT = 4;
	public static final int INDEX_BOTTOMRIGHT = 5;
	
	public static final String[] DIRECTION_NAMES = {"Top-Left","Top-Right","Left","Right","Bottom-Left","Bottom-Right"};
	
	private static Map<Byte, byte[]> adjacency_map;
    
    public static Map<Byte, byte[]> getAdjacencyMap() {
    	
    	if (adjacency_map != null) return adjacency_map;
    	
    	adjacency_map = new HashMap<>();
    	
    	for (byte node=0; node < 121; node++) {
    		byte[] neighbours = new byte[6];
    		Arrays.fill(neighbours, NULL);
    		IndexPoint nodeIndex = getRowIndex(node);
    		
    		int this_row = nodeIndex.row_index;
    		if (this_row != 0) {
    			// TOP
    			int node_topleft = node - (num_nodes_per_row[this_row] + num_nodes_per_row[this_row - 1])/2 - 1;
    			int node_topright = node - (num_nodes_per_row[this_row] + num_nodes_per_row[this_row - 1])/2;
    			if (getRowIndex(node_topleft).row_index == this_row - 1) neighbours[INDEX_TOPLEFT] = (byte) node_topleft;
        		if (getRowIndex(node_topright).row_index == this_row - 1) neighbours[INDEX_TOPRIGHT] = (byte) node_topright;
    		}
    		if (this_row != num_nodes_per_row.length-1) {
    			// BOTTOM
    			int node_bottomleft = node + (num_nodes_per_row[this_row] + num_nodes_per_row[this_row + 1])/2;
    			int node_bottomright = node + (num_nodes_per_row[this_row] + num_nodes_per_row[this_row + 1])/2 + 1;
    			if (getRowIndex(node_bottomleft).row_index == this_row + 1) neighbours[INDEX_BOTTOMLEFT] = (byte) node_bottomleft;
        		if (getRowIndex(node_bottomright).row_index == this_row + 1) neighbours[INDEX_BOTTOMRIGHT] = (byte) node_bottomright;
    		}
    		// LEFT AND RIGHT
    		if (getRowIndex(node - 1).row_index == this_row) neighbours[INDEX_LEFT] = (byte) (node - 1);
    		if (getRowIndex(node + 1).row_index == this_row) neighbours[INDEX_RIGHT] = (byte) (node + 1);
    		
    		adjacency_map.put(node, neighbours);
    	}
    	
    	return adjacency_map;
    	
    }
    
    public static IndexPoint getRowIndex(int nodeIndex) {
    	int row = 0;
    	int nodes = 0;
    	for (int i=0; i < nodeIndex; i++) {
    		nodes++;
    		if (num_nodes_per_row[row] <= nodes) {
    			nodes = 0;
    			row++;
    		}
    	}
    	return new IndexPoint(row, nodes);
    }
    
    public static class IndexPoint {
    	
    	public final int row_index;
    	public final int node_index;
    	
    	public IndexPoint(int row, int node) {
    		row_index = row;
    		node_index = node;
    	}
    	public String toString() {
    		return "row: "+row_index+", node: "+node_index;
    	}
    }

	public static final int[] num_nodes_per_row = {
			1, 2, 3, 4, 5 + 8, 6 + 6, 7 + 4, 8 + 2, 9,
			8 + 2, 7 + 4, 6 + 6, 5 + 8, 4, 3, 2, 1
	};

}
