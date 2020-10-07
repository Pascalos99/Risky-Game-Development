package graphics.sample;

import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.Pane;

import javax.swing.*;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class GameSceneController {

    @FXML
    public static Pane GamePane;
//    @FXML
//    public static SwingNode swingNode;

    @FXML
    private void Exit(ActionEvent event) throws IOException {
        Controller.newStage.close();
        Main.mainStage.show();
    }
    @FXML
    private void PlayAgain(ActionEvent event) throws IOException {
        Controller.newStage.close();
        Main.mainStage.show();
    }
}

