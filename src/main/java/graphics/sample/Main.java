package graphics.sample;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application {

    public static Stage mainStage;// = new Stage();

	@Override
    public void start(Stage primaryStage) throws Exception{
        mainStage = primaryStage;
        Parent root = FXMLLoader.load(AssetFinder.getResource("Menu.fxml"));
        primaryStage.setTitle("Chinese Checkers");
        mainStage.setScene(new Scene(root, 802, 602));
        mainStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                Platform.exit();
            }
        });
        mainStage.show();
    }

    public static void main(String[] args) {
        try {
        	launch(args);
        } catch (Exception e) {
        	System.out.println(e.getCause());
        	e.printStackTrace();
        }
    }
}
