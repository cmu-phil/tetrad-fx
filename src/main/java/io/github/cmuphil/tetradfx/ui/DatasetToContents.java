package io.github.cmuphil.tetradfx.ui;

import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.RandomGraph;
import edu.cmu.tetrad.sem.LargeScaleSimulation;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps datasets to their contents.
 *
 * @author josephramsey
 */
public class DatasetToContents {
    private final Map<DataSet, Contents> datasetToContents = new HashMap<>();
    private final Map<String, DataSet> datasetNamesToDataset = new HashMap<>();
    private DataSet selectedDataSet;

    private static DatasetToContents instance;
    private BorderPane activePane = new BorderPane();

    private DatasetToContents() {

    }

    public static DatasetToContents getInstance() {
        if (instance == null) {
            instance = new DatasetToContents();
            instance.sampleSimulation();
            instance.getActivePane().setCenter(instance.getSelectedMain());
        }
        return instance;
    }

    private void sampleSimulation() {
        Graph graph = RandomGraph.randomGraphRandomForwardEdges(10, 0,
                20, 500, 100, 1000, false);
        LargeScaleSimulation simulation = new LargeScaleSimulation(graph);
        simulation.setCoefRange(0, 0.5);
        simulation.setSelfLoopCoef(0.1);
        DataSet dataSet = simulation.simulateDataReducedForm(1000);
        DatasetToContents.getInstance().add(dataSet, graph, "Sample Data", "Sample Graph");
        this.selectedDataSet = dataSet;
    }

    public void add(DataSet dataSet, String dataName) {
        datasetToContents.put(dataSet, new Contents(dataSet, dataName));
        datasetNamesToDataset.put(dataSet.getName(), dataSet);
        this.selectedDataSet = dataSet;
        activePane.setCenter(getSelectedMain());
    }

    public void add(DataSet dataSet, Graph graph, String dataName, String graphName) {
        datasetToContents.put(dataSet, dataSet == null ? new Contents(dataSet, dataSet.getName())
                : new Contents(dataSet, graph, dataName, graphName));
        datasetNamesToDataset.put(dataSet.getName(), dataSet);
        this.selectedDataSet = dataSet;
        activePane.setCenter(getSelectedMain());
    }

    public Contents get(DataSet dataSet) {
        return datasetToContents.get(dataSet);
    }

    public Contents get(String datasetName) {
        return datasetToContents.get(datasetNamesToDataset.get(datasetName));
    }

    public void remove(DataSet dataSet) {
        datasetToContents.remove(dataSet);
        datasetNamesToDataset.remove(dataSet.getName());
        selectedDataSet = datasetToContents.keySet().iterator().next();
    }

    public void remove(String datasetName) {
        DataSet dataSet = datasetNamesToDataset.get(datasetName);
        remove(dataSet);
    }

    public Node getSelectedMain() {
        return get(selectedDataSet).getMain();
    }

    public BorderPane getActivePane() {
        return activePane;
    }

    public Contents getSelectedContents() {
        return datasetToContents.get(selectedDataSet);
    }
}
