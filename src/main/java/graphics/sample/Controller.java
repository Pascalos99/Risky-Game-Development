package graphics.sample;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXSlider;
import com.jfoenix.controls.JFXTextField;

import game.GamePanel;
import game.GameSetup;
import game.events.GameEvent;
import gamerules.Board;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ColorPicker;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import javax.swing.event.ChangeEvent;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import java.io.IOException;


public class Controller implements Initializable {

    public JFXSlider slider1;
    public JFXSlider slider6;
    public JFXSlider slider5;
    public JFXSlider slider4;
    public JFXSlider slider3;
    public JFXSlider slider2;

    static class PlayerSelect {
		public String name = null;
		public String type = null;
		public Color color = null;
        public boolean isComplete() {
			return name!= null && !name.matches("[\\s\\v\\h ]*") && type != null && color != null; }
	}

    public static GamePanel gamePanel;

	private PlayerSelect[] players = {new PlayerSelect(), new PlayerSelect(), new PlayerSelect(), new PlayerSelect(), new PlayerSelect(), new PlayerSelect()};
	
    public static Stage newStage = new Stage();
    public static Stage gameStage = new Stage();
    public JFXButton PlayBtn;
    public JFXButton GameRulesBtn;
    public JFXButton SetNameBtn;
    public String[] names = new String[]{
    		"Daniel Pink","Alex Pentland","Eric Schmidt","Donald Trump","Neil deGrasse Tyson","Bill Gates",
    		"Barrack Obama", "Mark Rutte", "Alan Turing", "Charles Babbage"};

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
    
    private List<JFXTextField> namefields;
    
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
    
    private List<JFXComboBox<String>> comboboxes;
    
    @FXML
    private ColorPicker color1;
    @FXML
    private ColorPicker color2;
    @FXML
    private ColorPicker color3;
    @FXML
    private ColorPicker color4;
    @FXML
    private ColorPicker color5;
    @FXML
    private ColorPicker color6;
    
    public int selected_graphics = GameSetup.REALISTIC_BOARD_GRAPHICS;

    private List<ColorPicker> colorpickers;
    
    @FXML
    private Canvas boardPreview;
    private GraphicsContext bp_graphics;

    private void setListener(JFXTextField field, int id) {
    	field.textProperty().addListener((observable, oldValue, newValue) -> {
    		updatePlayers();
    	});
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    	
    	namefields = List.of(name1, name2, name3, name4, name5, name6);
    	comboboxes = List.of(combo1, combo2, combo3, combo4, combo5, combo6);
    	colorpickers = List.of(color1, color2, color3, color4, color5, color6);
    	
        ObservableList<String> list = FXCollections.observableArrayList(GameSetup.player_type_names);
        for (JFXComboBox<String> b : comboboxes) b.setItems(list);
        for (ColorPicker p : colorpickers) p.setBackground(Background.EMPTY);
        for (int i=0; i < namefields.size(); i++) setListener(namefields.get(i), i);
        
        color1.setValue(Color.RED);
        color2.setValue(Color.GREEN);
        color3.setValue(Color.BLACK);
        color4.setValue(Color.WHITE);
        color5.setValue(Color.BLUE);
        color6.setValue(Color.YELLOW);
        
        Image image = new Image(AssetFinder.getResource("circle-cropped-2.png").toString());
        bp_graphics = boardPreview.getGraphicsContext2D();
        bp_graphics.drawImage(image, 0, 0, boardPreview.getWidth(), boardPreview.getHeight());

        updatePlayers();
    }
    
    private GameSetup getGameSetup() {
    	GameSetup gs = new GameSetup();
    	gs.setBoardGraphics(selected_graphics);
    	for (PlayerSelect ps : players)
    		if (ps.isComplete()) gs.addPlayer(ps.type, ps.name,
    				new java.awt.Color((int)(255 * ps.color.getRed()), (int)(255 * ps.color.getGreen()), (int)(255 * ps.color.getBlue())));
    	return gs;
    }

    @FXML
    private void Play() throws IOException{
    	updatePlayers();
    	GameSetup gs = getGameSetup();
    	if (gs.playerCount() == 0) { System.err.println("Starting graphical debug mode"); }
    	else if (!gs.hasValidPlayerCount()) { System.err.println("Must have at least 2 players and an even number of players to play"); return; }

    	GameEvent.clearAll();
		GamePanel result = gs.build();
		gamePanel = result;

        Parent root = FXMLLoader.load(AssetFinder.getResource("GameScene.fxml"));
        gameStage.setScene(new Scene(root, 800, 600));
        Main.mainStage.hide();
        gameStage.showAndWait();

    	System.out.println("Created Game");
    }

    @FXML
    private void Rules(ActionEvent event) throws IOException{
        StageChanger();
        Main.mainStage.hide();
        newStage.showAndWait();
    }

    public void StageChanger() throws IOException {
        Parent type2view = FXMLLoader.load(AssetFinder.getResource("GameRule.fxml"));
        Scene type2ViewScene = new Scene(type2view);
        newStage.setScene(type2ViewScene);
        newStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                newStage.close();
                Main.mainStage.show();
            }
        });
    }
    @FXML
    public void updatePlayers() {
    	boolean preview_needs_update = false;
    	for (int i=0; i < players.length; i++) {
    		Color old_color = players[i].color;
    		boolean was_valid = players[i].isComplete();

    		players[i].name = namefields.get(i).getText();
    		players[i].type = comboboxes.get(i).getValue();
    		players[i].color = colorpickers.get(i).getValue();

    		if (!players[i].color.equals(old_color)) preview_needs_update = true;
    		if (was_valid != players[i].isComplete()) preview_needs_update = true;
    	}
    	if (preview_needs_update) updatePreview();
    }

    public void updatePreview() {
    	if (bp_graphics == null) return;
    	Board.preview_settings = true;
    	GraphicsContext g = bp_graphics;
    	g.clearRect(0, 0, boardPreview.getWidth(), boardPreview.getHeight());
    	GameSetup trial = getGameSetup();
    	Image img = SwingFXUtils.toFXImage(trial.getGraphics(trial.getBoard()).getImage(), null);
    	g.drawImage(img, 0, 0, boardPreview.getWidth(), boardPreview.getHeight());
    	Board.preview_settings = false;
    }

    public void SetNames(ActionEvent actionEvent) {

        for(int i=0;i<6;i++){
            int num = (int) (Math.round(Math.random()*5));
            namefields.get(i).setPromptText(names[num]);
            namefields.get(i).setText(names[num]);
        }
    }

    public void slider1(MouseEvent mouseEvent) {
        combo1.setValue(setSliders(slider1));
    }

    public void slider6(MouseEvent mouseEvent) {
        combo6.setValue(setSliders(slider6));
    }

    public void slider5(MouseEvent mouseEvent) {
        combo5.setValue(setSliders(slider5));
    }

    public void slider3(MouseEvent mouseEvent) {
        combo3.setValue(setSliders(slider3));
    }

    public void slider2(MouseEvent mouseEvent) {
        combo2.setValue(setSliders(slider2));
    }

    public void slider4(MouseEvent mouseEvent) {
        combo4.setValue(setSliders(slider4));
    }

    public String setSliders(JFXSlider slider){

        return combo1.getItems().get((int)slider.getValue());
    }
}
