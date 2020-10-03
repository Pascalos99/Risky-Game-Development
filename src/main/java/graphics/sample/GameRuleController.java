package graphics.sample;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import java.io.IOException;

public class GameRuleController {


    @FXML
    private void Exit(ActionEvent event) throws IOException{
        Controller.newStage.close();
        Main.mainStage.show();
    }
}



