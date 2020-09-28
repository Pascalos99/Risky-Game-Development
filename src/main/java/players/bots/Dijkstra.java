package players.bots;

import gamerules.BoardNode;

import java.util.HashMap;

public class Dijkstra {
    private BoardNode target;
    private BoardNode startPoint;
    private HashMap<BoardNode,Integer> distances;

    private void recursive(BoardNode position,int distance){
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

    public int getDistances(BoardNode target, BoardNode startPoint) {
        this.target = target;
        this.startPoint = startPoint;
        distances = new HashMap<BoardNode,Integer>();
        recursive(startPoint,0);
        return distances.get(target);
    }
}
