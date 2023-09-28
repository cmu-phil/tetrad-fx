package io.github.cmuphil.tetradfx.ui;

import edu.cmu.tetrad.algcomparison.graph.RandomForward;
import edu.cmu.tetrad.algcomparison.simulation.BayesNetSimulation;
import edu.cmu.tetrad.algcomparison.simulation.LeeHastieSimulation;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphSaveLoadUtils;
import edu.cmu.tetrad.graph.RandomGraph;
import edu.cmu.tetrad.sem.LargeScaleSimulation;
import edu.cmu.tetrad.util.Parameters;
import edu.cmu.tetrad.util.RandomUtil;
import edu.pitt.dbmi.data.reader.Delimiter;
import io.github.cmuphil.tetradfx.for751lib.ChangedStuffINeed;
import io.github.cmuphil.tetradfx.utils.Utils;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * <p>Dislays all of the projects for a session.</p>
 *
 * @author josephramsey
 */
public class TetradFxMain {
    private static final TetradFxMain INSTANCE = new TetradFxMain();

    /**
     * @return The singleton instance of this class.
     */
    public static TetradFxMain getInstance() {
        return TetradFxMain.INSTANCE;
    }

    /**
     * Gets a simulation. This just returns some stock simulations for now.
     *
     * @param parameters The parameters.
     * @param type       The type of simulation.
     * @return The result of the simulation.
     */
    // This will eventually be replaced by some flexible UI for making simulations (or eliminated).
    @NotNull
    private static Result getSimulation(Parameters parameters, SimulationType type) {
        if (type == SimulationType.CONTINUOUS) {
            Graph graph = RandomGraph.randomGraphRandomForwardEdges(100, 0,
                    200, 500, 100, 1000, false);
            LargeScaleSimulation simulation = new LargeScaleSimulation(graph);
            simulation.setCoefRange(0, 0.5);
            simulation.setSelfLoopCoef(0.1);
            DataSet dataSet = simulation.simulateDataReducedForm(1000);
            return new Result(graph, dataSet);
        } else if (type == SimulationType.DISCRETE) {
            BayesNetSimulation simulation = new BayesNetSimulation(new RandomForward());
            simulation.createData(parameters, true);
            Graph graph = simulation.getTrueGraph(0);
            DataSet dataSet = (DataSet) simulation.getDataModel(0);
            return new Result(graph, dataSet);
        } else {
            LeeHastieSimulation simulation = new LeeHastieSimulation(new RandomForward());
            simulation.createData(parameters, true);
            Graph graph = simulation.getTrueGraph(0);
            DataSet dataSet = (DataSet) simulation.getDataModel(0);
            return new Result(graph, dataSet);
        }
    }

    /**
     * Creates the root pane. Passing primaryStage in here so that I can quit the application from a menu item and pop
     * up dialogs.
     *
     * @param primaryStage The primary stage.
     * @return The root pane.
     */
    public Pane getRoot(Stage primaryStage) {
        BorderPane activePane = Session.getInstance().getActivePane();
        MenuBar menuBar = getMenuBar(primaryStage);
        activePane.setTop(menuBar);
        activePane.setPrefSize(1000, 800);

        SplitPane mainSplit = new SplitPane();

        SplitPane leftSplit = new SplitPane();
        leftSplit.setOrientation(Orientation.VERTICAL);
        leftSplit.setDividerPosition(0, 0.5);

        BorderPane parametersPane = Session.getInstance().getParametersPane();
        TextArea parametersArea = Session.getInstance().getSelectedProject().getParametersArea();
        parametersArea.setFont(new Font("Arial", 14));
        parametersPane.setCenter(parametersArea);
        Tab paraneters = new Tab("Parameters", parametersPane);
        paraneters.setClosable(false);

        BorderPane notesPane = Session.getInstance().getNotesPane();
        TextArea notesArea = Session.getInstance().getSelectedProject().getNotesArea();
        notesPane.setCenter(notesArea);
        notesArea.setFont(new Font("Arial", 14));
        Tab notes = new Tab("Notes", notesPane);
        notes.setClosable(false);

        TabPane tabPane = new TabPane();
        tabPane.getTabs().addAll(notes, paraneters);

        leftSplit.getItems().addAll(Session.getInstance().getSessionTreeView(), tabPane);

        mainSplit.getItems().addAll(leftSplit, activePane);
        mainSplit.setDividerPosition(0, 0.2);
        mainSplit.setDividerPosition(1, 0.8);

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(mainSplit);
        borderPane.setTop(menuBar);

        StackPane root = new StackPane();
        root.getChildren().add(borderPane);

        return root;
    }

