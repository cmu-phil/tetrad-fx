package io.github.cmuphil.tetradfx.ui;

import edu.cmu.tetrad.algcomparison.algorithm.oracle.pag.Gfci;
import edu.cmu.tetrad.algcomparison.graph.RandomForward;
import edu.cmu.tetrad.algcomparison.independence.SemBicDTest;
import edu.cmu.tetrad.algcomparison.score.SemBicScore;
import edu.cmu.tetrad.algcomparison.simulation.LeeHastieSimulation;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.SimpleDataLoader;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.RandomGraph;
import edu.cmu.tetrad.sem.LargeScaleSimulation;
import edu.cmu.tetrad.util.Parameters;
import edu.pitt.dbmi.data.reader.Delimiter;
import javafx.scene.control.*;
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

        ButtonType applyButtonType = new ButtonType("Apply");

        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(primaryStage);

        RadioButton continuousBtn = new RadioButton("Continuous Dataset");
        RadioButton discreteBtn = new RadioButton("Discrete Dataset");
        RadioButton mixedBtn = new RadioButton("Mixed Dataset");
        ToggleGroup toggleGroup = new ToggleGroup();
        continuousBtn.setToggleGroup(toggleGroup);
        discreteBtn.setToggleGroup(toggleGroup);
        mixedBtn.setToggleGroup(toggleGroup);
        continuousBtn.setSelected(true);

        TextField textField = new TextField("3");

        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                textField.setText(newValue.replaceAll("\\D", ""));
            }
        });

        HBox choice = new HBox(10, continuousBtn, discreteBtn, mixedBtn,
                new Label("Number of Categories:"), textField);
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
        MenuItem exitItem = new MenuItem("Exit");

        loadData.setOnAction(e -> loadDataAction(primaryStage, tabs));
        exitItem.setOnAction(e -> primaryStage.close());
        fileMenu.getItems().addAll(loadData, new SeparatorMenuItem(), exitItem);

        menuBar.getMenus().addAll(fileMenu);
        return menuBar;
    }

    // This will eventually be replaced by some flexible UI for making simulations.
    @NotNull
    private static Result getSimulation(Parameters parameters) {

        // Temporary hack to switch between mixed and continuous simulations.
        boolean mixed = false;

        DataSet dataSet;
        Graph graph;
        if (mixed) {
            LeeHastieSimulation simulation = new LeeHastieSimulation(new RandomForward());
            simulation.createData(parameters, true);
            graph = simulation.getTrueGraph(0);
            dataSet = (DataSet) simulation.getDataModel(0);
        } else {
            graph = RandomGraph.randomGraphRandomForwardEdges(10, 0,
                    10, 500, 100, 1000, false);

            LargeScaleSimulation simulation = new LargeScaleSimulation(graph);
            simulation.setCoefRange(0, 0.5);
            simulation.setSelfLoopCoef(0.1);
            dataSet = simulation.simulateDataReducedForm(1000);
        }
        return new Result(graph, dataSet);
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

    private static void loadContinuous(File selectedFile, TabPane tabs) {
        try {
            DataSet dataSet = SimpleDataLoader.loadContinuousData(selectedFile, "//", '\"',
                    "*", true, Delimiter.TAB, false);
            tabs.getTabs().add(new Tab(selectedFile.getName(), DataView.getTableView(dataSet)));
        } catch (IOException ex) {
            System.out.println("Error loading continuous data.");
            throw new RuntimeException(ex);
        }
    }

    private static void loadDiscrete(File selectedFile, TabPane tabs) {
        try {
            DataSet dataSet = SimpleDataLoader.loadDiscreteData(selectedFile, "//",
                    '\"', "*", true, Delimiter.TAB, false);
            tabs.getTabs().add(new Tab(selectedFile.getName(), DataView.getTableView(dataSet)));
        } catch (IOException ex) {
            System.out.println("Error loading discrete data.");
            throw new RuntimeException(ex);
        }
    }

    private static void loadMixed(File selectedFile, TextField textField, TabPane tabs) {
        try {
            int maxNumCategories = Integer.parseInt(textField.getText());
            DataSet dataSet = SimpleDataLoader.loadMixedData(selectedFile, "//", '\"',
                    "*", true, maxNumCategories, Delimiter.TAB, false);
            tabs.getTabs().add(new Tab(selectedFile.getName(), DataView.getTableView(dataSet)));
        } catch (IOException ex) {
            System.out.println("Error loading mixed data.");
            throw new RuntimeException(ex);
        }
    }

    private record Result(Graph graph, DataSet dataSet) {
    }

    // Passing primaryStage in here so that I can quit the application from a menu item
    // and pop up dialogs.
    public Pane getRoot(Stage primaryStage) {
        TabPane tabs = new TabPane();

        Result result = getSimulation(new Parameters());
        System.out.println("Simulation done");

        TableView<DataView.DataRow> table = DataView.getTableView(result.dataSet());
        ScrollPane trueGraphScroll = GraphView.getGraphDisplay(result.graph());

        Gfci fci = new Gfci(new SemBicDTest(), new SemBicScore());
        Graph pag = fci.search(result.dataSet(), new Parameters());

        MenuBar menuBar = getMenuBar(primaryStage, tabs);

        BorderPane root = new BorderPane();
        root.setTop(menuBar);

        tabs.getTabs().add(new Tab("s1-data", table));
        tabs.getTabs().add(new Tab("s2-graph", trueGraphScroll));
        tabs.getTabs().add(new Tab("fci", GraphView.getGraphDisplay(pag)));

        tabs.setPrefSize(800, 600);
        root.setCenter(tabs);

        return root;
    }
}


