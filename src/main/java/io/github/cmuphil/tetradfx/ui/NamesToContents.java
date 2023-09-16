package io.github.cmuphil.tetradfx.ui;

import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphSaveLoadUtils;
import edu.cmu.tetrad.graph.RandomGraph;
import edu.cmu.tetrad.sem.LargeScaleSimulation;
import edu.cmu.tetrad.util.Parameters;
import edu.pitt.dbmi.data.reader.Delimiter;
import io.github.cmuphil.tetradfx.for751lib.ChangedStuffINeed;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Maps datasets to their contents.
 *
 * @author josephramsey
 */
public class NamesToContents {
    private static NamesToContents instance;
    private final Map<String, Contents> namesToContents = new HashMap<>();
    private final BorderPane activePane = new BorderPane();
    private final TreeView<String> sessionTreeView;
    private final TreeItem<String> projects;
    private final File dir;
    private final BorderPane parametersPane;
    private String selectedName;

    private NamesToContents(BorderPane parametersPane) {
        this.parametersPane = parametersPane;

        TreeItem<String> root = new TreeItem<>("Session");
        root.setExpanded(true);

        String sessionName = "Sample Session";

        dir = new File("tetrad-fx-docs");

        if (!dir.exists()) {
            boolean made = dir.mkdir();
            projects = new TreeItem<>(sessionName);
            sessionTreeView = new TreeView<>(projects);
            projects.setExpanded(true);

            if (!made) {
                throw new IllegalArgumentException("Could not make directory " + dir.getPath());
            }

            var graph = RandomGraph.randomGraphRandomForwardEdges(10, 0,
                    20, 500, 100, 1000, false);
            var simulation = new LargeScaleSimulation(graph);
            simulation.setCoefRange(0, 0.5);
            simulation.setSelfLoopCoef(0.1);
            var dataSet = simulation.simulateDataReducedForm(1000);
            this.selectedName = Utils.nextName("Sample Simulation", namesToContents.keySet());
            add(dataSet, graph, this.selectedName, "Sample Data", "Sample Graph");
        } else {
            File[] sessionDirs = dir.listFiles();

            String _sessionName = "Sample Session";
            File sessionDir = dir;

            if (sessionDirs == null) {
                throw new NullPointerException("sessionDirs1 is null");
            }

            for (File dir : sessionDirs) {
                if (dir.isDirectory()) {
                    _sessionName = dir.getName().replace('_', ' ');
                    sessionDir = dir;
                    break;
                }
            }

            sessionName = _sessionName;

            selectedName = sessionName;
            projects = new TreeItem<>(sessionName);
            sessionTreeView = new TreeView<>(projects);
            projects.setExpanded(true);

            for (File dir : sessionDirs) {
                if (dir.isDirectory()) {
                    sessionName = dir.getName().replace('_', ' ');

                    if (!namesToContents.containsKey(sessionName)) {
                        Contents _contents = new Contents(sessionName, sessionDir);
                        namesToContents.put(sessionName, _contents);
                        selectedName = sessionName;
                        activePane.setCenter(getSelectedMain());
                        TreeItem<String> childItem1 = _contents.getTreeItem();
                        projects.getChildren().add(childItem1);
                    }

                    File dataDir = new File(dir, "data");
                    File graphDir = new File(dir, "graph");
                    File searchDir = new File(dir, "search");

                    this.selectedName = sessionName;

                    File[] dataFiles = dataDir.listFiles();

                    if (dataFiles != null) {
                        for (File file : dataFiles) {
                            if (file.getName().endsWith(".txt")) {
                                try {
                                    int maxNumCategories = 5;
                                    DataSet _dataSet = ChangedStuffINeed.loadMixedData(file, "//", '\"',
                                            "*", true, maxNumCategories, Delimiter.TAB, false);

                                    String name = _dataSet.getName();

                                    if (name.endsWith(".txt")) name = name.substring(0, name.length() - 4);

                                    String replace = name.replace('_', ' ');
                                    getSelectedContents().addDataSet(replace, _dataSet, true, false);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    }

                    File[] graphFiles = graphDir.listFiles();

                    if (graphFiles != null) {
                        for (File file : graphFiles) {
                            if (file.getName().endsWith("txt")) {
                                Graph _graph = GraphSaveLoadUtils.loadGraphTxt(file);
                                getSelectedContents().addGraph(file.getName().replace('_', ' ').replace(".txt", ""), _graph, true, false);
                            } else if (file.getName().endsWith("json")) {
                                Graph _graph = GraphSaveLoadUtils.loadGraphJson(file);
                                getSelectedContents().addGraph(file.getName().replace('_', ' ').replace(".json", ""), _graph, true, false);
                            }
                        }
                    }

                    File[] searchFiles = searchDir.listFiles();

                    if (searchFiles != null) {
                        for (File file : searchFiles) {
                            if (file.getName().endsWith("txt")) {
                                Graph _graph = GraphSaveLoadUtils.loadGraphTxt(file);
                                getSelectedContents().addSearchResult(file.getName().replace('_', ' ').replace(".txt", ""), _graph, true, false, new Parameters(), new ArrayList<>());
                            } else if (file.getName().endsWith("json")) {
                                Graph _graph = GraphSaveLoadUtils.loadGraphJson(file);
                                getSelectedContents().addSearchResult(file.getName().replace('_', ' ').replace(".json", ""), _graph, true, false, new Parameters(), new ArrayList<>());
                            }
                        }
                    }
                }
            }
        }


        System.out.println("dir: " + dir.getAbsolutePath());

        sessionTreeView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                TreeItem<String> selectedItem = sessionTreeView.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    selectedName = selectedItem.getValue();
                    activePane.setCenter(getSelectedMain());

                    Node parametersArea = NamesToContents.getInstance().getSelectedContents().getParametersArea();
                    parametersPane.setCenter(parametersArea);
                }
            }
        });
    }

    public static NamesToContents getInstance() {
        if (instance == null) {
            instance = new NamesToContents(new BorderPane());
        }

        return instance;
    }

    public void add(DataSet dataSet, Graph graph, String contentsName, String dataName, String graphName) {
        File sessionDir = new File(dir, contentsName.replace(" ", "_"));

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
        Contents selected = getSelectedContents();
        return selected.getMain();
    }

    public BorderPane getActivePane() {
        return activePane;
    }

    public Contents getSelectedContents() {
        Contents contents = namesToContents.get(selectedName);

        if (contents == null) {
            throw new NullPointerException("contents is null");
        }

        return contents;
    }

    public TreeView<String> getSessionTreeView() {
        return sessionTreeView;
    }

    public Collection<String> getProjectNames() {
        return new HashSet<>(namesToContents.keySet());
    }

    public BorderPane getParametersPane() {
        return parametersPane;
    }
}
