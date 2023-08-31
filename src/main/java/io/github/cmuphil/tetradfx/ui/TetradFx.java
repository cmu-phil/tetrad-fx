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

    // Passing primaryStage in here so that I can quit the application from a menu item.
    public Pane getRoot(Stage primaryStage) {
        TabPane tabs = new TabPane();

        Result result = getSimulation(new Parameters(), true);
        System.out.println("Simulation done");

        TableView<DataView.DataRow> table = DataView.getTableView(result.dataSet());
        ScrollPane trueGraphScroll = GraphView.getGraphDisplay(result.graph());

        // Create the menu bar
        MenuBar menuBar = new MenuBar();

        // Create the File menu with some menu items
        Menu fileMenu = new Menu("File");
        MenuItem loadData = new MenuItem("Load Data");
        MenuItem exitItem = new MenuItem("Exit");

        loadData.setOnAction(e -> loadDataAction(primaryStage, tabs));
        exitItem.setOnAction(e -> primaryStage.close());
        fileMenu.getItems().addAll(loadData, new SeparatorMenuItem(), exitItem);

        // Add menus to the menu bar
        menuBar.getMenus().addAll(fileMenu);

        // Add the menu bar to the main scene
        BorderPane root = new BorderPane();
        root.setTop(menuBar);

        tabs.getTabs().add(new Tab("Tab 1", table));
        tabs.getTabs().add(new Tab("Tab 2", trueGraphScroll));

        tabs.setPrefSize(300, 300);

        root.setCenter(tabs);

        return root;
    }

    private static void loadDataAction(Stage primaryStage, TabPane tabs) {
        System.out.println("Loading data.");

        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(primaryStage);

        // Radio buttons setup
        RadioButton continuousBtn = new RadioButton("Continuous Dataset");
        RadioButton discreteBtn = new RadioButton("Discrete Dataset");
        ToggleGroup toggleGroup = new ToggleGroup();
        continuousBtn.setToggleGroup(toggleGroup);
        discreteBtn.setToggleGroup(toggleGroup);
        continuousBtn.setSelected(true);  // Default selected radio button

        Button loadDataBtn = loadDataBtn(selectedFile, continuousBtn, tabs);

        HBox choice = new HBox(10, continuousBtn, discreteBtn);
        VBox layout = new VBox(10, choice, loadDataBtn);

        Dialog<VBox> dialog = new Dialog<>();
        dialog.getDialogPane().setContent(layout);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.APPLY, ButtonType.CLOSE);
        dialog.showAndWait();
    }

    @NotNull
    private static Button loadDataBtn(File selectedFile, RadioButton continuousBtn, TabPane tabs) {
        // Putting everything together
        // Load data button setup
        Button loadDataBtn = new Button("Load Data");

        loadDataBtn.setOnAction(e2 -> {
            if (selectedFile != null) {
                System.out.println("File selected: " + selectedFile.getAbsolutePath());

                DataSet dataSet;

                // You can add further processing based on the type of dataset chosen.
                if (continuousBtn.isSelected()) {
                    try {
                        dataSet = SimpleDataLoader.loadContinuousData(selectedFile, "//", '\"',
                                "*", true, Delimiter.TAB);
                        tabs.getTabs().add(new Tab(selectedFile.getName(), DataView.getTableView(dataSet)));
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                } else {
                    try {
                        dataSet = SimpleDataLoader.loadDiscreteData(selectedFile, "//", '\"',
                                "*", true, Delimiter.TAB);
                        tabs.getTabs().add(new Tab(selectedFile.getName(), DataView.getTableView(dataSet)));
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        throw new RuntimeException(ex);
                    }
                }

            } else {
                System.out.println("File selection cancelled.");
            }
        });
        return loadDataBtn;
    }

    private record Result(Graph graph, DataSet dataSet) {
    }

    // This will eventually be replaced by some flexible method for making simulations.
    @NotNull
    private static Result getSimulation(Parameters parameters, boolean mixed) {
        DataSet dataSet;
        Graph graph;
        if (mixed) {
            LeeHastieSimulation simulation = new LeeHastieSimulation(new RandomForward());
            simulation.createData(parameters, true);
            graph = simulation.getTrueGraph(0);
            dataSet = (DataSet) simulation.getDataModel(0);
        } else {
            graph = RandomGraph.randomGraphRandomForwardEdges(1000, 0,
                    2000, 500, 100, 1000, false);

            LargeScaleSimulation simulation = new LargeScaleSimulation(graph);
            simulation.setCoefRange(0, 0.5);
            simulation.setSelfLoopCoef(0.1);
            dataSet = simulation.simulateDataReducedForm(1000);
        }
        return new Result(graph, dataSet);
    }
}


