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
import io.github.cmuphil.tetradfx.for751lib.DataTransforms;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import org.jetbrains.annotations.NotNull;

/**
 * <p>Displays a dataset in a table.</p>
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

        var contextMenu = getContextMenu(table, dataSet);

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

    @NotNull
    static ContextMenu getContextMenu(TableView<DataRow> pane, DataSet dataSet) {
        var contextMenu = new ContextMenu();

        if (dataSet == null) {
            return contextMenu;
        }

        pane.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY ||
                    (event.getButton() == MouseButton.PRIMARY && event.isControlDown())) {
                contextMenu.show(pane, event.getScreenX(), event.getScreenY());
            }
        });

        var transformData = new Menu("Transform Data");
        contextMenu.getItems().add(transformData);

        var removeConstantColumns = new MenuItem("Remove Constant Columns");
        removeConstantColumns.setOnAction(e -> {
            DataSet filtered = DataTransforms.removeConstantColumns(dataSet);
            Session.getInstance().getSelectedProject().addDataSet("Remove Constant Columns", filtered, true, true);
        });
        transformData.getItems().add(removeConstantColumns);

        var removeDuplicateColumns = new MenuItem("Remove Duplicate Columns");
        removeDuplicateColumns.setOnAction(e -> {
            DataSet filtered = DataTransforms.removeConstantColumns(dataSet);
            Session.getInstance().getSelectedProject().addDataSet("Remove Constant Columns", filtered, true, true);
        });
        transformData.getItems().add(removeDuplicateColumns);

        var center = new MenuItem("Center");
        center.setOnAction(e -> {
            DataSet filtered = DataTransforms.center(dataSet);
            Session.getInstance().getSelectedProject().addDataSet("Center", filtered, true, true);
        });
        transformData.getItems().add(center);

        var standardize = new MenuItem("Standardize");
        standardize.setOnAction(e -> {
            DataSet filtered = DataTransforms.standardizeData(dataSet);
            Session.getInstance().getSelectedProject().addDataSet("Standardize", filtered, true, true);
        });
        transformData.getItems().add(standardize);

        var nonparanormalTransform = new MenuItem("Nonparanormal Transform");
        nonparanormalTransform.setOnAction(e -> {
            DataSet filtered = DataTransforms.getNonparanormalTransformed(dataSet);
            Session.getInstance().getSelectedProject().addDataSet("Nonparanormal Transform", filtered, true, true);
        });
        transformData.getItems().add(nonparanormalTransform);

        var logTransform = new MenuItem("Log Transform");
        logTransform.setOnAction(e -> {
            DataSet filtered = DataTransforms.logData(dataSet, 10, false, 2);
            Session.getInstance().getSelectedProject().addDataSet("Log Transform", filtered, true, true);
        });
        transformData.getItems().add(logTransform);

        var numericalDiscreteToContinuous = new MenuItem("Numerical Discrete to Continuous");
        numericalDiscreteToContinuous.setOnAction(e -> {
            DataSet filtered = DataTransforms.convertNumericalDiscreteToContinuous(dataSet);
            Session.getInstance().getSelectedProject().addDataSet("Numerical Discrete to Continuous", filtered, true, true);
        });
        transformData.getItems().add(numericalDiscreteToContinuous);

        var discretizeToBinary = new MenuItem("Discretize to Binary");
        discretizeToBinary.setOnAction(e -> {
            DataSet filtered = DataTransforms.discretize(dataSet, 2, true);
            Session.getInstance().getSelectedProject().addDataSet("Discretize to Binary", filtered, true, true);
        });
        transformData.getItems().add(discretizeToBinary);

        var discretize = new MenuItem("Discretize to Trinary");
        discretize.setOnAction(e -> {
            DataSet filtered = DataTransforms.discretize(dataSet, 3, true);
            Session.getInstance().getSelectedProject().addDataSet("Discretize to Trinary", filtered, true, true);
        });
        transformData.getItems().add(discretize);

        var restrictToMeasured = new MenuItem("Restrict to Measured");
        restrictToMeasured.setOnAction(e -> {
            DataSet filtered = DataTransforms.restrictToMeasured(dataSet);
            Session.getInstance().getSelectedProject().addDataSet("Restrict to Measured", filtered, true, true);
        });
        transformData.getItems().add(restrictToMeasured);

        var shuffleColumns = new MenuItem("Shuffle Columns");
        shuffleColumns.setOnAction(e -> {
            DataSet filtered = DataTransforms.shuffleColumns(dataSet);
            Session.getInstance().getSelectedProject().addDataSet("Shuffle Columns", filtered, true, true);
        });
        transformData.getItems().add(shuffleColumns);

        var makeModel = new Menu("Make Model");
        contextMenu.getItems().add(makeModel);

        var saveData = new Menu("Save Data");
        contextMenu.getItems().add(saveData);

        return contextMenu;
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
}


