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
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Displays a dataset in a table. Not much to see here; JavaFX's TableView does all the work,
 * though we are wrapping the Tetrad DataSet in a pretty efficient way here so that it will
 * scale to thousands of variables.</p>
 *
 * @author josephramsey
 */
public class DataViewTabPane {

    @NotNull
    public static TableView<DataRow> getTableView(DataSet dataSet, TabPane tabs) {
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

        ContextMenu contextMenu = getContextMenu(table, dataSet, tabs);

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
     * display the data in a TableView without having to copy the data into a new data
     * structure.</p>
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
    private static ContextMenu getContextMenu(TableView<DataRow> pane, DataSet dataSet, TabPane tabs) {
        ContextMenu contextMenu = new ContextMenu();
        Menu layout = new Menu("Do a Search");

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

        List<MenuItem> items = getMenuItems(dataSet, tabs, algorithms);

        layout.getItems().addAll(items);
        contextMenu.getItems().addAll(layout);

        pane.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY ||
                    (event.getButton() == MouseButton.PRIMARY && event.isControlDown())) {
                contextMenu.show(pane, event.getScreenX(), event.getScreenY());
            }
        });

        return contextMenu;
    }

    @NotNull
    private static List<MenuItem> getMenuItems(DataSet dataSet, TabPane tabs, List<Algorithm> algorithms) {
        List<MenuItem> items = new ArrayList<>();

        for (Algorithm algorithm : algorithms) {
            MenuItem item = new MenuItem(algorithm.getDescription());
            item.setOnAction(e -> {
                Graph graph = algorithm.search(dataSet, new Parameters());
                ScrollPane graphScroll = GraphView.getGraphDisplay(graph);
                Tab tab = new Tab(algorithm.getDescription(), graphScroll);
                tabs.getTabs().add(tab);
                tabs.getSelectionModel().select(tab);
            });

            items.add(item);
        }
        return items;
    }

    @NotNull
    private static ScoreWrapper getScore(DataSet dataSet) {
        if (dataSet.isContinuous()) {
            return new SemBicScore();
        } else if (dataSet.isDiscrete()) {
            return new BdeuScore();
        } else {
            return new ConditionalGaussianBicScore();
        }
    }

    private static IndependenceWrapper getTest(DataSet dataSet) {
        if (dataSet.isContinuous()) {
            return new FisherZ();
        } else if (dataSet.isDiscrete()) {
            return new ChiSquare();
        } else {
            return new ConditionalGaussianLRT();
        }
    }
}


