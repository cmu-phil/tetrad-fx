package io.github.cmuphil.tetradfx.ui;

import edu.cmu.tetrad.data.ContinuousVariable;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.DiscreteVariable;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.util.StatUtils;
import io.github.cmuphil.tetradfx.utils.VariableRow;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>Displays a list of variables in a table with ancillary information and fields the user can
 * fill in for describing the data.</p>
 *
 * @author josephramsey
 */
public class VariablesView {
    private TableView<VariableRow> tableView;

    /**
     * Creates a new variable view.
     * @param dataSet the dataset the variables are from.
     */
    public VariablesView(DataSet dataSet) {
        tableView = new TableView<>();

        if (dataSet == null) {
            return;
        }

        TableView<VariableRow> table = new TableView<>();

        TableColumn<VariableRow, String> variableName = new TableColumn<>("Variable Name");
        variableName.setCellValueFactory(new PropertyValueFactory<>("variableName"));

        TableColumn<VariableRow, String> variableType = new TableColumn<>("Variable Type");
        variableType.setCellValueFactory(new PropertyValueFactory<>("variableType"));

        TableColumn<VariableRow, String> statsCol = new TableColumn<>("Stats");
        statsCol.setCellFactory(param -> new TextAreaCell());
        statsCol.setCellValueFactory(new PropertyValueFactory<>("stats"));

        TableColumn<VariableRow, String> notesCol = new TableColumn<>("Notes");
        notesCol.setCellFactory(param -> new TextAreaCell());
        notesCol.setCellValueFactory(new PropertyValueFactory<>("notes"));

        table.getColumns().addAll(variableName, variableType, statsCol, notesCol);

        List<VariableRow> rows = new ArrayList<>();

        for (int i = 0; i < dataSet.getNumColumns(); i++) {
            Node variable = dataSet.getVariable(i);
            rows.add(new VariableRow(variable.getName(), variable instanceof DiscreteVariable ? "Discrete" : "Continuous",
                    getVariablestats(dataSet, variable)));

            ObservableList<VariableRow> data = FXCollections.observableArrayList(rows);

            table.setItems(data);
            this.tableView = table;
        }
    }

    /**
     * Gets the table view.
     * @return the table view.
     */
    public TableView<VariableRow> getTableView() {
        return tableView;
    }

    /**
     * Gets the table view.
     * @param dataSet the dataset the variables are from.
     * @param variable the variable to get stats for.
     * @return the table view.
     */
    private String getVariablestats(DataSet dataSet, edu.cmu.tetrad.graph.Node variable) {
        if (variable instanceof DiscreteVariable) {
            return getDiscreteVariablestats(dataSet, (DiscreteVariable) variable);
        } else if (variable instanceof ContinuousVariable) {
            return getContinuousVariablestats(dataSet, (ContinuousVariable) variable);
        } else {
            throw new IllegalArgumentException("Unknown variable type: " + variable.getClass().getSimpleName());
        }
    }

    /**
     * Gets the stats for a continuous variable.
     * @param dataSet the dataset the variables are from.
     * @param variable the variable to get stats for.
     * @return the stats for the variable.
     */
    private String getContinuousVariablestats(DataSet dataSet, ContinuousVariable variable) {
        int index = dataSet.getColumn(variable);

        double[] values = new double[dataSet.getNumRows()];
        for (int i = 0; i < dataSet.getNumRows(); i++) {
            values[i] = dataSet.getDouble(i, index);
        }

        int N = values.length;
        double min = StatUtils.min(values);
        double max = StatUtils.max(values);
        double mean = StatUtils.mean(values);
        double median = StatUtils.median(values);
        double skewness = StatUtils.skewness(values);
        double kurtosis = StatUtils.kurtosis(values) + 3;

        NumberFormat nf = new DecimalFormat("#.####");

        return "N = " + N + "\n" +
                "Min: " + nf.format(min) + "\n" +
                "Max: " + nf.format(max) + "\n" +
                "Mean: " + nf.format(mean) + "\n" +
                "Median: " + nf.format(median) + "\n" +
                "Skewness: " + nf.format(skewness) + "\n" +
                "Kurtosis: " + nf.format(kurtosis);
    }

    /**
     * Gets the stats for a discrete variable.
     * @param dataSet the dataset the variables are from.
     * @param variable the variable to get stats for.
     * @return the stats for the variable.
     */
    private String getDiscreteVariablestats(DataSet dataSet, DiscreteVariable variable) {
        List<String> categories = variable.getCategories();
        int index = dataSet.getColumn(variable);

        int[] counts = new int[categories.size()];
        for (int i = 0; i < dataSet.getNumRows(); i++) {
            counts[dataSet.getInt(i, index)]++;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("N = ").append(dataSet.getNumRows()).append("\n");

        for (int i = 0; i < categories.size(); i++) {
            sb.append(categories.get(i)).append(": ").append(counts[i]).append("\n");
        }

        return sb.toString();
    }

    /**
     * <p>A cell that displays a text area.</p>
     */
    public static class TextAreaCell extends TableCell<VariableRow, String> {
        private final TextArea textArea;

        public TextAreaCell() {
            textArea = new TextArea();
            textArea.setPrefSize(200, 100);
            textArea.setWrapText(true);
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setGraphic(null);
            } else {
                textArea.setText(item);
                setGraphic(textArea);
            }
        }
    }
}


