package graphics.sample;


import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;

public class GameRuleController {


    @FXML
    private void Exit(ActionEvent event) throws IOException{
        Controller.newStage.close();
        Main.mainStage.show();
    }
}



