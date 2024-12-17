package ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class mainMenuController {
    @FXML
    private Label welcomeText;

    @FXML
    public void onPlayButtonClick(ActionEvent event) throws IOException {
        // Load the game interface (second window)
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("inGame-view.fxml"));
        Scene gameScene = new Scene(fxmlLoader.load());

        // Get the current stage and set the new scene
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.setScene(gameScene);
        stage.setTitle("Game Interface");
        stage.show();
    }
}
