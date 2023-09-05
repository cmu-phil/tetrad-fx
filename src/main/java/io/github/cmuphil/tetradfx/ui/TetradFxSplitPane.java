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
import javafx.scene.Node;
import javafx.scene.control.*;
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
 * <p>The main display for Tetrad-FX. Work in progress.</p>
 *
 * @author josephramsey
 */
public class TetradFxSplitPane {
    private static final TetradFxSplitPane INSTANCE = new TetradFxSplitPane();
    private final TabPane graphs = new TabPane();

    public static TetradFxSplitPane getInstance() {
        return TetradFxSplitPane.INSTANCE;
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

    private void loadDataAction(Stage primaryStage, SplitPane tabs) {
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
                loadTheData(selectedFile, continuousBtn, discreteBtn, textField, tabs));

        dialog.showAndWait();
    }

    @NotNull
    private MenuBar getMenuBar(Stage primaryStage, SplitPane tabs) {
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

        loadData.setOnAction(e -> loadDataAction(primaryStage, tabs));
        continuousSimulation.setOnAction(e -> addSimulation(tabs, SimulationType.CONTINUOUS));
        discreteSimulation.setOnAction(e -> addSimulation(tabs, SimulationType.DISCRETE));
        mixedSimulation.setOnAction(e -> addSimulation(tabs, SimulationType.MIXED));
        exitItem.setOnAction(e -> primaryStage.close());
        fileMenu.getItems().addAll(loadData, simulation, new SeparatorMenuItem(), exitItem);

        menuBar.getMenus().addAll(fileMenu);
        return menuBar;
    }

    private void loadTheData(File selectedFile, RadioButton continuousBtn, RadioButton discreteBtn, TextField textField, SplitPane tabs) {
        if (selectedFile != null) {

            // You can add further processing based on the type of dataset chosen.
            if (continuousBtn.isSelected()) {
                loadContinuous(selectedFile, tabs);
            } else if (discreteBtn.isSelected()) {
                loadDiscrete(selectedFile, tabs);
            } else {
                loadMixed(selectedFile, textField, tabs);
            }
        } else {
            System.out.println("File selection cancelled.");
        }
    }

    private void loadContinuous(File selectedFile, SplitPane tabs) {
        try {
            DataSet dataSet = SimpleDataLoader.loadContinuousData(selectedFile, "//", '\"',
                    "*", true, Delimiter.TAB, false);
            Node pane = DataViewSplitPane.getTableView(dataSet, tabs);
            tabs.getItems().set(0, pane);
            graphs.getTabs().removeAll(graphs.getTabs());
        } catch (IOException ex) {
            System.out.println("Error loading continuous data.");
            throw new RuntimeException(ex);
        }
    }

    private void loadDiscrete(File selectedFile, SplitPane tabs) {
        try {
            DataSet dataSet = SimpleDataLoader.loadDiscreteData(selectedFile, "//",
                    '\"', "*", true, Delimiter.TAB, false);
            Node node = DataViewSplitPane.getTableView(dataSet, tabs);
            tabs.getItems().set(0, node);
            graphs.getTabs().removeAll(graphs.getTabs());
        } catch (IOException ex) {
            System.out.println("Error loading discrete data.");
            throw new RuntimeException(ex);
        }
    }

    private void loadMixed(File selectedFile, TextField textField, SplitPane split) {
        try {
            int maxNumCategories = Integer.parseInt(textField.getText());
            DataSet dataSet = loadMixedData(selectedFile, "//", '\"',
                    "*", true, maxNumCategories, Delimiter.TAB, false);
            Node node = DataViewSplitPane.getTableView(dataSet, split);
            split.getItems().set(0, node);
            graphs.getTabs().removeAll(graphs.getTabs());
        } catch (IOException ex) {
            System.out.println("Error loading mixed data.");
            throw new RuntimeException(ex);
        }
    }

    private void sampleSimulation(SplitPane split) {
        Graph graph = RandomGraph.randomGraphRandomForwardEdges(10, 0,
                20, 500, 100, 1000, false);

        LargeScaleSimulation simulation = new LargeScaleSimulation(graph);
        simulation.setCoefRange(0, 0.5);
        simulation.setSelfLoopCoef(0.1);
        DataSet dataSet = simulation.simulateDataReducedForm(1000);

        TableView<DataViewSplitPane.DataRow> table = DataViewSplitPane.getTableView(dataSet, split);
        ScrollPane trueGraphScroll = GraphView.getGraphDisplay(graph);

        Tab t2 = new Tab("True", trueGraphScroll);
        graphs.getTabs().add(t2);
        split.getItems().addAll(table, graphs);
    }

    private void addSimulation(SplitPane split, SimulationType type) {
        Result result = getSimulation(new Parameters(), type);
        System.out.println("Simulation done");

        TableView<DataViewSplitPane.DataRow> table = DataViewSplitPane.getTableView(result.dataSet(), split);
        ScrollPane trueGraphScroll = GraphView.getGraphDisplay(result.graph());

        Tab t2 = new Tab("True", trueGraphScroll);

        split.getItems().set(0, table);
        graphs.getTabs().removeAll(graphs.getTabs());
        graphs.getTabs().add(t2);
    }

    // Passing primaryStage in here so that I can quit the application from a menu item
    // and pop up dialogs.
    public Pane getRoot(Stage primaryStage) {
        SplitPane split = new SplitPane();

        BorderPane root = new BorderPane();
        MenuBar menuBar = getMenuBar(primaryStage, split);
        root.setTop(menuBar);

        split.setPrefSize(1000, 800);
        root.setCenter(split);

        sampleSimulation(split);

        return root;
    }

    public enum SimulationType {
        CONTINUOUS,
        DISCRETE,
        MIXED
    }

    private record Result(Graph graph, DataSet dataSet) {
    }
}


