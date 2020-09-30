package players.bots;

import gamerules.BoardNode;

import java.util.HashMap;

public class Dijkstra {
    private HashMap<BoardNode,Integer> distances;
    private BoardNode target;
    
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
        target = goal;
        distances = new HashMap<BoardNode,Integer>();
        recursive(startPoint,0);
        return distances.get(target);
    }
}
