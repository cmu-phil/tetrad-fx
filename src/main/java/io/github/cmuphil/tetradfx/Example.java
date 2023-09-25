package io.github.cmuphil.tetradfx;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

public class Example extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        TextArea inputArea = new TextArea();
        inputArea.setPromptText("Enter variable names here, separated by new lines...");
        root.getChildren().add(inputArea);

        TextArea unmatchedVarsArea = new TextArea();
        unmatchedVarsArea.setEditable(false);  // Ensure users cannot edit content

        List<TextArea> displayAreas = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            HBox hBox = new HBox(10);

            TextField regexField = new TextField();
            TextArea displayArea = new TextArea();
            displayAreas.add(displayArea);

            regexField.textProperty().addListener((observable, oldValue, newValue) -> updateDisplays(inputArea, displayAreas, unmatchedVarsArea));

            hBox.getChildren().addAll(regexField, displayArea);
            root.getChildren().add(hBox);
        }

        // New TextArea for unmatched variables
        root.getChildren().add(unmatchedVarsArea);

        inputArea.textProperty().addListener((observable, oldValue, newValue) -> updateDisplays(inputArea, displayAreas, unmatchedVarsArea));

        primaryStage.setScene(new Scene(root, 600, 600)); // Adjusted height to accommodate new TextArea
        primaryStage.setTitle("Regex Filter");
        primaryStage.show();
    }

    private void updateDisplays(TextArea inputArea, List<TextArea> displayAreas, TextArea unmatchedVarsArea) {
        List<String> variableNames = new ArrayList<>(List.of(inputArea.getText().split("\n")));
        for (TextArea displayArea : displayAreas) {
            displayArea.clear();
        }
        unmatchedVarsArea.clear();

        for (int i = 0; i < 3; i++) {
            TextArea displayArea = displayAreas.get(i);
            TextField regexField = (TextField) ((HBox) displayArea.getParent()).getChildren().get(0);

            Pattern pattern;
            try {
                pattern = Pattern.compile(regexField.getText());
            } catch (Exception e) {
                displayArea.setText("Invalid regex");
                continue;
            }

            Iterator<String> iterator = variableNames.iterator();
            while (iterator.hasNext()) {
                String varName = iterator.next();
                if (pattern.matcher(varName).find()) {
                    displayArea.appendText(varName + "\n");
                    iterator.remove();
                }
            }
        }

        // For any remaining unmatched variables
        for (String varName : variableNames) {
            unmatchedVarsArea.appendText(varName + "\n");
        }
    }
}


