package io.github.cmuphil.tetradfx;


import io.github.cmuphil.tetradfx.ui.TetradFxMain;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.net.ServerSocket;
import java.util.Objects;

/**
 * Tetrad-fx app, an application for Tetrad in JavaFX. Evolving.
 *
 * <p>Getting a lot of advice for coding from ChatGPT: OpenAI. (2023). ChatGPT (August 3 Version)
 * [Large language model]. <a href="https://chat.openai.com">https://chat.openai.com</a></p>
 *
 * @author josephramsey
 */
public class App extends Application {

    private static final int PORT = 65432;
    private static ServerSocket socket;
    private static Stage mainStage;

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

        try {
            socket = new ServerSocket(PORT);
            mainStage = primaryStage;

            Scene scene = new Scene(TetradFxMain.getInstance().getRoot(primaryStage));
            primaryStage.setScene(scene);
            primaryStage.setTitle("Tetrad-FX");
            primaryStage.show();
        } catch (Exception ex) {
            ex.printStackTrace();

            // If another instance is running, focus on that instance
            if (mainStage != null) {
                Platform.runLater(() -> {
                    mainStage.toFront();
                });
            }
            System.out.println("Application is already running.");
            Platform.exit();
        }

//        Scene scene = new Scene(TetradFxMain.getInstance().getRoot(primaryStage));
////        String alwaysShowCloseButton = "-fx-visible: true";
////        scene.getStylesheets().add(alwaysShowCloseButton);
//        primaryStage.setScene(scene);
//        primaryStage.setTitle("Tetrad-FX");
//        primaryStage.show();
    }
}
