package io.github.cmuphil.tetradfx.ui;

import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.DiscreteVariable;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.jetbrains.annotations.NotNull;

/**
 * <p>Displays a dataset in a table. Not much to see here; JavaFX's TableView does all the work,
 * though we are wrapping the Tetrad DataSet in a pretty efficient way here so that it will
 * scale to thousands of variables.</p>
 *
 * @author josephramsey
 */
public class DataView {

    @NotNull
    public static TableView<DataRow> getTableView(DataSet dataSet) {
        TableView<DataRow> table = new TableView<>();

        int numberOfColumns = dataSet.getNumColumns();

        for (int j = 0; j < numberOfColumns; j++) {
            final int colIndex = j;
            TableColumn<DataRow, String> column = new TableColumn<>(dataSet.getVariable(colIndex).getName());
            column.setCellValueFactory(cellData -> cellData.getValue().getValue(colIndex));
            column.setStyle("-fx-alignment: CENTER-RIGHT;");
            table.getColumns().add(column);
        }

        for (int i = 0; i < dataSet.getNumRows(); i++) {
            table.getItems().add(new DataRow(dataSet, i));
        }

        table.setPrefHeight(300);
        table.setPrefWidth(400);
        return table;
    }

    /**
     * <p>A row in the table. This is a wrapper around the Tetrad DataSet that allows us to
     * display the data in a TableView without having to copy the data into a new data
     * structure.</p>
     */
    public static class DataRow {
        private final int row;
        DataSet dataSet;

        public DataRow(DataSet dataSet, int row) {
            this.dataSet = dataSet;
            this.row = row;
        }

        public SimpleStringProperty getValue(int col) {
            if (dataSet.getVariable(col) instanceof edu.cmu.tetrad.data.ContinuousVariable) {
                double value = dataSet.getDouble(row, col);
                return new SimpleStringProperty(String.format("%.2f", value));
            } else {
                DiscreteVariable variable = (DiscreteVariable) dataSet.getVariable(col);
                return new SimpleStringProperty(variable.getCategory(dataSet.getInt(row, col)));
            }
        }
    }
}


