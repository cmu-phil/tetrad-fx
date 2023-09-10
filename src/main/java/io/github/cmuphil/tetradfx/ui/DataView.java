package io.github.cmuphil.tetradfx.ui;

import edu.cmu.tetrad.algcomparison.algorithm.Algorithm;
import edu.cmu.tetrad.algcomparison.algorithm.oracle.cpdag.*;
import edu.cmu.tetrad.algcomparison.algorithm.oracle.pag.Bfci;
import edu.cmu.tetrad.algcomparison.algorithm.oracle.pag.Fci;
import edu.cmu.tetrad.algcomparison.algorithm.oracle.pag.Gfci;
import edu.cmu.tetrad.algcomparison.algorithm.oracle.pag.GraspFci;
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
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.util.Parameters;
import io.github.cmuphil.tetradfx.stufffor751lib.DataTransforms;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Displays a dataset in a table. Not much to see here; JavaFX's TableView does all the work,
 * though we are wrapping the Tetrad DataSet in a pretty efficient way here so that it will scale to thousands of
 * variables.</p>
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

        ContextMenu contextMenu = getContextMenu(table, dataSet);

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
    public static List<MenuItem> getMenuItems(DataSet dataSet, List<Algorithm> algorithms) {
        List<MenuItem> items = new ArrayList<>();

        for (Algorithm algorithm : algorithms) {
            MenuItem item = new MenuItem(algorithm.getDescription());
            item.setOnAction(e -> {
                Graph graph = algorithm.search(dataSet, new Parameters());
                NamesToContents.getInstance().getSelected().addGraph(algorithm.getClass().getSimpleName(),
                        graph, true);
            });

            items.add(item);
        }

        return items;
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
        ContextMenu contextMenu = new ContextMenu();
        Menu layout = new Menu("Run a search algorithm on this dataset");

        List<Algorithm> algorithms = new ArrayList<>();
        ScoreWrapper score = getScore(dataSet);

        algorithms.add(new Boss(score));
        IndependenceWrapper test = getTest(dataSet);
        algorithms.add(new Grasp(test, score));
        algorithms.add(new Pc(test));
        algorithms.add(new Fges(score));
        algorithms.add(new Cpc(test));

        algorithms.add(new Fci(test));
        algorithms.add(new Gfci(test, score));
        algorithms.add(new Bfci(test, score));
        algorithms.add(new GraspFci(test, score));

        List<MenuItem> items = getMenuItems(dataSet, algorithms);

        layout.getItems().addAll(items);
        contextMenu.getItems().addAll(layout);

        pane.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY ||
                    (event.getButton() == MouseButton.PRIMARY && event.isControlDown())) {
                contextMenu.show(pane, event.getScreenX(), event.getScreenY());
            }
        });

        Menu transformData = new Menu("Transform Data");
        contextMenu.getItems().add(transformData);

        MenuItem removeConstantColumns = new MenuItem("Remove Constant Columns");
        removeConstantColumns.setOnAction(e -> {
            DataSet filtered = DataTransforms.removeConstantColumns(dataSet);
            NamesToContents.getInstance().getSelected().addDataSet("Remove Constant Columns", filtered, true);
        });
        transformData.getItems().add(removeConstantColumns);

        MenuItem removeDuplicateColumns = new MenuItem("Remove Duplicate Columns");
        removeDuplicateColumns.setOnAction(e -> {
            DataSet filtered = DataTransforms.removeConstantColumns(dataSet);
            NamesToContents.getInstance().getSelected().addDataSet("Remove Constant Columns", filtered, true);
        });
        transformData.getItems().add(removeDuplicateColumns);

        MenuItem center = new MenuItem("Center");
        center.setOnAction(e -> {
            DataSet filtered = DataTransforms.center(dataSet);
            NamesToContents.getInstance().getSelected().addDataSet("Center", filtered, true);
        });
        transformData.getItems().add(center);

        MenuItem standardize = new MenuItem("Standardize");
        standardize.setOnAction(e -> {
            DataSet filtered = DataTransforms.standardizeData(dataSet);
            NamesToContents.getInstance().getSelected().addDataSet("Standardize", filtered, true);
        });
        transformData.getItems().add(standardize);

        MenuItem nonparanormalTransform = new MenuItem("Nonparanormal Transform");
        nonparanormalTransform.setOnAction(e -> {
            DataSet filtered = DataTransforms.getNonparanormalTransformed(dataSet);
            NamesToContents.getInstance().getSelected().addDataSet("Nonparanormal Transform", filtered, true);
        });
        transformData.getItems().add(nonparanormalTransform);

        MenuItem logTransform = new MenuItem("Log Transform");
        logTransform.setOnAction(e -> {
            DataSet filtered = DataTransforms.logData(dataSet, 10, false, 2);
            NamesToContents.getInstance().getSelected().addDataSet("Log Transform", filtered, true);
        });
        transformData.getItems().add(logTransform);

        MenuItem numericalDiscreteToContinuous = new MenuItem("Numerical Discrete to Continuous");
        numericalDiscreteToContinuous.setOnAction(e -> {
            DataSet filtered = DataTransforms.convertNumericalDiscreteToContinuous(dataSet);
            NamesToContents.getInstance().getSelected().addDataSet("Numerical Discrete to Continuous", filtered, true);
        });
        transformData.getItems().add(numericalDiscreteToContinuous);

        MenuItem discretizeToBinary = new MenuItem("Discretize to Binary");
        discretizeToBinary.setOnAction(e -> {
            DataSet filtered = DataTransforms.discretize(dataSet, 2, true);
            NamesToContents.getInstance().getSelected().addDataSet("Numerical Discrete to Binary", filtered, true);
        });
        transformData.getItems().add(discretizeToBinary);

        MenuItem discretize = new MenuItem("Discretize to Trinary");
        discretize.setOnAction(e -> {
            DataSet filtered = DataTransforms.discretize(dataSet, 3, true);
            NamesToContents.getInstance().getSelected().addDataSet("Numerical Discrete to Binary", filtered, true);
        });
        transformData.getItems().add(discretize);

        MenuItem restrictToMeasured = new MenuItem("Restrict to Measured");
        restrictToMeasured.setOnAction(e -> {
            DataSet filtered = DataTransforms.restrictToMeasured(dataSet);
            NamesToContents.getInstance().getSelected().addDataSet("Restrict to Measured", filtered, true);
        });
        transformData.getItems().add(restrictToMeasured);

        MenuItem shuffleColumns = new MenuItem("Shuffle Columns");
        shuffleColumns.setOnAction(e -> {
            DataSet filtered = DataTransforms.shuffleColumns(dataSet);
            NamesToContents.getInstance().getSelected().addDataSet("Shuffle Columns", filtered, true);
        });
        transformData.getItems().add(shuffleColumns);

        Menu makeModel = new Menu("Make Model");
        contextMenu.getItems().add(makeModel);

        Menu saveData = new Menu("Save Data");
        contextMenu.getItems().add(saveData);

        MenuItem games = new MenuItem("Base Games on this Dataset!");

        games.setOnAction(e -> {
            Games.baseGamesOnDataset();
        });


        contextMenu.getItems().add(games);


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


