package graphics.sample;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

import java.net.URL;
import java.util.ResourceBundle;
import java.io.IOException;


public class Controller implements Initializable {

    public static Stage newStage = new Stage();
    public String Name1;
    public String Name2;
    public String Name3;
    public String Name4;
    public String Name5;
    public String Name6;

    @FXML
    private JFXTextField name1;
    @FXML
    private JFXTextField name2;
    @FXML
    private JFXTextField name3;
    @FXML
    private JFXTextField name4;
    @FXML
    private JFXTextField name5;
    @FXML
    private JFXTextField name6;

    @FXML
    private void Play() throws IOException{
        Name1 = name1.getText();
        Name2 = name2.getText();
        Name3 = name3.getText();
        Name4 = name4.getText();
        Name5 = name5.getText();
        Name6 = name6.getText();
        System.out.println(Name1);
    }

    @FXML
    private void Rules(ActionEvent event) throws IOException{
        StageChanger();
//        Main.mainStage.hide();
        newStage.showAndWait();
    }

    public void StageChanger() throws IOException {
        Parent type2view = FXMLLoader.load(getClass().getResource("GameRule.fxml"));
        Scene type2ViewScene = new Scene(type2view);
        newStage.setScene(type2ViewScene);
        newStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                newStage.close();
//                Main.mainStage.show();
            }
        });
    }

    @FXML
    public JFXComboBox<String> combo1;
    @FXML
    public JFXComboBox<String> combo2;
    @FXML
    public JFXComboBox<String> combo3;
    @FXML
    public JFXComboBox<String> combo4;
    @FXML
    public JFXComboBox<String> combo5;
    @FXML
    public JFXComboBox<String> combo6;

    @FXML
    public JFXComboBox<String> Color1;
    @FXML
    public JFXComboBox<String> Color2;
    @FXML
    public JFXComboBox<String> Color3;
    @FXML
    public JFXComboBox<String> Color4;
    @FXML
    public JFXComboBox<String> Color5;
    @FXML
    public JFXComboBox<String> Color6;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ObservableList<String> lis = FXCollections.observableArrayList(
                "Human", "Aliens", "None"
        );
        combo1.setItems(lis);
        combo2.setItems(lis);
        combo3.setItems(lis);
        combo4.setItems(lis);
        combo5.setItems(lis);
        combo6.setItems(lis);

        ObservableList<String> lis2 = FXCollections.observableArrayList(
                "Red", "Blue", "Yellow","Green","Black","White"
        );
        Color1.setItems(lis2);
        Color2.setItems(lis2);
        Color3.setItems(lis2);
        Color4.setItems(lis2);
        Color5.setItems(lis2);
        Color6.setItems(lis2);

        Color1.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {

            @Override
            public ListCell<String> call(ListView<String> arg0) {
                ListCell<String> cell = new ListCell<String>() {

                    @Override
                    public void updateItem(String person, boolean empty) {
                        super.updateItem(person, empty);
                        if (person != null) {
                            setText(person);
                            if (person.equals("Red")) {
                                setTextFill(Color.RED);
                            }
                            else if(person.equals("Green")){
                                setTextFill(Color.GREEN);
                            }
                            else if(person.equals("Yellow")){
                                setTextFill(Color.YELLOW);
                            }
                            else if(person.equals("Blue")){
                                setTextFill(Color.BLUE);
                            }
                            else if(person.equals("Black")){
                                setTextFill(Color.BLACK);
                            }
                            else if(person.equals("White")){
                                setTextFill(Color.BLACK);
                            }
                        } else {
                            setText(Color1.getPromptText());
                        }
                    }
                };
                return cell;
            }
        });
        Color2.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {

            @Override
            public ListCell<String> call(ListView<String> arg0) {
                ListCell<String> cell = new ListCell<String>() {

                    @Override
                    public void updateItem(String person, boolean empty) {
                        super.updateItem(person, empty);
                        if (person != null) {
                            setText(person);
                            if (person.equals("Red")) {
                                setTextFill(Color.RED);
                            }
                            else if(person.equals("Green")){
                                setTextFill(Color.GREEN);
                            }
                            else if(person.equals("Yellow")){
                                setTextFill(Color.YELLOW);
                            }
                            else if(person.equals("Blue")){
                                setTextFill(Color.BLUE);
                            }
                            else if(person.equals("Black")){
                                setTextFill(Color.BLACK);
                            }
                            else if(person.equals("White")){
                                setTextFill(Color.BLACK);
                            }
                        } else {
                            setText(Color2.getPromptText());
                        }
                    }
                };
                return cell;
            }
        });
        Color3.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {

            @Override
            public ListCell<String> call(ListView<String> arg0) {
                ListCell<String> cell = new ListCell<String>() {

                    @Override
                    public void updateItem(String person, boolean empty) {
                        super.updateItem(person, empty);
                        if (person != null) {
                            setText(person);
                            if (person.equals("Red")) {
                                setTextFill(Color.RED);
                            }
                            else if(person.equals("Green")){
                                setTextFill(Color.GREEN);
                            }
                            else if(person.equals("Yellow")){
                                setTextFill(Color.YELLOW);
                            }
                            else if(person.equals("Blue")){
                                setTextFill(Color.BLUE);
                            }
                            else if(person.equals("Black")){
                                setTextFill(Color.BLACK);
                            }
                            else if(person.equals("White")){
                                setTextFill(Color.BLACK);
                            }
                        } else {
                            setText(Color3.getPromptText());
                        }
                    }
                };
                return cell;
            }
        });
        Color4.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {

            @Override
            public ListCell<String> call(ListView<String> arg0) {
                ListCell<String> cell = new ListCell<String>() {

                    @Override
                    public void updateItem(String person, boolean empty) {
                        super.updateItem(person, empty);
                        if (person != null) {
                            setText(person);
                            if (person.equals("Red")) {
                                setTextFill(Color.RED);
                            }
                            else if(person.equals("Green")){
                                setTextFill(Color.GREEN);
                            }
                            else if(person.equals("Yellow")){
                                setTextFill(Color.YELLOW);
                            }
                            else if(person.equals("Blue")){
                                setTextFill(Color.BLUE);
                            }
                            else if(person.equals("Black")){
                                setTextFill(Color.BLACK);
                            }
                            else if(person.equals("White")){
                                setTextFill(Color.BLACK);
                            }
                        } else {
                            setText(Color4.getPromptText());
                        }
                    }
                };
                return cell;
            }
        });
        Color5.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {

            @Override
            public ListCell<String> call(ListView<String> arg0) {
                ListCell<String> cell = new ListCell<String>() {

                    @Override
                    public void updateItem(String person, boolean empty) {
                        super.updateItem(person, empty);
                        if (person != null) {
                            setText(person);
                            if (person.equals("Red")) {
                                setTextFill(Color.RED);
                            }
                            else if(person.equals("Green")){
                                setTextFill(Color.GREEN);
                            }
                            else if(person.equals("Yellow")){
                                setTextFill(Color.YELLOW);
                            }
                            else if(person.equals("Blue")){
                                setTextFill(Color.BLUE);
                            }
                            else if(person.equals("Black")){
                                setTextFill(Color.BLACK);
                            }
                            else if(person.equals("White")){
                                setTextFill(Color.BLACK);
                            }
                        } else {
                            setText(Color5.getPromptText());
                        }
                    }
                };
                return cell;
            }
        });
        Color6.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {

            @Override
            public ListCell<String> call(ListView<String> arg0) {
                ListCell<String> cell = new ListCell<String>() {

                    @Override
                    public void updateItem(String person, boolean empty) {
                        super.updateItem(person, empty);
                        if (person != null) {
                            setText(person);
                            if (person.equals("Red")) {
                                setTextFill(Color.RED);
                            }
                            else if(person.equals("Green")){
                                setTextFill(Color.GREEN);
                            }
                            else if(person.equals("Yellow")){
                                setTextFill(Color.YELLOW);
                            }
                            else if(person.equals("Blue")){
                                setTextFill(Color.BLUE);
                            }
                            else if(person.equals("Black")){
                                setTextFill(Color.BLACK);
                            }
                            else if(person.equals("White")){
                                setTextFill(Color.BLACK);
                            }
                        } else {
                            setText(Color6.getPromptText());
                        }
                    }
                };
                return cell;
            }
        });

    }

    @FXML
    public void comboBoxWasUpdated1()
    {
        this.combo1.setPromptText(combo1.getValue());

    }
    @FXML
    public void comboBoxWasUpdated2()
    {
        this.combo2.setPromptText(combo2.getValue());
    }
    @FXML
    public void comboBoxWasUpdated3()
    {
        this.combo3.setPromptText(combo3.getValue());
    }
    @FXML
    public void comboBoxWasUpdated4()
    {
        this.combo4.setPromptText(combo4.getValue());
    }
    @FXML
    public void comboBoxWasUpdated5()
    {
        this.combo5.setPromptText(combo5.getValue());
    }
    @FXML
    public void comboBoxWasUpdated6()
    {
        this.combo6.setPromptText(combo6.getValue());
    }
    @FXML
    public void comboBoxWasUpdated7()
    {
        this.Color1.setPromptText(Color1.getValue());
    }
    @FXML
    public void comboBoxWasUpdated8()
    {
        this.Color2.setPromptText(Color2.getValue());
    }
    @FXML
    public void comboBoxWasUpdated9()
    {
        this.Color3.setPromptText(Color3.getValue());
    }
    @FXML
    public void comboBoxWasUpdated10()
    {
        this.Color4.setPromptText(Color4.getValue());
    }
    @FXML
    public void comboBoxWasUpdated11()
    {
        this.Color5.setPromptText(Color5.getValue());
    }
    @FXML
    public void comboBoxWasUpdated12()
    {
        this.Color6.setPromptText(Color6.getValue());
    }

}
