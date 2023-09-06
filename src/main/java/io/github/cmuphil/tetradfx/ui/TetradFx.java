package io.github.cmuphil.tetradfx.ui;

import edu.cmu.tetrad.algcomparison.graph.RandomForward;
import edu.cmu.tetrad.algcomparison.simulation.BayesNetSimulation;
import edu.cmu.tetrad.algcomparison.simulation.LeeHastieSimulation;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.SimpleDataLoader;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.RandomGraph;
import edu.cmu.tetrad.sem.LargeScaleSimulation;
import edu.cmu.tetrad.util.Parameters;
import edu.pitt.dbmi.data.reader.Delimiter;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

import static edu.cmu.tetrad.data.SimpleDataLoader.loadMixedData;

/**
 * <p>The main display for Tetrad-FX. This one uses a split pane layout. Work in progress.</p>
 *
 * @author josephramsey
 */
public class TetradFx {
    private static final TetradFx INSTANCE = new TetradFx();
    private final TabPane mainTabs = new TabPane();
    private final TabPane graphs = new TabPane();
    private Tab dataTab;

    public static TetradFx getInstance() {
        return TetradFx.INSTANCE;
    }

    // Passing primaryStage in here so that I can quit the application from a menu item
    // and pop up dialogs.
    public Pane getRoot(Stage primaryStage) {
        TreeItem<String> rootItem = new TreeItem<>("Datasets");

        for (int i = 1; i <= 10; i++) {
            TreeItem<String> childItem1 = new TreeItem<>("data set " + i);
            rootItem.getChildren().add(childItem1);
        }

        rootItem.setExpanded(true);

        TreeView<String> treeView = new TreeView<>(rootItem);

        BorderPane activePane = new BorderPane();
        MenuBar menuBar = getMenuBar(primaryStage);
        activePane.setTop(menuBar);

        mainTabs.setPrefSize(1000, 800);
        mainTabs.setSide(Side.LEFT);

        dataTab = new Tab("Data", new Pane());
        Tab graphTab = new Tab("Graphs", graphs);
        Tab modelsTab = new Tab("Models", new Pane());
        Tab insightsTab = new Tab("Insights", new Pane());
        Tab gamesTab = new Tab("Games", new Pane());

        dataTab.setClosable(false);
        graphTab.setClosable(false);
        modelsTab.setClosable(false);
        insightsTab.setClosable(false);
        gamesTab.setClosable(false);

        mainTabs.getTabs().add(dataTab);
        mainTabs.getTabs().add(graphTab);
        mainTabs.getTabs().add(modelsTab);
        mainTabs.getTabs().add(insightsTab);
        mainTabs.getTabs().add(gamesTab);

        activePane.setCenter(mainTabs);

        sampleSimulation();

        SplitPane mainSplit = new SplitPane();
        mainSplit.setDividerPosition(0, 0.2);

        mainSplit.getItems().addAll(treeView, activePane);

        BorderPane root = new BorderPane();
        root.setCenter(mainSplit);
        root.setTop(menuBar);

        return root;
    }

    // This will eventually be replaced by some flexible UI for making simulations.
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