    /**
     * Loads data from a file.
     */
    private void loadDataAction() {
        var applyButtonType = new ButtonType("Load");

        var continuousBtn = new RadioButton("Optimize for Continuous");
        var discreteBtn = new RadioButton("Optimize for Discrete");
        var mixedBtn = new RadioButton("General");

        var toggleGroup = new ToggleGroup();
        continuousBtn.setToggleGroup(toggleGroup);
        discreteBtn.setToggleGroup(toggleGroup);
        mixedBtn.setToggleGroup(toggleGroup);
        mixedBtn.setSelected(true);

        var textField = new TextField("3");
        textField.setPrefColumnCount(2);

        var firstRowAreVariableNamesLabel = new Label("First row contains variable names:");
        var toggleGroup2 = new ToggleGroup();
        var toggleFirstRowVarNamesYes = new RadioButton("Yes");
        var toggleFirstRowVarNamesNo = new RadioButton("No");
        toggleFirstRowVarNamesYes.setToggleGroup(toggleGroup2);
        toggleFirstRowVarNamesNo.setToggleGroup(toggleGroup2);
        toggleFirstRowVarNamesYes.setSelected(true);

        HBox firstRowAreVariableNames = new HBox(10, firstRowAreVariableNamesLabel, toggleFirstRowVarNamesYes,
                toggleFirstRowVarNamesNo);

        var delimiterLabel = new Label("Delimiter:");
        var toggleGroup3 = new ToggleGroup();
        var whitespace = new RadioButton("Whitespace");
        var tab = new RadioButton("Tab");
        var comma = new RadioButton("Comma");
        var semicolon = new RadioButton("Semicolon");
        whitespace.setToggleGroup(toggleGroup3);
        tab.setToggleGroup(toggleGroup3);
        comma.setToggleGroup(toggleGroup3);
        semicolon.setToggleGroup(toggleGroup3);
        whitespace.setSelected(true);

        HBox delimiter = new HBox(10, delimiterLabel, whitespace, tab, comma, semicolon);

        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\D")) {
                textField.setText(newValue.replaceAll("\\D", ""));
            }
        });

        HBox choice = new HBox(10, new Label("Load as:"), mixedBtn, new Label("(Max Categories"), textField, new Label(")"),
                continuousBtn, discreteBtn);

        VBox layout = new VBox(10, firstRowAreVariableNames, choice, delimiter);

        Dialog<VBox> dialog = new Dialog<>();
        dialog.getDialogPane().setContent(layout);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, applyButtonType);

        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(layout.getScene().getWindow());

        ((Button) dialog.getDialogPane().lookupButton(applyButtonType)).setOnAction(e ->
                loadTheData(selectedFile, continuousBtn, discreteBtn, textField, toggleFirstRowVarNamesYes,
                        tab, comma, semicolon));

        dialog.showAndWait();
    }

    /**
     * Loads a graph from a file.
     *
     * @param primaryStage The primary stage.
     */
    private void loadGraphAction(Stage primaryStage) {
        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(primaryStage);

        Graph graph;

        if (selectedFile.getName().endsWith(".txt")) {
            graph = GraphSaveLoadUtils.loadGraphTxt(selectedFile);
        } else if (selectedFile.getName().endsWith(".json")) {
            graph = GraphSaveLoadUtils.loadGraphJson(selectedFile);
        } else {
            throw new RuntimeException("Unknown file type: " + selectedFile.getName());
        }

        Session.getInstance().add(null, graph,
                Utils.nextName(selectedFile.getName(), Session.getInstance().getProjectNames()),
                null, "Graph");
    }

    /**
     * Creates the menu bar.
     *
     * @param primaryStage The primary stage.
     * @return The menu bar.
     */
    @NotNull
    public MenuBar getMenuBar(Stage primaryStage) {
        MenuBar menuBar = new MenuBar();

        Menu fileMenu = new Menu("File");
        MenuItem loadSession = new MenuItem("Load Session");
        MenuItem saveSession = new MenuItem("Save Session");
        fileMenu.getItems().addAll(loadSession, saveSession);

        loadSession.setAccelerator(KeyCombination.keyCombination("Ctrl+O"));
        saveSession.setAccelerator(KeyCombination.keyCombination("Ctrl+S"));

        loadSession.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Tetrad-FX (.tfx) File");

            // Setting the extension filter
            FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter("Tetrad-FX Files", "*.tfx");
            fileChooser.getExtensionFilters().add(imageFilter);

            // Make the imageFilter the default
            fileChooser.setSelectedExtensionFilter(imageFilter);

            File selectedFile = fileChooser.showOpenDialog(primaryStage);
            loadSession(selectedFile, primaryStage);
        });

        saveSession.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Tetrad-FX (.tfx) File");

            // Setting the extension filter
            FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter("Tetrad-FX Files", "*.tfx");
            fileChooser.getExtensionFilters().add(imageFilter);

            // Make the imageFilter the default
            fileChooser.setSelectedExtensionFilter(imageFilter);

            File selectedFile = fileChooser.showSaveDialog(primaryStage);
            String userHomeDirectory = System.getProperty("user.home");

            saveSession(selectedFile, new File(userHomeDirectory, ".tetrad-fx-session"));
        });

        MenuItem deleteSelectedProject = new MenuItem("Delete Selected Project");
        deleteSelectedProject.setOnAction(e -> Session.getInstance().deleteSelectedProject());

        MenuItem loadData = new MenuItem("Load Data");
