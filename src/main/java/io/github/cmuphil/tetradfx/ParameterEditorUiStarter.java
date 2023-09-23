package io.github.cmuphil.tetradfx;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.regex.Pattern;

public class ParameterEditorUiStarter extends Application {

    // Define your bounds and default values here
    private static final double[] REAL_BOUNDS = {1.0, 10.0, 50.0, 100.0};
    private static final double[] REAL_DEFAULTS = {5.0, 60.0};
    private static final int[] INT_BOUNDS = {10, 20, 30, 40};
    private static final int[] INT_DEFAULTS = {15, 35};

    @Override
    public void start(Stage primaryStage) {
        Button showDialogButton = new Button("Show Dialog");
        showDialogButton.setOnAction(e -> showExtendedInputDialog());

        VBox root = new VBox(10, showDialogButton);
        root.setPadding(new Insets(20));

        Scene scene = new Scene(root, 300, 200);
        primaryStage.setScene(scene);
        primaryStage.setTitle("JavaFX Extended Dialog Example");
        primaryStage.show();
    }

    private void showExtendedInputDialog() {
        Dialog<Object[]> dialog = new Dialog<>();
        dialog.setTitle("Enter Data");
        dialog.setHeaderText("Please enter the required values.");

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.APPLY, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        // Alphanumeric fields
        TextField[] alphaNumericFields = new TextField[2];
        for (int i = 0; i < 2; i++) {
            TextField tf = new TextField(""); // Default to an empty string

            tf.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("[a-zA-Z0-9]*")) {  // Check to ensure only alphanumeric characters
                    tf.setText(newValue.replaceAll("[^a-zA-Z0-9]", ""));
                }
            });

            grid.add(new Label("Alphanumeric " + (i + 1) + ":"), 0, i + 6);  // Adjusting the row index for layout
            grid.add(tf, 1, i + 6);
            alphaNumericFields[i] = tf;
        }

        TextField[] integerFields = new TextField[2];
        for (int i = 0; i < 2; i++) {
            int min = INT_BOUNDS[i * 2];
            int max = INT_BOUNDS[i * 2 + 1];
            int defaultValue = INT_DEFAULTS[i];

            TextField tf = new TextField(String.valueOf(defaultValue));

            tf.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d*")) {
                    tf.setText(oldValue);
                }
            });

            tf.focusedProperty().addListener((observable, hadFocus, hasFocus) -> {
                if (hadFocus && !hasFocus) {
                    validateIntegerInput(tf, min, max, defaultValue);
                }
            });

            tf.setOnAction(e -> validateIntegerInput(tf, min, max, defaultValue));

            grid.add(new Label("Integer " + (i + 1) + " (" + min + "-" + max + "):"), 0, i);
            grid.add(tf, 1, i);
            integerFields[i] = tf;
        }

        // Real-valued fields with user-defined bounds
        TextField[] realFields = new TextField[2];
        for (int i = 0; i < 2; i++) {
            double min = REAL_BOUNDS[i * 2];
            double max = REAL_BOUNDS[i * 2 + 1];
            double defaultValue = REAL_DEFAULTS[i];

            TextField tf = new TextField(String.valueOf(defaultValue));

            Pattern realPattern = Pattern.compile("-?\\d*(\\.\\d*)?");

            tf.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!realPattern.matcher(newValue).matches()) {
                    tf.setText(oldValue);
                }
            });

            tf.focusedProperty().addListener((observable, hadFocus, hasFocus) -> {
                if (hadFocus && !hasFocus) {
                    validateRealInput(tf, min, max, defaultValue);
                }
            });

            tf.setOnAction(e -> validateRealInput(tf, min, max, defaultValue));

            grid.add(new Label("Real Value " + (i + 1) + " (" + min + "-" + max + "):"), 0, i + 2);
            grid.add(tf, 1, i + 2);
            realFields[i] = tf;
        }

        // Radio buttons for Yes/No choice
        ToggleGroup[] yesNoGroups = new ToggleGroup[2];
        for (int i = 0; i < 2; i++) {
            ToggleGroup group = new ToggleGroup();
            RadioButton yesButton = new RadioButton("Yes");
            RadioButton noButton = new RadioButton("No");
            yesButton.setToggleGroup(group);
            noButton.setToggleGroup(group);
            yesButton.setSelected(true);  // default value

            HBox radioGroup = new HBox(10, yesButton, noButton);
            grid.add(new Label("Choice " + (i + 1) + ":"), 0, i + 4);
            grid.add(radioGroup, 1, i + 4);
            yesNoGroups[i] = group;
        }

        dialogPane.setContent(grid);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.APPLY) {
                String[] alphanumericResults = new String[2];
                for (int i = 0; i < 2; i++) {
                    alphanumericResults[i] = alphaNumericFields[i].getText();
                }

                int[] intResults = new int[2];
                for (int i = 0; i < 2; i++) {
                    intResults[i] = Integer.parseInt(integerFields[i].getText());
                }

                double[] realResults = new double[2];
                for (int i = 0; i < 2; i++) {
                    realResults[i] = Double.parseDouble(realFields[i].getText());
                }

                String[] choices = new String[2];
                for (int i = 0; i < 2; i++) {
                    RadioButton selected = (RadioButton) yesNoGroups[i].getSelectedToggle();
                    choices[i] = selected.getText();
                }

                return new Object[]{alphanumericResults, intResults, realResults, choices};
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            String[] alphanumericResults = (String[]) result[0];
            int[] intResults = (int[]) result[1];
            double[] realResults = (double[]) result[2];
            String[] choices = (String[]) result[3];

            System.out.println("Alphanumeric Results:");
            for (String val : alphanumericResults) {
                System.out.println(val);
            }

            System.out.println("\nInteger Results:");
            for (int val : intResults) {
                System.out.println(val);
            }

            System.out.println("\nReal Value Results:");
            for (double val : realResults) {
                System.out.println(val);
            }

            System.out.println("\nChoices:");
            for (String choice : choices) {
                System.out.println(choice);
            }
        });
    }

    private void validateIntegerInput(TextField tf, int min, int max, int defaultValue) {
        try {
            int value = Integer.parseInt(tf.getText());
            if (value < min || value > max) {
                tf.setText(String.valueOf(defaultValue)); // or default value, or previous value, etc.
            }
        } catch (NumberFormatException e) {
            tf.setText(String.valueOf(defaultValue)); // or default value, or previous value, etc.
        }
    }

    private void validateRealInput(TextField tf, double min, double max, double defaultValue) {
        try {
            double value = Double.parseDouble(tf.getText());
            if (value < min || value > max) {
                tf.setText(String.valueOf(defaultValue)); // or default value, or previous value, etc.
            }
        } catch (NumberFormatException e) {
            tf.setText(String.valueOf(defaultValue)); // or default value, or previous value, etc.
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}





