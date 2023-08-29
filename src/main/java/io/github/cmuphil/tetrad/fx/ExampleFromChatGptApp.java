package io.github.cmuphil.tetrad.fx;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * JavaFX App
 */
public class ExampleFromChatGptApp extends Application {

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) {
        Scene scene = ExampleFromChatGpt.getInstance().getScene();
        stage.setScene(scene);
        stage.show();
    }

}
