package io.github.cmuphil.tetradfx.ui;

import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.DelimiterType;
import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.data.SimpleDataLoader;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphSaveLoadUtils;
import edu.cmu.tetrad.graph.RandomGraph;
import edu.cmu.tetrad.sem.LargeScaleSimulation;
import edu.cmu.tetrad.util.Parameters;
import edu.pitt.dbmi.data.reader.Delimiter;
import io.github.cmuphil.tetradfx.for751lib.ChangedStuffINeed;
import io.github.cmuphil.tetradfx.utils.Utils;
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
import java.util.*;

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
    private final File sessionDir;
    private final BorderPane parametersPane;
    private final BorderPane notesPane;
    private final Parameters parameters;

    private String selectedName;

    /**
     * Private constructor. Loads the session from the session directory.
     *
     * @param sessionDir The directory where the session is stored.z
     */
    private Session(File sessionDir) {
        this.parametersPane = new BorderPane();
        this.notesPane = new BorderPane();

        var root = new TreeItem<>("Session Projects");
        root.setExpanded(true);

        this.sessionDir = sessionDir;

        if (!this.sessionDir.exists() || Objects.requireNonNull(this.sessionDir.listFiles()).length == 0) {
            boolean made = this.sessionDir.mkdir();

            if (!made) {
                System.out.println("Directory exists: " + this.sessionDir.getPath());
            } else {
                System.out.println("Made directory: " + this.sessionDir.getPath());
            }

            projects = new TreeItem<>("Session Projects");
            sessionTreeView = new TreeView<>(projects);
            projects.setExpanded(true);

            var graph = RandomGraph.randomGraphRandomForwardEdges(10, 0,
                    20, 500, 100, 1000, false);
            var simulation = new LargeScaleSimulation(graph);
            simulation.setCoefRange(0, 0.5);
            simulation.setSelfLoopCoef(0.1);
            var dataSet = simulation.simulateDataReducedForm(1000);
            String newName = Utils.nextName("Sample Simulation", namesToProjects.keySet());
            add(dataSet, graph, newName, "Sample Data", "True Graph");
            selectProject(newName);
//            getSelectedProject().setParametersAndNotesText();
        } else {
            File[] projectDirs = this.sessionDir.listFiles();

            if (projectDirs == null) {
                throw new NullPointerException("There were no projects in the session directory");
            }

            projects = new TreeItem<>("Session Projects");
            sessionTreeView = new TreeView<>(projects);
            projects.setExpanded(true);

            namesToProjects.clear();

            for (File dir : projectDirs) {
                if (!dir.isDirectory() && dir.getName().equals("parameters.json")) continue;

                if (!dir.isDirectory()) {
                    try {
                        Files.delete(dir.toPath());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    String projectName = dir.getName().replace('_', ' ');
                    File projectDir = new File(this.sessionDir, projectName.replace(" ", "_"));

                    if (!namesToProjects.containsKey(projectName)) {
                        Project _project = new Project(null, null, projectName, null, null, projectDir);
                        namesToProjects.put(projectName, _project);
                        TreeItem<String> childItem1 = _project.getTreeItem();
                        projects.getChildren().add(childItem1);
                        selectProject(projectName);
                    }

                    File dataDir = new File(dir, "data");
                    File searchDir = new File(dir, "search");
                    File knowledgeDir = new File(dir, "knowledge");
                    File graphDir = new File(dir, "other_graphs");

                    File[] dataFiles = dataDir.listFiles();

                    if (dataFiles != null) {
                        for (File file : dataFiles) {
                            if (file.getName().endsWith(".txt") && !file.getName().toLowerCase().contains("note")) {
                                try {
                                    int maxNumCategories = 5;
                                    DataSet _dataSet = ChangedStuffINeed.loadMixedData(file, "//", '\"',
                                            "*", true, maxNumCategories, Delimiter.TAB, !file.getName().contains("Data"));

                                    String name = _dataSet.getName();

                                    if (name.endsWith(".txt")) name = name.substring(0, name.length() - 4);

                                    String replace = name.replace('_', ' ');
                                    getSelectedProject().addDataSet(replace, _dataSet, false);
                                } catch (IOException e) {
                                    Alert alert = new Alert(Alert.AlertType.ERROR);
                                    alert.setTitle("Error Dialog");
                                    alert.setContentText(e.getMessage());
                                    alert.showAndWait();
                                }
                            }
                        }
                    }

                    var searchFiles = searchDir.listFiles();

                    if (searchFiles != null) {
                        for (File file : searchFiles) {
                            if (file.getName().endsWith("txt") && !file.getName().toLowerCase().contains("note")) {
                                var _graph = GraphSaveLoadUtils.loadGraphTxt(file);
                                getSelectedProject().addSearchResult(
                                        file.getName().replace('_', ' ').replace(".txt",
                                                ""), _graph, false, getParameters(), new ArrayList<>());
                            } else if (file.getName().endsWith("json")) {
//                                Graph _graph = (Graph) ChangedStuffINeed.javaFromJson(file, EdgeListGraph.class);
                                var _graph = GraphSaveLoadUtils.loadGraphJson(file);
                                getSelectedProject().addSearchResult(
                                        file.getName().replace('_', ' ').replace(".json",
                                                ""), _graph , false, getParameters(), new ArrayList<>());
                            }
                        }
                    }

                    var knowledgeFiles = knowledgeDir.listFiles();

                    if (knowledgeFiles != null) {
                        for (var file : knowledgeFiles) {
                            if (file.getName().endsWith("txt") && !file.getName().toLowerCase().contains("note")) {
                                try {
                                    Knowledge knowledge = SimpleDataLoader.loadKnowledge(file, DelimiterType.WHITESPACE,
                                            "//");
                                    getSelectedProject().addKnowledge(file.getName().replace('_', ' ').replace(".txt", ""),
                                            knowledge, false);
                                } catch (IOException e) {
                                    Alert alert = new Alert(Alert.AlertType.ERROR);
                                    alert.setTitle("Error Dialog");
                                    alert.setHeaderText(null); // You can set a header text or keep it null
                                    alert.setContentText("Could not load knowledge file: " + e.getMessage());
                                    alert.showAndWait();
                                }
                            }
                        }
                    }

                    var graphFiles = graphDir.listFiles();

                    if (graphFiles != null) {
                        for (var file : graphFiles) {
                            if (file.getName().endsWith("txt") && !file.getName().toLowerCase().contains("note")) {
                                Graph _graph = GraphSaveLoadUtils.loadGraphTxt(file);
                                getSelectedProject().addGraph(file.getName().replace('_', ' ').replace(".txt", ""), _graph, false);
                            } else if (file.getName().endsWith("json")) {
//                                Graph _graph = (Graph) ChangedStuffINeed.javaFromJson(file, EdgeListGraph.class);
                                Graph _graph = GraphSaveLoadUtils.loadGraphJson(file);
                                getSelectedProject().addGraph(file.getName().replace('_', ' ').replace(".json", ""), _graph, false);
                            }
                        }
                    }
                }
            }

            getSelectedProject().setParametersAndNotesText();
        }

        System.out.println("dir: " + this.sessionDir.getAbsolutePath());

        sessionTreeView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) {
                var selectedItem = sessionTreeView.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    String selectedName = selectedItem.getValue();
                    selectProject(selectedName);
                }
            }
        });

        this.parameters = ChangedStuffINeed.loadParameters(new File(sessionDir, "parameters.json"));
    }

    /**
     * Selects a project in the session.
     *
     * @param selectedName The name of the project to be selected.
     */
    public void selectProject(String selectedName) {
        setSelectedName(selectedName);
        activePane.setCenter(getSelectedMain());
        notesPane.setCenter(getSelectedProject().getNotesArea());
        parametersPane.setCenter(getSelectedProject().getParametersArea());
        getSelectedProject().setParametersAndNotesText();
        sessionTreeView.getSelectionModel().select(getSelectedProject().getTreeItem());
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
     *
     * @param selectedName The name of the selected project.
     */
    private void setSelectedName(String selectedName) {
        if (!namesToProjects.containsKey(selectedName)) {
            throw new IllegalArgumentException("No project with name " + selectedName);
        }

        this.selectedName = selectedName;
        System.out.println("Setting selected name to " + selectedName);
    }

    /**
     * Returns the singleton instance of the session.
     *
     * @return The singleton instance of the session.
     */
    public static Session getInstance() {
        if (instance == null) {
            String userHomeDirectory = System.getProperty("user.home");

            // Temp, delete the old directory.
            try {
                ChangedStuffINeed.deleteDirectory(new File(userHomeDirectory, ".tetrad-fx-docs").toPath());
            } catch (IOException e) {
                // Ignore.
            }

            File file = new File(userHomeDirectory, ".tetrad-fx-session");

            if (file.exists()) {
                instance = new Session(file);
            } else {
                newInstance();
            }
        }

        return instance;
    }

    /**
     * Causes a new session to be created. After this method is called, the singleton instance will be the new session.
     */
    public static void newInstance() {
        String userHomeDirectory = System.getProperty("user.home");
        instance = new Session(new File(userHomeDirectory, ".tetrad-fx-session"));
    }

    /**
     * Adds a project to the session.
     *
     * @param dataSet     The dataset to be analyzed. (This may be null.)
     * @param graph       The graph to be analyzed. (This may be null.)
     * @param projectName The name of the project.
     * @param dataName    The name of the dataset. (This may be null.)
     * @param graphName   The name of the graph. (This may be null.)
     */
    public void add(DataSet dataSet, Graph graph, String projectName, String dataName, String graphName) {
        var sessionDir = new File(this.sessionDir, projectName.replace(" ", "_"));

        if (!sessionDir.exists()) {
            boolean made = sessionDir.mkdir();

            if (!made) {
                throw new IllegalArgumentException("Could not make directory " + sessionDir.getPath());
            }
        }

        Project project = new Project(dataSet, graph, projectName, dataName, graphName, sessionDir);
        namesToProjects.put(projectName, project);
        selectedName = projectName;

        System.out.println("add setting select name to " + selectedName);

        TreeItem<String> item = getSelectedProject().getTreeItem();
        projects.getChildren().add(item);
        selectProject(projectName);
    }

    /**
     * Returns the main component of the selected project.
     *
     * @return The main component of the selected project.
     */
    public Node getSelectedMain() {
        Project selected = getSelectedProject();
        return selected.getSessionTabPane();
    }

    /**
     * Returns the active pane.
     *
     * @return The active pane.
     */
    public BorderPane getActivePane() {
        return activePane;
    }

    /**
     * Returns the selected project.
     *
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
     *
     * @return The tree view of the session.
     */
    public TreeView<String> getSessionTreeView() {
        return sessionTreeView;
    }

    /**
     * Returns the names of the projects in the session.
     *
     * @return The names of the projects in the session.
     */
    public HashSet<String> getProjectNames() {
        return new HashSet<>(namesToProjects.keySet());
    }

    /**
     * Returns the parameters pane.
     *
     * @return The parameters pane.
     */
    public BorderPane getParametersPane() {
        return parametersPane;
    }

    /**
     * Returns the notes pane.
     *
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

//        if (projects.getChildren().size() == 1) {
//            Alert alert = new Alert(Alert.AlertType.ERROR);
//            alert.setTitle("Delete Project");
//            alert.setHeaderText("Delete Project");
//            alert.setContentText("You cannot delete the last project in the session.");
//
//            alert.showAndWait();
//            return;
//        }

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

                    File _dir = new File(sessionDir, selectedName.replace(" ", "_"));

                    try {
                        if (_dir.exists()) {
                            deleteDirectory(_dir.toPath());
                        } else {
                            System.out.println("Directory does not exist: " + _dir.getAbsolutePath());
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    TreeItem<String> treeItem = projects.getChildren().get(0);
                    selectProject(treeItem.getValue());
                } else if (response == myNoButton) {
                    System.out.println("User clicked Cancel");
                }
            });
        }
    }

    public Parameters getParameters() {
        return this.parameters;
    }

    public File getSessionDir() {
        return sessionDir;
    }
}
