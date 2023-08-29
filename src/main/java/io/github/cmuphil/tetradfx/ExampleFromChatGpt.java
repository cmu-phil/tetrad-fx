package io.github.cmuphil.tetradfx;

import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.RandomGraph;
import edu.cmu.tetrad.sem.LargeScaleSimulation;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

public class ExampleFromChatGpt {
    private static final ExampleFromChatGpt INSTANCE = new ExampleFromChatGpt();

    public static ExampleFromChatGpt getInstance() {
        return ExampleFromChatGpt.INSTANCE;
    }

    public Scene getScene() {
        Graph graph = RandomGraph.randomGraphRandomForwardEdges(1000, 0, 1000,
                100, 100, 1000, false);

        LargeScaleSimulation scaleSimulation = new LargeScaleSimulation(graph);
        DataSet dataSet = scaleSimulation.simulateDataFisher(1000);

        System.out.println("Simuilation done");

        double[][] matrix = dataSet.getDoubleData().toArray();

        TableView<ObservableList<DoubleProperty>> table = new TableView<>();

        for (double[] row : matrix) {
            ObservableList<DoubleProperty> observableRow = FXCollections.observableArrayList();
            for (double value : row) {
                observableRow.add(new SimpleDoubleProperty(Math.round(value * 10000.0) / 10000.0));
            }
            table.getItems().add(observableRow);
        }

        // Create columns for the TableView
        for (int j = 0; j < matrix[0].length; j++) {
            final int colIndex = j;
            TableColumn<ObservableList<DoubleProperty>, Number> column = new TableColumn<>(dataSet.getVariable(colIndex).getName());
            column.setStyle("-fx-alignment: CENTER-RIGHT;");
            column.setCellValueFactory(cellDataFeatures -> cellDataFeatures.getValue().get(colIndex));
            table.getColumns().add(column);
        }

        table.setPrefHeight(900);
        table.setPrefWidth(800);
        VBox vbox = new VBox(table);

        return new Scene(vbox, 900, 800);
    }
}


