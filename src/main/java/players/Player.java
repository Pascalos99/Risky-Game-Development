package players;

import gamerules.Board;
import gamerules.Move;

public interface Player {
     public Move returnMove(Board gameBoard);
}
