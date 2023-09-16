package io.github.cmuphil.tetradfx.ui;

import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphSaveLoadUtils;
import edu.cmu.tetrad.graph.RandomGraph;
import edu.cmu.tetrad.sem.LargeScaleSimulation;
import edu.cmu.tetrad.util.Parameters;
import edu.pitt.dbmi.data.reader.Delimiter;
import io.github.cmuphil.tetradfx.for751lib.ChangedStuffINeed;
import io.github.cmuphil.tetradfx.utils.Utils;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Maps datasets to their projects.
 *
 * @author josephramsey
 */
public class NamesToProjects {
    private static NamesToProjects instance;
    private final Map<String, Project> namesToProjects = new HashMap<>();
    private final BorderPane activePane = new BorderPane();
    private final TreeView<String> sessionTreeView;
    private final TreeItem<String> projects;
    private final File dir;
    private final BorderPane parametersPane;
    private String selectedName;

    private NamesToProjects(BorderPane parametersPane) {
        this.parametersPane = parametersPane;

        var root = new TreeItem<>("Session");
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
            this.selectedName = Utils.nextName("Sample Simulation", namesToProjects.keySet());
            add(dataSet, graph, this.selectedName, "Sample Data", "Sample Graph");
        } else {
            File[] sessionDirs = dir.listFiles();

            var _sessionName = "Sample Session";
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

                    if (!namesToProjects.containsKey(sessionName)) {
                        Project _project = new Project(null, null, sessionName, null, null, sessionDir);
                        namesToProjects.put(sessionName, _project);
                        selectedName = sessionName;
                        activePane.setCenter(getSelectedMain());
                        TreeItem<String> childItem1 = _project.getTreeItem();
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
                                    getSelectedProject().addDataSet(replace, _dataSet, true, false);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    }

                    var graphFiles = graphDir.listFiles();

                    if (graphFiles != null) {
                        for (var file : graphFiles) {
                            if (file.getName().endsWith("txt")) {
                                Graph _graph = GraphSaveLoadUtils.loadGraphTxt(file);
                                getSelectedProject().addGraph(file.getName().replace('_', ' ').replace(".txt", ""), _graph, true, false);
                            } else if (file.getName().endsWith("json")) {
                                Graph _graph = GraphSaveLoadUtils.loadGraphJson(file);
                                getSelectedProject().addGraph(file.getName().replace('_', ' ').replace(".json", ""), _graph, true, false);
                            }
                        }
                    }

                    var searchFiles = searchDir.listFiles();

                    if (searchFiles != null) {
                        for (File file : searchFiles) {
                            if (file.getName().endsWith("txt")) {
                                var _graph = GraphSaveLoadUtils.loadGraphTxt(file);
                                getSelectedProject().addSearchResult(file.getName().replace('_', ' ').replace(".txt", ""), _graph, true, false, new Parameters(), new ArrayList<>());
                            } else if (file.getName().endsWith("json")) {
                                var _graph = GraphSaveLoadUtils.loadGraphJson(file);
                                getSelectedProject().addSearchResult(file.getName().replace('_', ' ').replace(".json", ""), _graph, true, false, new Parameters(), new ArrayList<>());
                            }
                        }
                    }
                }
            }
        }


        System.out.println("dir: " + dir.getAbsolutePath());

        sessionTreeView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                var selectedItem = sessionTreeView.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    selectedName = selectedItem.getValue();
                    activePane.setCenter(getSelectedMain());

                    var parametersArea = NamesToProjects.getInstance().getSelectedProject().getParametersArea();
                    parametersPane.setCenter(parametersArea);
                }
            }
        });
    }

    public static NamesToProjects getInstance() {
        if (instance == null) {
            instance = new NamesToProjects(new BorderPane());
        }

        return instance;
    }

    public void add(DataSet dataSet, Graph graph, String projectName, String dataName, String graphName) {
        var sessionDir = new File(dir, projectName.replace(" ", "_"));

        if (!sessionDir.exists()) {
            boolean made = sessionDir.mkdir();

            if (!made) {
                throw new IllegalArgumentException("Could not make directory " + sessionDir.getPath());
            }
        }

        namesToProjects.put(projectName, new Project(dataSet, graph, projectName, dataName, graphName, sessionDir));
        selectedName = projectName;
        activePane.setCenter(getSelectedMain());
        TreeItem<String> childItem1 = getSelectedProject().getTreeItem();
        projects.getChildren().add(childItem1);
    }

    public Node getSelectedMain() {
        Project selected = getSelectedProject();
        return selected.getMain();
    }

    public BorderPane getActivePane() {
        return activePane;
    }

    public Project getSelectedProject() {
        var project = namesToProjects.get(selectedName);

        if (project == null) {
            throw new NullPointerException("Project is null");
        }

        return project;
    }

    public TreeView<String> getSessionTreeView() {
        return sessionTreeView;
    }

    public Collection<String> getProjectNames() {
        return new HashSet<>(namesToProjects.keySet());
    }

    public BorderPane getParametersPane() {
        return parametersPane;
    }
}
