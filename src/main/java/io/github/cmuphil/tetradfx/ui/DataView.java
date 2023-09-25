package io.github.cmuphil.tetradfx.ui;

import edu.cmu.tetrad.algcomparison.independence.ChiSquare;
import edu.cmu.tetrad.algcomparison.independence.ConditionalGaussianLRT;
import edu.cmu.tetrad.algcomparison.independence.FisherZ;
import edu.cmu.tetrad.algcomparison.independence.IndependenceWrapper;
import edu.cmu.tetrad.algcomparison.score.BdeuScore;
import edu.cmu.tetrad.algcomparison.score.ConditionalGaussianBicScore;
import edu.cmu.tetrad.algcomparison.score.ScoreWrapper;
import edu.cmu.tetrad.algcomparison.score.SemBicScore;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.DiscreteVariable;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import org.jetbrains.annotations.NotNull;

/**
 * <p>Returns a TableView for a DataSet that can be displayed in a ScrollPane</p>
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

        var contextMenu = MenuItems.getDataContextMenu(table, dataSet);

        // Show context menu on right-click on the label
        table.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.SECONDARY ||
                    (event.getButton() == MouseButton.PRIMARY && event.isControlDown())) {
                contextMenu.show(table, event.getScreenX(), event.getScreenY());
            }
        });

        table.setPrefHeight(300);
        table.setPrefWidth(400);
        table.setSelectionModel(null);
        return table;
    }

    /**
     * <p>A row in the table. This is a wrapper around the Tetrad DataSet that allows us to
     * display the data in a TableView without having to copy the data into a new data structure.</p>
     */
    public static class DataRow {
        private final int row;
        private final DataSet dataSet;

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

    @NotNull
    public static ScoreWrapper getScore(DataSet dataSet) {
        if (dataSet.isContinuous()) {
            return new SemBicScore();
        } else if (dataSet.isDiscrete()) {
            return new BdeuScore();
        } else {
            return new ConditionalGaussianBicScore();
        }
    }

    public static IndependenceWrapper getTest(DataSet dataSet) {
        if (dataSet.isContinuous()) {
            return new FisherZ();
        } else if (dataSet.isDiscrete()) {
            return new ChiSquare();
        } else {
            return new ConditionalGaussianLRT();
        }
    }
}


