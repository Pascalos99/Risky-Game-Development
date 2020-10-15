package players.bots.utils;

import gamerules.BoardNode;
import gamerules.DirectedAdjacencyMap;
import gamerules.DirectedAdjacencyMap.IndexPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static gamerules.BoardNode.*;

public class Dijkstra {
    private HashMap<BoardNode,Integer> distances;
    private BoardNode target;
    
    private Dijkstra() {
    	
    }
    
    private static int[][] lookup_table = null;
    
    private void recursive(BoardNode position,Integer distance){
        if(distance>20) return;
        if(distances.containsKey(position)){
            if(distances.get(position)>distance){
                distances.replace(position,distance);
            }
            else{
                return;
            }
        }
        else{
            distances.put(position,distance);
        }
        if(position!=target){
            for(BoardNode node : position.getNeighbours()){
                recursive(node,distance+1);
            }
        }
    }

    public static int getDistance(BoardNode goal, BoardNode start) {
    	if (lookup_table == null) {
    		ArrayList<BoardNode> nodes = new ArrayList<>();
    		setupTable(getAllNodes(nodes, start));
    	}
    	return lookup_table[start.getID()][goal.getID()];
    }
    
    /**
     * after some testing, this gives the same result for all possible node pairs but computes roughly 30-90 times faster:
     * test1: [computing entire lookup table]
     * old-method took 3000 ms
     * new-method took 33 ms
     * test2: [computing entire lookup table, order reversed - java favors whoever goes last]
     * new-method took 81 ms
     * old-method took 2681 ms
     */
    private int simpleCalcDistance(BoardNode from, BoardNode to, int distance_so_far) {
    	if (to.isNeighbour(from)) return distance_so_far + 1;
    	IndexPoint from_index = DirectedAdjacencyMap.getRowIndex(from.getID());
    	IndexPoint to_index = DirectedAdjacencyMap.getRowIndex(to.getID());
    	if (from_index.row_index == to_index.row_index) return distance_so_far + Math.abs(from_index.node_index - to_index.node_index);
    	
    	boolean go_down = false;
    	boolean go_left = false;
    	if (to_index.row_index > from_index.row_index) go_down = true;
    	int from_deviation = from_index.node_index - (DirectedAdjacencyMap.num_nodes_per_row[from_index.row_index] - 1) / 2;
    	int to_deviation = to_index.node_index - (DirectedAdjacencyMap.num_nodes_per_row[to_index.row_index] - 1) / 2;
    	if (to_deviation <= from_deviation) go_left = true;
    	
    	boolean[] can_go = new boolean[6];
    	for (Integer dir : from.getDirections()) can_go[dir] = true;
    	
    	if (go_down) {
    		if (!can_go[BOTTOMLEFT] && !can_go[BOTTOMRIGHT])
    			return simpleCalcDistance(from.getNeighbourToThe(go_left? LEFT : RIGHT), to, distance_so_far + 1);
    		boolean can_go_direct = (go_left && can_go[BOTTOMLEFT]) || (!go_left && can_go[BOTTOMRIGHT]);
    		return simpleCalcDistance(from.getNeighbourToThe(
    				can_go_direct? (go_left? BOTTOMLEFT : BOTTOMRIGHT) : (go_left? BOTTOMRIGHT : BOTTOMLEFT)
    				), to, distance_so_far + 1);
    	}
    	else {
    		if (!can_go[TOPLEFT] && !can_go[TOPRIGHT])
    			return simpleCalcDistance(from.getNeighbourToThe(go_left? LEFT : RIGHT), to, distance_so_far + 1);
    		boolean can_go_direct = (go_left && can_go[TOPLEFT]) || (!go_left && can_go[TOPRIGHT]);
    		return simpleCalcDistance(from.getNeighbourToThe(
    				can_go_direct? (go_left? TOPLEFT : TOPRIGHT) : (go_left? TOPRIGHT : TOPLEFT)
    				), to, distance_so_far + 1);
    	}
    }
    
    private static boolean test_simplecalc = true;
    
    private int calculateDistances(BoardNode goal, BoardNode startPoint) {
    	if (test_simplecalc) return simpleCalcDistance(startPoint, goal, 0);
    	target = goal;
        distances = new HashMap<BoardNode,Integer>();
        recursive(startPoint,0);
        return distances.get(target);
    }
    
    private static List<BoardNode> getAllNodes(List<BoardNode> to_fill, BoardNode start) {
    	to_fill.add(start);
    	for (BoardNode neighbour : start.getNeighbours())
    		if (to_fill.contains(neighbour)) continue;
    		else getAllNodes(to_fill, neighbour);
    	return to_fill;
    }
    
    /**
     * @param allNodes should all be nodes with consequtive IDs starting from 0 and ending at allNodes.size()-1
     */
    public static void setupTable(List<BoardNode> allNodes) {
    	is_calculating_table = true;
    	Dijkstra dijk = new Dijkstra();
    	lookup_table = new int[allNodes.size()][allNodes.size()];
    	for (BoardNode from : allNodes)
    		for (BoardNode to : allNodes)
    			lookup_table[from.getID()][to.getID()] = dijk.calculateDistances(to, from);
    	is_table_setup = true;
    	is_calculating_table = false;
    }
    
    private static boolean is_table_setup = false;
    private static boolean is_calculating_table = false;
    
    public static boolean isTableSetup() {
    	return is_table_setup;
    }
    public static boolean isCalculatingTable() {
    	return is_calculating_table;
    }
}
