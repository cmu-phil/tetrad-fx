package io.github.cmuphil.tetradfx;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

// This is an example of how to create a "dialog" without using the JavaFX Dialog class.
// The Dialog class is buggy in full screen mode, so we need to create our own dialog.
public class Example1 extends Application {

    @Override
    public void start(Stage primaryStage) {
        Button showDialogButton = new Button("Show Dialog");
        StackPane root = new StackPane();

        showDialogButton.setOnAction(event -> {
            VBox dialog = createDialog(root);
            root.getChildren().add(dialog);
        });

        root.getChildren().add(showDialogButton);

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
//        primaryStage.setFullScreen(true);
        primaryStage.show();
    }

    private VBox createDialog(StackPane root) {
        VBox dialog = new VBox(20);
        dialog.setAlignment(Pos.CENTER);

        Label message = new Label("This is a dialog.");
        Button closeDialogButton = new Button("Close Dialog");

        closeDialogButton.setOnAction(event -> {
            root.getChildren().remove(dialog);
        });

        dialog.getChildren().addAll(message, closeDialogButton);
        dialog.setStyle("-fx-padding: 20; -fx-background-color: white; -fx-border-color: black;");
        StackPane.setAlignment(dialog, Pos.CENTER);

        return dialog;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
