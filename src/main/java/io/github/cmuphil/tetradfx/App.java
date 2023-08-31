package io.github.cmuphil.tetradfx;

import io.github.cmuphil.tetradfx.ui.TetradFx;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Tetrad-fx app, an application for Tetrad in JavaFX. Evolving, new.
 *
 * <p>Getting a lot of advice for coding from ChatGPT: OpenAI. (2023). ChatGPT (August 3 Version)
 * [Large language model]. <a href="https://chat.openai.com">https://chat.openai.com</a></p>
 *
 * @author josephramsey
 */
public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        Scene scene = new Scene(TetradFx.getInstance().getRoot(primaryStage));
        primaryStage.setScene(scene);
        primaryStage.setTitle("ScrollPane with Pane");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