//        MenuItem loadGraph = new MenuItem("Load Graph"); // Not part of first draft.
        Menu simulation = new Menu("Sample Simulations");
        MenuItem continuousSimulation = new MenuItem("Continuous");
        MenuItem discreteSimulation = new MenuItem("Discrete");
        MenuItem mixedSimulation = new MenuItem("Mixed");
        simulation.getItems().addAll(continuousSimulation, discreteSimulation, mixedSimulation);
        MenuItem exitItem = new MenuItem("Exit");

        loadData.setAccelerator(KeyCombination.keyCombination("Ctrl+D"));
//        loadGraph.setAccelerator(KeyCombination.keyCombination("Ctrl+G"));  // Not part of first draft.
        exitItem.setAccelerator(KeyCombination.keyCombination("Ctrl+Q"));

        loadData.setOnAction(e -> loadDataAction());
//        loadGraph.setOnAction(e -> loadGraphAction(primaryStage));  // Not part of first draft.
        continuousSimulation.setOnAction(e -> addSimulation(SimulationType.CONTINUOUS));
        discreteSimulation.setOnAction(e -> addSimulation(SimulationType.DISCRETE));
        mixedSimulation.setOnAction(e -> addSimulation(SimulationType.MIXED));
        exitItem.setOnAction(e -> primaryStage.close());
//        fileMenu.getItems().addAll(loadData, loadGraph, simulation, new SeparatorMenuItem(), exitItem);
        fileMenu.getItems().addAll(new SeparatorMenuItem(), loadData,
                new SeparatorMenuItem(), deleteSelectedProject,
                new SeparatorMenuItem(), simulation,
                new SeparatorMenuItem(), exitItem);

        Menu search = new Menu("Search");
        Menu searchMenu = new Menu("Search on Selected Data");

        searchMenu.getItems().addAll(MenuItems.searchFromDataMenuItems(Session.getInstance().getParameters(),
                Session.getInstance().getSessionDir()));

        search.getItems().addAll(searchMenu);
        search.getItems().addAll(new SeparatorMenuItem());

        MenuItem knowledge = new MenuItem("Add Knowledge");
