package io.github.cmuphil.tetradfx;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Example extends Application {
    @Override
    public void start(Stage primaryStage) {
        TextArea textArea = new TextArea();

        // Add MOUSE_EXITED event listener
        textArea.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_EXITED, event -> {
            System.out.println("Mouse exited TextArea!");
            // Perform any other action you want here
        });

        StackPane root = new StackPane();
        root.getChildren().add(textArea);

        Scene scene = new Scene(root, 300, 200);

        primaryStage.setTitle("Mouse Exit TextArea Example");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

