package io.github.cmuphil.tetradfx.ui;

import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.Graph;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;

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
    private String selectedName = "";

    private static NamesToContents instance;
    private final BorderPane activePane = new BorderPane();
    private final TreeView<String> sessionTreeView;
    private final TreeItem<String> projects = new TreeItem<>(Utils.nextName("Session",
            namesToContents.keySet()));

    private NamesToContents() {
        TreeItem<String> root = new TreeItem<>("Session");
        root.setExpanded(true);
        sessionTreeView = new TreeView<>(projects);
        projects.setExpanded(true);

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
        if (dataSet == null && graph == null) {
            namesToContents.put(contentsName, new Contents(null, null, contentsName, null, null));
            activePane.setCenter(getSelectedMain());
            return;
        } else if (dataSet != null && graph == null) {
            namesToContents.put(contentsName, new Contents(dataSet, null, contentsName, dataName, null));
        } else if (dataSet == null) {
            namesToContents.put(contentsName, new Contents(null, graph, contentsName, null, graphName));
        } else {
            namesToContents.put(contentsName, new Contents(dataSet, graph, contentsName, dataName, graphName));
        }

        selectedName = contentsName;

        activePane.setCenter(getSelectedMain());
        TreeItem<String> childItem1 = getSelectedContents().getTreeItem();// new TreeItem<>(getSelectedName());
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
