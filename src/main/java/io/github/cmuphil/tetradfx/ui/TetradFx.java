package io.github.cmuphil.tetradfx.ui;

import edu.cmu.tetrad.algcomparison.graph.RandomForward;
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

/**
 * <p>The main display for Tetrad-FX. Work in progress.</p>
 *
 * @author josephramsey
 */
public class TetradFx {
    private static final TetradFx INSTANCE = new TetradFx();

    public static TetradFx getInstance() {
        return TetradFx.INSTANCE;
    }

    private static void loadDataAction(Stage primaryStage, TabPane tabs) {
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
    private static MenuBar getMenuBar(Stage primaryStage, TabPane tabs) {
        MenuBar menuBar = new MenuBar();

        Menu fileMenu = new Menu("File");
        MenuItem loadData = new MenuItem("Load Data");

        Menu simulation = new Menu("Simulation");
        MenuItem continuousSimulation1 = new MenuItem("Continuous");
        MenuItem mixedSimulation = new MenuItem("Mixed");
        simulation.getItems().addAll(continuousSimulation1, mixedSimulation);
        MenuItem exitItem = new MenuItem("Exit");

        loadData.setAccelerator(KeyCombination.keyCombination("Ctrl+O"));
        continuousSimulation1.setAccelerator(KeyCombination.keyCombination("Ctrl+C"));
        mixedSimulation.setAccelerator(KeyCombination.keyCombination("Ctrl+M"));
        exitItem.setAccelerator(KeyCombination.keyCombination("Ctrl+Q"));

        loadData.setOnAction(e -> loadDataAction(primaryStage, tabs));
        continuousSimulation1.setOnAction(e -> addSimulation(tabs, false));
        mixedSimulation.setOnAction(e -> addSimulation(tabs, true));
        exitItem.setOnAction(e -> primaryStage.close());
        fileMenu.getItems().addAll(loadData, simulation, new SeparatorMenuItem(), exitItem);

        menuBar.getMenus().addAll(fileMenu);
        return menuBar;
    }

    // This will eventually be replaced by some flexible UI for making simulations.
    @NotNull
    private static Result getSimulation(Parameters parameters, boolean mixed) {
        if (mixed) {
            LeeHastieSimulation simulation = new LeeHastieSimulation(new RandomForward());
            simulation.createData(parameters, true);
            Graph graph = simulation.getTrueGraph(0);
            DataSet dataSet = (DataSet) simulation.getDataModel(0);
            return new Result(graph, dataSet);
        } else {
            Graph graph = RandomGraph.randomGraphRandomForwardEdges(100, 0,
                    200, 500, 100, 1000, false);

            LargeScaleSimulation simulation = new LargeScaleSimulation(graph);
            simulation.setCoefRange(0, 0.5);
            simulation.setSelfLoopCoef(0.1);
            DataSet dataSet = simulation.simulateDataReducedForm(1000);
            return new Result(graph, dataSet);
        }
    }

    private static void loadTheData(File selectedFile, RadioButton continuousBtn, RadioButton discreteBtn, TextField textField, TabPane tabs) {
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

    private static void loadContinuous(File selectedFile, TabPane tabbedPane) {
        try {
            DataSet dataSet = SimpleDataLoader.loadContinuousData(selectedFile, "//", '\"',
                    "*", true, Delimiter.TAB, false);
            Tab tab = new Tab(selectedFile.getName(), DataView.getTableView(dataSet, tabbedPane));
            tabbedPane.getTabs().clear();
            tabbedPane.getTabs().add(tab);
            tabbedPane.getSelectionModel().select(tab);
        } catch (IOException ex) {
            System.out.println("Error loading continuous data.");
            throw new RuntimeException(ex);
        }
    }

    private static void loadDiscrete(File selectedFile, TabPane tabbedPane) {
        try {
            DataSet dataSet = SimpleDataLoader.loadDiscreteData(selectedFile, "//",
                    '\"', "*", true, Delimiter.TAB, false);
            Tab tab = new Tab(selectedFile.getName(), DataView.getTableView(dataSet, tabbedPane));
            tabbedPane.getTabs().clear();
            tabbedPane.getTabs().add(tab);
            tabbedPane.getSelectionModel().select(tab);

        } catch (IOException ex) {
            System.out.println("Error loading discrete data.");
            throw new RuntimeException(ex);
        }
    }

    private static void loadMixed(File selectedFile, TextField textField, TabPane tabbedPane) {
        try {
            int maxNumCategories = Integer.parseInt(textField.getText());
            DataSet dataSet = SimpleDataLoader.loadMixedData(selectedFile, "//", '\"',
                    "*", true, maxNumCategories, Delimiter.TAB, false);
            Tab tab = new Tab(selectedFile.getName(), DataView.getTableView(dataSet, tabbedPane));
            tabbedPane.getTabs().clear();
            tabbedPane.getTabs().add(tab);
            tabbedPane.getSelectionModel().select(tab);
        } catch (IOException ex) {
            System.out.println("Error loading mixed data.");
            throw new RuntimeException(ex);
        }
    }

    // Passing primaryStage in here so that I can quit the application from a menu item
    // and pop up dialogs.
    public Pane getRoot(Stage primaryStage) {
        TabPane tabbedPane = new TabPane();
        tabbedPane.setSide(Side.TOP);

        BorderPane root = new BorderPane();
        MenuBar menuBar = getMenuBar(primaryStage, tabbedPane);
        root.setTop(menuBar);

        tabbedPane.setPrefSize(1000, 800);
        root.setCenter(tabbedPane);

        return root;
    }

    private static void addSimulation( TabPane tabbedPane, boolean mixed) {
        Result result = getSimulation(new Parameters(), mixed);
        System.out.println("Simulation done");

        TableView<DataView.DataRow> table = DataView.getTableView(result.dataSet(), tabbedPane);
        ScrollPane trueGraphScroll = GraphView.getGraphDisplay(result.graph());

        Tab t1 = new Tab("s2-graph", trueGraphScroll);
        Tab t2 = new Tab("s1-data", table);

        tabbedPane.getTabs().clear();
        tabbedPane.getTabs().add(t1);
        tabbedPane.getTabs().add(t2);

        tabbedPane.getSelectionModel().select(t2);
    }

    private record Result(Graph graph, DataSet dataSet) {
    }
}


