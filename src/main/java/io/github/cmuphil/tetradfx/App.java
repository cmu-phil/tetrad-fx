package io.github.cmuphil.tetradfx;

import io.github.cmuphil.tetradfx.ui.TetradFxMain;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import static io.github.cmuphil.tetradfx.for751lib.ChangedStuffINeed.unzipDirectory;

/**
 * Tetrad-fx app, an application for Tetrad in JavaFX. Evolving.
 *
 * <p>Getting a lot of advice for coding from ChatGPT: OpenAI. (2023). ChatGPT (August 3 Version)
 * [Large language model]. <a href="https://chat.openai.com">https://chat.openai.com</a></p>
 *
 * @author josephramsey
 */
public class App extends Application {

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/tyler32.png"))));

        Scene scene = new Scene(TetradFxMain.getInstance().getRoot(primaryStage));
        primaryStage.setScene(scene);
        primaryStage.setTitle("Tetrad-FX");
        primaryStage.show();
    }
}
