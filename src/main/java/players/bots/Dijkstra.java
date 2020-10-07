package players.bots;

import gamerules.BoardNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Dijkstra {
    private HashMap<BoardNode,Integer> distances;
    private BoardNode target;
    
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

    public int getDistances(BoardNode goal, BoardNode startPoint) {
    	if (lookup_table == null) {
    		ArrayList<BoardNode> nodes = new ArrayList<>();
    		setupTable(getAllNodes(nodes, startPoint));
    	}
    	return lookup_table[startPoint.getID()][goal.getID()];
    }
    
    private int calculateDistances(BoardNode goal, BoardNode startPoint) {
    	target = goal;
        distances = new HashMap<BoardNode,Integer>();
        recursive(startPoint,0);
        return distances.get(target);
    }
    
    private List<BoardNode> getAllNodes(List<BoardNode> to_fill, BoardNode start) {
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
