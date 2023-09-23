package io.github.cmuphil.tetradfx;

import edu.cmu.tetrad.util.ParamDescription;
import edu.cmu.tetrad.util.ParamDescriptions;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
        edu.cmu.tetrad.util.Parameters parameters = new edu.cmu.tetrad.util.Parameters();
        ParamDescriptions paramDescs = ParamDescriptions.getInstance();
        Set<String> params = paramDescs.getNames();
        ParamDescription alphparameter = paramDescs.get("alpha");
        Object _default = alphparameter.getDefaultValue();

        if (_default instanceof Double) {
            double min = alphparameter.getLowerBoundDouble();
            double max = alphparameter.getUpperBoundDouble();
        }


        Dialog<Object[]> dialog = new Dialog<>();
        dialog.setTitle("Enter Data");
        dialog.setHeaderText("Please enter the required values.");

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.APPLY, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        int row = -1;

        // Alphanumeric fields
        List<Object> _parameters = new ArrayList<>();
        List<Object> _defaults = new ArrayList<>();
        List<Object> editables = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            ++row;

            _parameters.add("Alphanumeric " + row + ":");
            _defaults.add("Default");

            TextField tf = new TextField("");

            tf.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("[a-zA-Z0-9]*")) {  // Check to ensure only alphanumeric characters
                    tf.setText(newValue.replaceAll("[^a-zA-Z0-9]", ""));
                }
            });

            grid.add(new Label((String) _parameters.get(row)), 0, row);  // Adjusting the row index for layout
            grid.add(tf, 1, row);
            editables.add(tf);
        }

        for (int i = 0; i < 2; i++) {
            ++row;

            _parameters.add("Integer " + row + ":");
            _defaults.add(5);

            int min = 2;
            int max = 5;
            int defaultValue = (Integer) _defaults.get(row);

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

            grid.add(new Label("Integer " + row + " (" + min + "-" + max + "):"), 0, row);
            grid.add(tf, 1, row);
            editables.add(tf);
        }

        // Real-valued fields with user-defined bounds
        for (int i = 0; i < 2; i++) {
            ++row;

            double min = 1;
            double max = 5;
            _defaults.add(5.0);
            double defaultValue = (Double) _defaults.get(row);

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

            grid.add(new Label("Real Value " + row + " (" + min + "-" + max + "):"), 0, row);
            grid.add(tf, 1, row);
            editables.add(tf);
        }

        // Radio buttons for Yes/No choice
        for (int i = 0; i < 2; i++) {
            ++row;

            _defaults.add(true);
            boolean defaultValue = (Boolean) _defaults.get(row);

            ToggleGroup group = new ToggleGroup();
            RadioButton yesButton = new RadioButton("Yes");
            RadioButton noButton = new RadioButton("No");
            yesButton.setToggleGroup(group);
            noButton.setToggleGroup(group);
            yesButton.setSelected(defaultValue);  // default value

            HBox radioGroup = new HBox(10, yesButton, noButton);
            grid.add(new Label("Choice " + row + ":"), 0, row);
            grid.add(radioGroup, 1, row);
            editables.add(group);
        }

        dialog.setResizable(true);
        VBox vBox = new VBox(grid);
        ScrollPane scrollPane = new ScrollPane(vBox);
        dialogPane.setContent(scrollPane);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.APPLY) {
                String[] results = new String[2];

                for (int i = 0; i < editables.size(); i++) {
                    if (editables.get(i) instanceof TextField) {
                        results[i] = ((TextField) editables.get(i)).getText();
                    } else if (editables.get(i) instanceof ToggleGroup) {
                        RadioButton selected = (RadioButton) ((ToggleGroup) editables.get(i)).getSelectedToggle();
                        results[i] = selected.getText();
                    }
                }

                return results;
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





