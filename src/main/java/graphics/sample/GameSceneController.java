package graphics.sample;

import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;

import java.io.IOException;

public class GameSceneController {

    public static Pane GamePane;
    public static SwingNode swingnode;

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

