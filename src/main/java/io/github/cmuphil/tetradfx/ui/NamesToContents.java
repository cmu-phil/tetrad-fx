package io.github.cmuphil.tetradfx.ui;

import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.RandomGraph;
import edu.cmu.tetrad.sem.LargeScaleSimulation;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Maps datasets to their contents.
 *
 * @author josephramsey
 */
public class NamesToContents {
    private final Map<String, Contents> namesToContents = new HashMap<>();
    private final String sessionName;
    private String selectedName = "";

    private static NamesToContents instance;
    private final BorderPane activePane = new BorderPane();
    private final TreeView<String> sessionTreeView;
    private final TreeItem<String> projects;
    private File dir;

    private NamesToContents() {
        TreeItem<String> root = new TreeItem<>("Session");
        root.setExpanded(true);
        sessionName = Utils.nextName("Session", namesToContents.keySet());
        projects = new TreeItem<>(sessionName);
        sessionTreeView = new TreeView<>(projects);
        projects.setExpanded(true);

        dir = new File("tetrad-fx-docs");

        if (!dir.exists()) {
            boolean made = dir.mkdir();

            if (!made) {
                throw new IllegalArgumentException("Could not make directory " + dir.getPath());
            }
        }

        System.out.println("dir: " + dir.getAbsolutePath());


        var graph = RandomGraph.randomGraphRandomForwardEdges(10, 0,
                20, 500, 100, 1000, false);
        var simulation = new LargeScaleSimulation(graph);
        simulation.setCoefRange(0, 0.5);
        simulation.setSelfLoopCoef(0.1);
        var dataSet = simulation.simulateDataReducedForm(1000);
        this.selectedName = Utils.nextName("Sample Simulation", namesToContents.keySet());
        add(dataSet, graph, this.selectedName, "Sample Data", "Sample Graph");

        sessionTreeView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                TreeItem<String> selectedItem = sessionTreeView.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    selectedName = selectedItem.getValue();
                    activePane.setCenter(getSelectedMain());
                }
            }
        });
    }

    public static NamesToContents getInstance() {
        if (instance == null) {
            instance = new NamesToContents();
        }

        return instance;
    }

    public void add(DataSet dataSet, Graph graph, String contentsName, String dataName, String graphName) {
        File sessionDir = new File(dir, contentsName);

        if (!sessionDir.exists()) {
            boolean made = sessionDir.mkdir();

            if (!made) {
                throw new IllegalArgumentException("Could not make directory " + sessionDir.getPath());
            }
        }

        namesToContents.put(contentsName, new Contents(dataSet, graph, contentsName, dataName, graphName, sessionDir));
        selectedName = contentsName;
        activePane.setCenter(getSelectedMain());
        TreeItem<String> childItem1 = getSelectedContents().getTreeItem();
        projects.getChildren().add(childItem1);
    }

    public Node getSelectedMain() {
        Contents selected = getSelected();
        return selected.getMain();
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

    public TreeView<String> getSessionTreeView() {
        return sessionTreeView;
    }

    public Collection<String> getProjectNames() {
        return new HashSet<>(namesToContents.keySet());
    }
}
