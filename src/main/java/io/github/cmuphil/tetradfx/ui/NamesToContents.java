package io.github.cmuphil.tetradfx.ui;

import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.RandomGraph;
import edu.cmu.tetrad.sem.LargeScaleSimulation;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps datasets to their contents.
 *
 * @author josephramsey
 */
public class NamesToContents {
    private final Map<String, Contents> namesToContents = new HashMap<>();
    private String selectedName = "";

    private static NamesToContents instance;
    private final BorderPane activePane = new BorderPane();
    private final TreeView<String> dataTreeView;
    private final TreeItem<String> projects = new TreeItem<>("Projects");

    private NamesToContents() {
        TreeItem<String> root = new TreeItem<>("Projects");
        root.setExpanded(true);
        dataTreeView = new TreeView<>(projects);
        projects.setExpanded(true);
    }

    public static NamesToContents getInstance() {
        if (instance == null) {
            instance = new NamesToContents();
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
        this.selectedName = "Sample Simulation";
        NamesToContents.getInstance().add(dataSet, graph, this.selectedName,"Sample Data", "Sample Graph");

        dataTreeView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                TreeItem<String> selectedItem = dataTreeView.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    String selectedText = selectedItem.getValue();
                    selectedName = selectedText;
                    activePane.setCenter(getSelectedMain());
                }
            }
        });
    }

    public String nextName(String projectName) {
        for (int i = 1; i < 1000; i++) {
            String name = projectName + " " + i;

            if (!namesToContents.containsKey(name)) {
                return name;
            }
        }

        throw new IllegalArgumentException("Too many projects");
    }

    public void add(DataSet dataSet, Graph graph, String contentsName, String dataName, String graphName) {
        if (dataSet != null && graph == null) {
            namesToContents.put(contentsName, new Contents(dataSet, null, dataName, null));
        } else if (dataSet == null && graph != null) {
            namesToContents.put(contentsName, new Contents(null, graph, null, graphName));
        } else {
            namesToContents.put(contentsName, new Contents(dataSet, graph, dataName, graphName));
        }

        selectedName = contentsName;

        activePane.setCenter(getSelectedMain());
        TreeItem<String> childItem1 = new TreeItem<>(getSelectedName());
        projects.getChildren().add(childItem1);
    }

    public Node getSelectedMain() {
        Contents selected = getSelected();
        return selected.getMain();
    }

    public String getSelectedName() {
        return selectedName;
    }

    public Contents getSelected() {
        return namesToContents.get(selectedName);
    }

    public BorderPane getActivePane() {
        return activePane;
    }

    public Contents getSelectedContents() {
        return getSelected();
    }

    public TreeView<String> getDataTreeView() {
        return dataTreeView;
    }
}
