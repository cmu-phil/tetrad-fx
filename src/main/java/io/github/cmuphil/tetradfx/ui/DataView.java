package io.github.cmuphil.tetradfx.ui;

import edu.cmu.tetrad.algcomparison.algorithm.oracle.cpdag.Boss;
import edu.cmu.tetrad.algcomparison.algorithm.oracle.pag.Bfci;
import edu.cmu.tetrad.algcomparison.independence.*;
import edu.cmu.tetrad.algcomparison.score.*;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.DiscreteVariable;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.util.Parameters;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
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
    public static TableView<DataRow> getTableView(DataSet dataSet, TabPane tabbedPane) {
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

        ContextMenu contextMenu = getContextMenu(table, dataSet, tabbedPane);

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

    @NotNull
    private static ContextMenu getContextMenu(TableView pane, DataSet dataSet, TabPane tabbedPane) {
        // Create a context menu
        ContextMenu contextMenu = new ContextMenu();

        Menu layout = new Menu("Run a search");

        // Create menu items
        MenuItem item1 = new MenuItem("BOSS");

        item1.setOnAction(e -> {
            Boss boss = new Boss(getScore(dataSet));
            Graph graph = boss.search(dataSet, new Parameters());
            ScrollPane graphScroll = GraphView.getGraphDisplay(graph);
            Tab boss1 = new Tab("BOSS", graphScroll);
            tabbedPane.getTabs().add(boss1);
            tabbedPane.getSelectionModel().select(boss1);
        });

        MenuItem item2 = new MenuItem("BFCI");
        item2.setOnAction(e -> {
            Bfci fci = new Bfci(getTest(dataSet), getScore(dataSet));
            Graph graph = fci.search(dataSet, new Parameters());
            ScrollPane graphScroll = GraphView.getGraphDisplay(graph);
            Tab bfci = new Tab("BFCI", graphScroll);
            tabbedPane.getTabs().add(bfci);
            tabbedPane.getSelectionModel().select(bfci);
        });

        // Add menu items to the context menu
        layout.getItems().addAll(item1, item2);
        contextMenu.getItems().addAll(layout);

        // Show context menu on right-click on the pane
        pane.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY ||
                    (event.getButton() == MouseButton.PRIMARY && event.isControlDown())) {
                contextMenu.show(pane, event.getScreenX(), event.getScreenY());
            }
        });

        return contextMenu;
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


