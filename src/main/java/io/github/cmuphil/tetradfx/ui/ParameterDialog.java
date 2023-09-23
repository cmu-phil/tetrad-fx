package io.github.cmuphil.tetradfx.ui;

import edu.cmu.tetrad.util.ParamDescriptions;
import edu.cmu.tetrad.util.Parameters;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * A dialog to edit parameters.
 *
 * @author josephramsey
 */
public class ParameterDialog {
    private final Parameters parameters;
    private final List<String> myParams;

    /**
     * Constructs a new parameter dialog.
     *
     * @param parameters The Parameters object to edit.
     * @param params     The list of parameters to edit. These are keys in the Parameters object and must be defined in
     *                   the ParamDescriptions class.
     */
    public ParameterDialog(Parameters parameters, List<String> params) {
        this.parameters = parameters;
        this.myParams = params;
    }

    /**
     * Adds a field for an string parameter.
     * @param description The description of the parameter.
     * @param defaultValue The default value of the parameter.
     * @param grid The grid to add the field to.
     * @param row The row to add the field to.
     * @param editables The list of editable fields.
     */
    private static void addAlphanumericField(String description, String defaultValue, GridPane grid, int row, List<Object> editables) {
        TextField tf = new TextField(defaultValue);

        tf.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[a-zA-Z0-9]*")) {
                tf.setText(newValue.replaceAll("[^a-zA-Z0-9]", ""));
            }
        });

        grid.add(new Label(description), 0, row);
        grid.add(tf, 1, row);
        editables.add(tf);
    }

    /**
     * Shows the dialog and edits the parameters upon clicking the Apply button.
     */
    public void showExtendedInputDialog() {
        ParamDescriptions paramDescs = ParamDescriptions.getInstance();

        Dialog<Object[]> dialog = new Dialog<>();
        dialog.setTitle("Edit Parameters");
        dialog.setHeaderText("Please edit the parameters of this search.");

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.APPLY, ButtonType.CANCEL);

        int row = -1;

        // Alphanumeric fields
        List<Object> editables = new ArrayList<>();

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        for (String myParam : myParams) {
            ++row;

            Object o = paramDescs.get(myParam).getDefaultValue();
            String description = paramDescs.get(myParam).getShortDescription();

            if (o instanceof Integer) {
                int min = paramDescs.get(myParam).getLowerBoundInt();
                int max = paramDescs.get(myParam).getUpperBoundInt();
                addIntegerField(description, (Integer) o, min, max, grid, row, editables);
            } else if (o instanceof Double) {
                double min = paramDescs.get(myParam).getLowerBoundDouble();
                double max = paramDescs.get(myParam).getUpperBoundDouble();
                addRealField(description, (Double) o, min, max, grid, row, editables);
            } else if (o instanceof String) {
                addAlphanumericField(description, (String) o, grid, row, editables);
            } else if (o instanceof Boolean) {
                addChoiceField(description, (Boolean) o, grid, row, editables);
            }
        }

        dialog.setResizable(true);
        VBox vBox = new VBox(grid);
        ScrollPane scrollPane = new ScrollPane(vBox);
        dialogPane.setContent(scrollPane);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.APPLY) {
                Object[] results = new Object[editables.size()];

                for (int i = 0; i < myParams.size(); i++) {
                    Object o = paramDescs.get(myParams.get(i)).getDefaultValue();

                    if (o instanceof String) {
                        results[i] = ((TextField) editables.get(i)).getText();
                    } else if (o instanceof Integer) {
                        results[i] = Integer.parseInt(((TextField) editables.get(i)).getText());
                    } else if (o instanceof Double) {
                        results[i] = Double.parseDouble(((TextField) editables.get(i)).getText());
                    } else if (o instanceof Boolean) {
                        RadioButton selected = (RadioButton) ((ToggleGroup) editables.get(i)).getSelectedToggle();
                        results[i] = selected.getText().equals("Yes");
                    }
                }

                return results;
            }

            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            for (int i = 0; i < myParams.size(); i++) {
                String param = myParams.get(i);
                Object o = paramDescs.get(param).getDefaultValue();

                if (o instanceof String) {
                    parameters.set(param, (String) result[i]);
                } else if (o instanceof Integer) {
                    parameters.set(param, result[i]);
                } else if (o instanceof Double) {
                    parameters.set(param, result[i]);
                } else if (o instanceof Boolean) {
                    parameters.set(param, result[i]);
                }
            }

            for (String param : myParams) {
                System.out.println(param + "=" + parameters.get(param));
            }
        });
    }

    /**
     * Adds a field for an integer. The field is validated to ensure that the value is an integer and within the
     * specified range. If the value is outside the range, the default value is used. The user may enter or click in
     * another field to validate the input.
     * @param description The description of the parameter.
     * @param defaultValue The default value of the parameter.
     * @param min The minimum value of the parameter.
     * @param max The maximum value of the parameter.
     * @param grid The grid to add the field to.
     * @param row The row to add the field to.
     * @param editables The list of editable fields.
     */
    private void addIntegerField(String description, int defaultValue, int min, int max, GridPane grid, int row, List<Object> editables) {
        TextField tf = new TextField(String.valueOf(defaultValue));

        tf.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("-?\\d*")) {
                tf.setText(oldValue);
            }
        });

        tf.focusedProperty().addListener((observable, hadFocus, hasFocus) -> {
            if (hadFocus && !hasFocus) {
                validateIntegerInput(tf, min, max, defaultValue);
            }
        });

        tf.setOnAction(e -> validateIntegerInput(tf, min, max, defaultValue));

        String minString = Integer.MIN_VALUE == min ? "-∞" : String.valueOf(min);
        String maxString = Integer.MAX_VALUE == max ? "∞" : String.valueOf(max);

        grid.add(new Label(description + " (" + minString + "-" + maxString + "):"), 0, row);
        grid.add(tf, 1, row);
        editables.add(tf);
    }

    /**
     * Adds a field for a real number. The field is validated to ensure that the value is a real number and
     * within the specified range. If the value is outside the range, the default value is used. The user may
     * enter or click in another field to validate the input.
     * @param description The description of the parameter.
     * @param defaultValue The default value of the parameter.
     * @param min The minimum value of the parameter.
     * @param max The maximum value of the parameter.
     * @param grid The grid to add the field to.
     * @param row The row to add the field to.
     * @param editables The list of editable fields.
     */
    private void addRealField(String description, double defaultValue, double min, double max, GridPane grid, int row, List<Object> editables) {
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

        String minString = Double.NEGATIVE_INFINITY == min ? "-∞" : String.valueOf(min);
        String maxString = Double.POSITIVE_INFINITY == max ? "∞" : String.valueOf(max);

        grid.add(new Label(description + " (" + minString + "-" + maxString + "):"), 0, row);
        grid.add(tf, 1, row);
        editables.add(tf);
    }

    /**
     * Adds a field for a binary Yes/No choice.
     *
     * @param description  The description of the parameter.
     * @param defaultValue The default value of the parameter.
     * @param grid         The grid to add the field to.
     * @param row          The row to add the field to.
     * @param editables    The list of editable fields.
     */
    private static void addChoiceField(String description, boolean defaultValue, GridPane grid, int row, List<Object> editables) {
        ToggleGroup group = new ToggleGroup();
        RadioButton yesButton = new RadioButton("Yes");
        RadioButton noButton = new RadioButton("No");
        yesButton.setToggleGroup(group);
        noButton.setToggleGroup(group);
        yesButton.setSelected(defaultValue);

        HBox radioGroup = new HBox(10, yesButton, noButton);
        grid.add(new Label(description), 0, row);
        grid.add(radioGroup, 1, row);
        editables.add(group);
    }

    /**
     * Validates the input of an integer field. If the input is not an integer or is outside the specified range, the
     * default value is used.
     * @param tf The text field to validate.
     * @param min The minimum value of the parameter.
     * @param max The maximum value of the parameter.
     * @param defaultValue The default value of the parameter.
     */
    private void validateIntegerInput(TextField tf, int min, int max, int defaultValue) {
        try {
            int value = Integer.parseInt(tf.getText());
            if (value < min || value > max) {
                tf.setText(String.valueOf(defaultValue));
            }
        } catch (NumberFormatException e) {
            tf.setText(String.valueOf(defaultValue));
        }
    }

    /**
     * Validates the input of a real number field. If the input is not a real number or is outside the specified range,
     * the default value is used.
     * @param tf The text field to validate.
     * @param min The minimum value of the parameter.
     * @param max The maximum value of the parameter.
     * @param defaultValue The default value of the parameter.
     */
    private void validateRealInput(TextField tf, double min, double max, double defaultValue) {
        try {
            double value = Double.parseDouble(tf.getText());
            if (value < min || value > max) {
                tf.setText(String.valueOf(defaultValue));
            }
        } catch (NumberFormatException e) {
            tf.setText(String.valueOf(defaultValue));
        }
    }
}





