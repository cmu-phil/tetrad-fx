package io.github.cmuphil.tetradfx.ui;

import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphSaveLoadUtils;
import edu.cmu.tetrad.graph.RandomGraph;
import edu.cmu.tetrad.sem.LargeScaleSimulation;
import edu.cmu.tetrad.util.Parameters;
import edu.pitt.dbmi.data.reader.Delimiter;
import io.github.cmuphil.tetradfx.for751lib.ChangedStuffINeed;
import io.github.cmuphil.tetradfx.utils.NameUtils;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * <p>Keeps track of projects in a session and which one is selected. The session tree view is
 * a component that lets the user switch between projects.</p>
 *
 * <p>Each project has as its goal to analyze a particular dataset. (This is currently the goal
 * of Tetrad-FX.)</p>
 *
 * @author josephramsey
 */
public class Session {
    private static Session instance;
    private final Map<String, Project> namesToProjects = new HashMap<>();
    private final BorderPane activePane = new BorderPane();
    private final TreeView<String> sessionTreeView;
    private final TreeItem<String> projects;
    private final File dir;
    private final BorderPane parametersPane;
    private final BorderPane notesPane;
    private String selectedName;

    /**
     * Private constructor.
     * @param mainDir The directory where the session is stored.
     */
    private Session(File mainDir) {
        this.parametersPane = new BorderPane();
        this.notesPane = new BorderPane();

        var root = new TreeItem<>("Session");
        root.setExpanded(true);

        String sessionName = "Sample Session";

        this.dir = mainDir;

        if (!dir.exists() || dir.listFiles().length == 0) {
            boolean made = dir.mkdir();
            projects = new TreeItem<>(sessionName);
            sessionTreeView = new TreeView<>(projects);
            projects.setExpanded(true);

            if (!made) {
                System.out.println("Directory was empty: " + dir.getPath());
            }

            var graph = RandomGraph.randomGraphRandomForwardEdges(10, 0,
                    20, 500, 100, 1000, false);
            var simulation = new LargeScaleSimulation(graph);
            simulation.setCoefRange(0, 0.5);
            simulation.setSelfLoopCoef(0.1);
            var dataSet = simulation.simulateDataReducedForm(1000);
            String newName = NameUtils.nextName("Sample Simulation", namesToProjects.keySet());
            add(dataSet, graph, newName, "Sample Data", "True Graph");
            setSelectedName(newName);
            getSelectedProject().setParametersAndNotesText();
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

            projects = new TreeItem<>(sessionName);
            sessionTreeView = new TreeView<>(projects);
            projects.setExpanded(true);

            namesToProjects.clear();

            for (File dir : sessionDirs) {
                if (dir.isDirectory()) {
                    sessionName = dir.getName().replace('_', ' ');

                    if (!namesToProjects.containsKey(sessionName)) {
                        Project _project = new Project(null, null, sessionName, null, null, sessionDir);
                        namesToProjects.put(sessionName, _project);
                        setSelectedName(sessionName);
                        activePane.setCenter(getSelectedMain());
                        TreeItem<String> childItem1 = _project.getTreeItem();
                        projects.getChildren().add(childItem1);
                    }

                    File dataDir = new File(dir, "data");
                    File searchDir = new File(dir, "search_graphs");
                    File graphDir = new File(dir, "other_graphs");

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
                                    getSelectedProject().addDataSet(replace, _dataSet, false, true);
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
                                getSelectedProject().addGraph(file.getName().replace('_', ' ').replace(".txt", ""), _graph, false, true);
                            } else if (file.getName().endsWith("json")) {
//                                Graph _graph = (Graph) ChangedStuffINeed.javaFromJson(file, EdgeListGraph.class);
                                Graph _graph = GraphSaveLoadUtils.loadGraphJson(file);
                                getSelectedProject().addGraph(file.getName().replace('_', ' ').replace(".json", ""), _graph, false, true);
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
//                                Graph _graph = (Graph) ChangedStuffINeed.javaFromJson(file, EdgeListGraph.class);
                                var _graph = GraphSaveLoadUtils.loadGraphJson(file);
                                getSelectedProject().addSearchResult(file.getName().replace('_', ' ').replace(".json", ""), _graph, true, false, new Parameters(), new ArrayList<>());
                            }
                        }
                    }
                }
            }

            getSelectedProject().setParametersAndNotesText();
        }

        sessionTreeView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                var selectedItem = sessionTreeView.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    String selectedName = selectedItem.getValue();
                    selectOtherProject(selectedName);
                }
            }
        });
    }

    private void selectOtherProject(String selectedName) {
        setSelectedName(selectedName);
        activePane.setCenter(getSelectedMain());
        parametersPane.setCenter(getSelectedProject().getParametersArea());
        notesPane.setCenter(getSelectedProject().getNotesArea());
        getSelectedProject().setParametersAndNotesText();
    }

    /**
     * Returns the selected project name.
     */
    public String getSelectedName() {
        System.out.println("Getting selected name: " + selectedName);
        return selectedName;
    }

    /**
     * Sets the name of the selected project.
     * @param selectedName The name of the selected project.
     */
    public void setSelectedName(String selectedName) {
        if (!namesToProjects.containsKey(selectedName)) {
            throw new IllegalArgumentException("No project with name " + selectedName);
        }

        this.selectedName = selectedName;
        System.out.println("Setting selected name to " + selectedName);
    }

    /**
     * Returns the singleton instance of the session.
     * @return The singleton instance of the session.
     */
    public static Session getInstance() {
        if (instance == null) {
            String userHomeDirectory = System.getProperty("user.home");

            File file = new File(userHomeDirectory, ".tetrad-fx-docs");

            if (file.exists()) {
                instance = new Session(file);
            } else {
                newInstance();
            }
        }

        return instance;
    }

    /**
     * Causes a new session to be created. After this method is called, the singleton instance
     * will be the new session.
     */
    public static void newInstance() {
        String userHomeDirectory = System.getProperty("user.home");
        instance = new Session(new File(userHomeDirectory, ".tetrad-fx-docs"));
    }

    /**
     * Adds a project to the session.
     * @param dataSet The dataset to be analyzed. (This may be null.)
     * @param graph The graph to be analyzed. (This may be null.)
     * @param projectName The name of the project.
     * @param dataName The name of the dataset. (This may be null.)
     * @param graphName The name of the graph. (This may be null.)
     */
    public void add(DataSet dataSet, Graph graph, String projectName, String dataName, String graphName) {
        var sessionDir = new File(dir, projectName.replace(" ", "_"));

        if (!sessionDir.exists()) {
            boolean made = sessionDir.mkdir();

            if (!made) {
                throw new IllegalArgumentException("Could not make directory " + sessionDir.getPath());
            }
        }

        namesToProjects.put(projectName, new Project(dataSet, graph, projectName, dataName, graphName, sessionDir));
        setSelectedName(projectName);
        activePane.setCenter(getSelectedMain());
        TreeItem<String> childItem1 = getSelectedProject().getTreeItem();
        projects.getChildren().add(childItem1);
    }

    /**
     * Returns the main component of the selected project.
     * @return The main component of the selected project.
     */
    public Node getSelectedMain() {
        Project selected = getSelectedProject();
        return selected.getMainTabPane();
    }

    /**
     * Returns the active pane.
     * @return The active pane.
     */
    public BorderPane getActivePane() {
        return activePane;
    }

    /**
     * Returns the selected project.
     * @return The selected project.
     */
    public Project getSelectedProject() {
        var project = namesToProjects.get(getSelectedName());

        if (project == null) {
            throw new NullPointerException("Project is null");
        }

        project.setParametersAndNotesText();

        return project;
    }

    /**
     * Returns the tree view of the session. Users can click here to switch projects.
     * @return The tree view of the session.
     */
    public TreeView<String> getSessionTreeView() {
        return sessionTreeView;
    }

    /**
     * Returns the names of the projects in the session.
     * @return The names of the projects in the session.
     */
    public HashSet<String> getProjectNames() {
        return new HashSet<>(namesToProjects.keySet());
    }

    /**
     * Returns the parameters pane.
     * @return The parameters pane.
     */
    public BorderPane getParametersPane() {
        return parametersPane;
    }

    /**
     * Returns the notes pane.
     * @return The notes pane.
     */
    public BorderPane getNotesPane() {
        return notesPane;
    }

    public static void deleteDirectory(Path dir) throws IOException {
        if (Files.exists(dir)) {
            if (Files.isDirectory(dir)) {
                try (DirectoryStream<Path> entries = Files.newDirectoryStream(dir)) {
                    for (Path entry : entries) {
                        deleteDirectory(entry);
                    }
                }
            }
            Files.delete(dir);
        }
    }

    public void deleteSelectedProject() {
        Project selectedProject = Session.getInstance().getSelectedProject();

        if (projects.getChildren().size() == 1) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Delete Project");
            alert.setHeaderText("Delete Project");
            alert.setContentText("You cannot delete the last project in the session.");

            alert.showAndWait();
            return;
        }

        if (selectedProject != null) {
            ButtonType myYesButton = new ButtonType("Yes, go ahead and delete it");
            ButtonType myNoButton = new ButtonType("No, DON\"T DELETE IT!");
            String text = "Are you sure you want to delete the project " + selectedName + "?"
                    + "\n\nThis will delete all data, graphs, and search results associated with this project."
                    + "\n\nThis action cannot be undone. You may wish to save the session first.";

            String selectedName = Session.getInstance().getSelectedName();
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, text, myYesButton, myNoButton);
            alert.setTitle("Delete Project");
            alert.setHeaderText("Delete Project");

            alert.showAndWait().ifPresent(response -> {
                if (response == myYesButton) {
                    namesToProjects.remove(selectedName);
                    projects.getChildren().remove(selectedProject.getTreeItem());

                    File _dir = new File(dir, selectedName.replace(" ", "_"));

                    try {
                        if (_dir.exists()) {
                            deleteDirectory(_dir.toPath());
                        } else {
                            System.out.println("Directory does not exist: " + _dir.getAbsolutePath());
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    TreeItem<String> stringTreeItem = projects.getChildren().get(0);
                    selectOtherProject(stringTreeItem.getValue());

                } else if (response == myNoButton) {
                    System.out.println("User clicked Cancel");
                }
            });
        }
    }
}
