package io.github.cmuphil.tetradfx;


import io.github.cmuphil.tetradfx.ui.TetradFxMain;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.WindowEvent;
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

        primaryStage.fullScreenProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                Platform.runLater(() -> {
                    if (primaryStage.isFullScreen()) {
                        primaryStage.setFullScreen(false);
                        primaryStage.setMaximized(true);

                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Information Dialog");
                        alert.setHeaderText(null); // You can set a header text or keep it null
                        alert.setContentText("Full screen mode is not supported yet--it messes with our dialogs on a Mac--" +
                                "but we have maximized your window for you.");
                        alert.showAndWait();
                    }
                });
            }
        });

        Scene scene = new Scene(TetradFxMain.getInstance().getRoot(primaryStage));
        String alwaysShowCloseButton = "-fx-visible: true";
        scene.getStylesheets().add(alwaysShowCloseButton);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Tetrad-FX");
        primaryStage.show();
    }
}