    private void loadDataAction(Stage primaryStage) {
        System.out.println("Loading data.");

        ButtonType applyButtonType = new ButtonType("Load");

        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(primaryStage);

        RadioButton continuousBtn = new RadioButton("Optimize for Continuous");
        RadioButton discreteBtn = new RadioButton("Optimize for Discrete");
        RadioButton mixedBtn = new RadioButton("General");
        ToggleGroup toggleGroup = new ToggleGroup();
        continuousBtn.setToggleGroup(toggleGroup);
        discreteBtn.setToggleGroup(toggleGroup);
        mixedBtn.setToggleGroup(toggleGroup);
        mixedBtn.setSelected(true);

        TextField textField = new TextField("3");
        textField.setPrefColumnCount(2);

        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\D")) {
                textField.setText(newValue.replaceAll("\\D", ""));
            }
        });

        HBox choice = new HBox(10, mixedBtn, new Label("(Max Categories"), textField, new Label(")"),
                continuousBtn, discreteBtn);
        VBox layout = new VBox(10, choice);

        Dialog<VBox> dialog = new Dialog<>();
        dialog.getDialogPane().setContent(layout);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, applyButtonType);

        ((Button) dialog.getDialogPane().lookupButton(applyButtonType)).setOnAction(e ->
                loadTheData(selectedFile, continuousBtn, discreteBtn, textField));

        dialog.showAndWait();
    }

    @NotNull
    public MenuBar getMenuBar(Stage primaryStage) {
        MenuBar menuBar = new MenuBar();

        Menu fileMenu = new Menu("File");
        MenuItem loadData = new MenuItem("Load Data");

        Menu simulation = new Menu("Simulation");
        MenuItem continuousSimulation = new MenuItem("Continuous");
        MenuItem discreteSimulation = new MenuItem("Discrete");
        MenuItem mixedSimulation = new MenuItem("Mixed");
        simulation.getItems().addAll(continuousSimulation, discreteSimulation, mixedSimulation);
        MenuItem exitItem = new MenuItem("Exit");

        loadData.setAccelerator(KeyCombination.keyCombination("Ctrl+O"));
        continuousSimulation.setAccelerator(KeyCombination.keyCombination("Ctrl+C"));
        discreteSimulation.setAccelerator(KeyCombination.keyCombination("Ctrl+D"));
        mixedSimulation.setAccelerator(KeyCombination.keyCombination("Ctrl+M"));
        exitItem.setAccelerator(KeyCombination.keyCombination("Ctrl+Q"));

        loadData.setOnAction(e -> loadDataAction(primaryStage));
        continuousSimulation.setOnAction(e -> addSimulation(SimulationType.CONTINUOUS, graphs));
        discreteSimulation.setOnAction(e -> addSimulation(SimulationType.DISCRETE, graphs));
        mixedSimulation.setOnAction(e -> addSimulation(SimulationType.MIXED, graphs));
        exitItem.setOnAction(e -> primaryStage.close());
        fileMenu.getItems().addAll(loadData, simulation, new SeparatorMenuItem(), exitItem);

        menuBar.getMenus().addAll(fileMenu);
        return menuBar;
    }

    private void loadTheData(File selectedFile, RadioButton continuousBtn, RadioButton discreteBtn, TextField textField) {
        if (selectedFile != null) {

            // You can add further processing based on the type of dataset chosen.
            if (continuousBtn.isSelected()) {
                loadContinuous(selectedFile);
            } else if (discreteBtn.isSelected()) {
                loadDiscrete(selectedFile);
            } else {
                loadMixed(selectedFile, textField);
            }
        } else {
            System.out.println("File selection cancelled.");
        }
    }

    private void loadContinuous(File selectedFile) {
        try {
            DataSet dataSet = SimpleDataLoader.loadContinuousData(selectedFile, "//", '\"',
                    "*", true, Delimiter.TAB, false);
            TableView<DataView.DataRow> table = DataView.getTableView(dataSet, mainTabs, graphs);
            dataTab.setContent(table);
            graphs.getTabs().removeAll(graphs.getTabs());

            mainTabs.getSelectionModel().select(mainTabs.getTabs().get(0));
        } catch (IOException ex) {
            System.out.println("Error loading continuous data.");
            throw new RuntimeException(ex);
        }
    }

    private void loadDiscrete(File selectedFile) {
        try {
            DataSet dataSet = SimpleDataLoader.loadDiscreteData(selectedFile, "//",
                    '\"', "*", true, Delimiter.TAB, false);
            TableView<DataView.DataRow> table = DataView.getTableView(dataSet, mainTabs, graphs);
            dataTab.setContent(table);
            graphs.getTabs().removeAll(graphs.getTabs());

            mainTabs.getSelectionModel().select(mainTabs.getTabs().get(0));
        } catch (IOException ex) {
            System.out.println("Error loading discrete data.");
            throw new RuntimeException(ex);
        }
    }

    private void loadMixed(File selectedFile, TextField textField) {
        try {
            int maxNumCategories = Integer.parseInt(textField.getText());
            DataSet dataSet = loadMixedData(selectedFile, "//", '\"',
                    "*", true, maxNumCategories, Delimiter.TAB, false);
            TableView<DataView.DataRow> table = DataView.getTableView(dataSet, mainTabs, graphs);
            dataTab.setContent(table);
            graphs.getTabs().removeAll(graphs.getTabs());

            mainTabs.getSelectionModel().select(mainTabs.getTabs().get(0));
        } catch (IOException ex) {
            System.out.println("Error loading mixed data.");
            throw new RuntimeException(ex);
        }
    }

    private void sampleSimulation() {
        Graph graph = RandomGraph.randomGraphRandomForwardEdges(10, 0,
                20, 500, 100, 1000, false);

        LargeScaleSimulation simulation = new LargeScaleSimulation(graph);
        simulation.setCoefRange(0, 0.5);
        simulation.setSelfLoopCoef(0.1);
        DataSet dataSet = simulation.simulateDataReducedForm(1000);

        TableView<DataView.DataRow> table = DataView.getTableView(dataSet, mainTabs, graphs);
        ScrollPane trueGraphScroll = GraphView.getGraphDisplay(graph);

        dataTab.setContent(table);

        Tab t2 = new Tab("True", trueGraphScroll);
        graphs.getTabs().add(t2);

        mainTabs.getSelectionModel().select(mainTabs.getTabs().get(0));
    }

    private void addSimulation(SimulationType type, TabPane graphs) {
        Result result = getSimulation(new Parameters(), type);
        System.out.println("Simulation done");

        TableView<DataView.DataRow> table = DataView.getTableView(result.dataSet(), mainTabs, graphs);
        ScrollPane trueGraphScroll = GraphView.getGraphDisplay(result.graph());

        Tab t2 = new Tab("True", trueGraphScroll);

        dataTab.setContent(table);
        graphs.getTabs().removeAll(graphs.getTabs());
        graphs.getTabs().add(t2);

        mainTabs.getSelectionModel().select(mainTabs.getTabs().get(0));
    }



    public enum SimulationType {
        CONTINUOUS,
        DISCRETE,
        MIXED
    }

    private record Result(Graph graph, DataSet dataSet) {
    }
}


