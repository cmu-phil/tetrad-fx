package io.github.cmuphil.tetradfx;

import io.github.cmuphil.tetradfx.ui.ParameterDialog;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class ParameterEditorUiStarter extends Application {

    @Override
    public void start(Stage primaryStage) {
        edu.cmu.tetrad.util.Parameters parameters = new edu.cmu.tetrad.util.Parameters();

        List<String> myParams = new ArrayList<>();
        myParams.add("alpha");
        myParams.add("depth");

        Button showDialogButton = new Button("Show Dialog");
        showDialogButton.setOnAction(e -> new ParameterDialog(parameters, myParams).showExtendedInputDialog());

        VBox root = new VBox(10, showDialogButton);
        root.setPadding(new Insets(20));

        Scene scene = new Scene(root, 300, 200);
        primaryStage.setScene(scene);
        primaryStage.setTitle("JavaFX Extended Dialog Example");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}