//        List<String> variableNames = Session.getInstance().getSelectedProject().getSelectedDataSet().getVariableNames();
//        Knowledge knowledge1 = new Knowledge(variableNames);
        knowledge.setOnAction(e -> {
            List<String> variableNames = Session.getInstance().getSelectedProject().getSelectedDataSet().getVariableNames();
            Knowledge knowledge1 = new Knowledge(variableNames);
            Session.getInstance().getSelectedProject().addKnowledge("Knowledge", knowledge1,
                    true, true);
        });
        searchMenu.getItems().addAll(new SeparatorMenuItem());
        search.getItems().addAll(knowledge);

        Menu insights = new Menu("Insights");
        Menu histogramsAndScatterplots = new Menu("Histograms and scatterplots");
        insights.getItems().addAll(histogramsAndScatterplots);
        histogramsAndScatterplots.getItems().addAll(new MenuItem("Plot Matrix"));
        histogramsAndScatterplots.getItems().addAll(new MenuItem("By Edge/Node"));
        insights.getItems().addAll(new MenuItem("Graph Metrics"));
        insights.getItems().addAll(new MenuItem("Data Metrics"));
        insights.getItems().addAll(new MenuItem("Causal Effects"));
        insights.getItems().addAll(new MenuItem("Check Markov and Faithfulness Assumptions"));
        insights.getItems().addAll(new MenuItem("Check for D-separation/M-separation"));

        Menu games = new Menu("Games");

        Menu permutationGames = new Menu("Permutation Games");

        MenuItem basedOnGraph_4_4 = new MenuItem("Make a random game with 4 nodes and 4 edges");
        MenuItem basedOnGraph_5_5 = new MenuItem("Make a random game with 5 nodes and 5 edges");
        MenuItem basedOnGraph_6_6 = new MenuItem("Make a random game with 6 nodes and 6 edges");
        MenuItem basedOnGraph_7_7 = new MenuItem("Make a random game with 7 nodes and 7 edges");
        MenuItem basedOnGraph_10_10 = new MenuItem("Make a random game with 10 nodes and 10 edges");
        MenuItem basedOnGraph_10_15 = new MenuItem("Make a random game with 10 nodes and 15 edges");
        MenuItem surpriseMe = new MenuItem("Surprise me!");

        basedOnGraph_4_4.setOnAction(e -> Games.baseGamesOnGraph(randomDag(4, 4)));
        basedOnGraph_5_5.setOnAction(e -> Games.baseGamesOnGraph(randomDag(5, 5)));
        basedOnGraph_6_6.setOnAction(e -> Games.baseGamesOnGraph(randomDag(6, 6)));
        basedOnGraph_7_7.setOnAction(e -> Games.baseGamesOnGraph(randomDag(7, 7)));
        basedOnGraph_10_10.setOnAction(e -> Games.baseGamesOnGraph(randomDag(10, 10)));
        basedOnGraph_10_15.setOnAction(e -> Games.baseGamesOnGraph(randomDag(10, 15)));
        surpriseMe.setOnAction(e -> Games.baseGamesOnGraph(randomDag(RandomUtil.getInstance().nextInt(6) + 5,
                RandomUtil.getInstance().nextInt(10) + 3)));

        permutationGames.getItems().addAll(basedOnGraph_4_4, basedOnGraph_5_5, basedOnGraph_6_6, basedOnGraph_7_7,
                basedOnGraph_10_10, basedOnGraph_10_15, surpriseMe);
        games.getItems().addAll(permutationGames);

        Menu help = new Menu("Help");
        help.getItems().addAll(new MenuItem("About"));
        help.getItems().addAll(new MenuItem("Help"));
        help.getItems().addAll(new MenuItem("Tetrad Website"));
        help.getItems().addAll(new MenuItem("Tetrad Manual"));
        help.getItems().addAll(new MenuItem("Tetrad Forum"));

        menuBar.getMenus().addAll(fileMenu, games);
        return menuBar;
    }

    /**
     * Creates a random DAG.
     *
     * @param numNodes The number of nodes.
     * @param numEdges The number of edges.
     * @return The random DAG.
     */
    @NotNull
    private static Graph randomDag(int numNodes, int numEdges) {
        return RandomGraph.randomGraph(numNodes, 0, numEdges, 100, 100,
                100, false);
    }

    /**
     * Loads data from a file.
     *
     * @param selectedFile  The selected file.
     * @param continuousBtn The continuous button.
     * @param discreteBtn   The discrete button.
     * @param textField     The text field.
     */
    private void loadTheData(File selectedFile, RadioButton continuousBtn, RadioButton discreteBtn,
                             TextField textField, RadioButton hasHeaderBtnYes,
                             RadioButton tab, RadioButton comma, RadioButton semicolon) {
        if (selectedFile != null) {
            boolean hasHeader = hasHeaderBtnYes.isSelected();

            Delimiter delimiter1 = Delimiter.WHITESPACE;
            if (tab.isSelected()) {
                delimiter1 = Delimiter.TAB;
            } else if (comma.isSelected()) {
                delimiter1 = Delimiter.COMMA;
            } else if (semicolon.isSelected()) {
                delimiter1 = Delimiter.SEMICOLON;
            }

            Delimiter delimiter = delimiter1;

            // You can add further processing based on the type of dataset chosen.
            if (continuousBtn.isSelected()) {
                loadContinuous(selectedFile, hasHeader, delimiter);
            } else if (discreteBtn.isSelected()) {
                loadDiscrete(selectedFile, hasHeader, delimiter);
            } else {
                loadMixed(selectedFile, textField, hasHeader, delimiter);
            }
        } else {
            System.out.println("File selection cancelled.");
        }
    }

    /**
     * Loads continuous data.
     *
     * @param selectedFile The selected file.
     */
    private void loadContinuous(File selectedFile, boolean hasHeader, Delimiter delimiter) {
        try {
            DataSet dataSet = ChangedStuffINeed.loadContinuousData(selectedFile, "//", '\"',
                    "*", hasHeader, delimiter, false);
            String name = selectedFile.getName();
            dataSet.setName(name);
            Session.getInstance().add(dataSet, null, Utils.nextName(selectedFile.getName(),
                            Session.getInstance().getProjectNames()),
                    "Data", null);
        } catch (IOException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Error loading continuous data.");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Loads discrete data.
     *
     * @param selectedFile The selected file.
     */
    private void loadDiscrete(File selectedFile, boolean hasHeader, Delimiter delimiter) {
        try {
            DataSet dataSet = ChangedStuffINeed.loadDiscreteData(selectedFile, "//",
                    '\"', "*", hasHeader, delimiter, false);
            Session.getInstance().add(dataSet, null, Utils.nextName(selectedFile.getName(),
                            Session.getInstance().getProjectNames()),
                    "Data", null);
        } catch (IOException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Error loading discrete data.");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Loads mixed data.
     *
     * @param selectedFile The selected file.
     * @param textField    The text field.
     */
    private void loadMixed(File selectedFile, TextField textField, boolean hasHeader, Delimiter delimiter) {
        try {
            int maxNumCategories = Integer.parseInt(textField.getText());
            DataSet dataSet = ChangedStuffINeed.loadMixedData(selectedFile, "//", '\"',
                    "*", hasHeader, maxNumCategories, delimiter, false);
            Session.getInstance().add(dataSet, null, Utils.nextName(selectedFile.getName(),
                            Session.getInstance().getProjectNames()),
                    "Data", null);
        } catch (IOException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Error loading mixed data.");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Adds a simulation to the session.
     *
     * @param type The type of simulation.
     */
    private void addSimulation(SimulationType type) {
        Result result = getSimulation(new Parameters(), type);
        String newName = Utils.nextName("Simulation", Session.getInstance().getProjectNames());
        Session.getInstance().add(result.dataSet(), result.graph(), newName,
                "simulated_data", "true_graph");
    }

    /**
     * Saves the session.
     *
     * @param zipFile The zip file.
     * @param dir     The directory.
     */
    private static void saveSession(File zipFile, File dir) {
        ChangedStuffINeed.zip(dir, zipFile);
    }

    /**
     * Loads the session.
     *
     * @param zipFile      The zip file.
     * @param primaryStage The primary stage.
     */
    private void loadSession(File zipFile, Stage primaryStage) {
        String userHomeDirectory = System.getProperty("user.home");
        File dir = new File(userHomeDirectory, ".tetrad-fx-session").getAbsoluteFile();

        try {
            ChangedStuffINeed.deleteDirectory(dir.toPath());
            boolean created = dir.mkdir();

            if (!created) {
                throw new RuntimeException("Could not create directory: " + dir.getAbsolutePath());
            }

            ChangedStuffINeed.unzipDirectory(zipFile.getAbsolutePath(), dir.getAbsolutePath());

            Session.newInstance();

            Scene scene = new Scene(TetradFxMain.getInstance().getRoot(primaryStage));
            primaryStage.setScene(scene);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * The type of simulation.
     */
    public enum SimulationType {
        CONTINUOUS,
        DISCRETE,
        MIXED
    }

    /**
     * The result of a simulation.
     *
     * @param graph   The graph.
     * @param dataSet The data set.
     */
    private record Result(Graph graph, DataSet dataSet) {
    }
}


