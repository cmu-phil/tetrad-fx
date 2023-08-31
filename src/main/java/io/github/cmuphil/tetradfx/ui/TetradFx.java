package io.github.cmuphil.tetradfx.ui;

import edu.cmu.tetrad.algcomparison.graph.RandomForward;
import edu.cmu.tetrad.algcomparison.simulation.LeeHastieSimulation;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.SimpleDataLoader;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;
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
import org.apache.commons.math3.util.FastMath;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * <p></p>The main display for Tetrad-FX. Currently, displays a graph, a data set, and a search result.<p></p>
 * <p>Yay I added a menu bar! None of the items work yet but I will add menu items that do work.</p>
 *
 * @author josephramsey
 */
public class TetradFx {
    private static final TetradFx INSTANCE = new TetradFx();

    public static TetradFx getInstance() {
        return TetradFx.INSTANCE;
    }

    private static void layout(Graph graph) {
//        circleLayout(graph);
        squareLayout(graph);
//        LayoutUtil.fruchtermanReingoldLayout(graph);
    }

    @NotNull
    private static ScrollPane getGraphDisplayScroll(Graph graph) {
        layout(graph);
        Pane graphView = new GraphView(graph);
        HBox hBox = new HBox(graphView);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(hBox);

        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        return scrollPane;
    }

    // TODO: Publish snapshot of Tetrad and use the one from LayoutUtils.
    public static void circleLayout(Graph graph) {
        if (graph == null) {
            return;
        }

        int centerx = 120 + 7 * graph.getNumNodes();
        int centery = 120 + 7 * graph.getNumNodes();
        int radius = centerx - 50;

        List<Node> nodes = new ArrayList<>(graph.getNodes());
        graph.paths().makeValidOrder(nodes);

        double rad = 6.28 / nodes.size();
        double phi = .75 * 6.28;    // start from 12 o'clock.

        for (Node node : nodes) {
            int centerX = centerx + (int) (radius * FastMath.cos(phi));
            int centerY = centery + (int) (radius * FastMath.sin(phi));

            node.setCenterX(centerX);
            node.setCenterY(centerY);

            phi += rad;
        }
    }

    // TODO: Publish snapshot of Tetrad and use the one from LayoutUtils.
    public static void squareLayout(Graph graph) {
        List<Node> nodes = new ArrayList<>(graph.getNodes());
        graph.paths().makeValidOrder(nodes);

        int bufferx = 70;
        int buffery = 50;
        int spacex = 70;
        int spacey = 50;

        int side = nodes.size() / 4;

        if (nodes.size() % 4 != 0) {
            side++;
        }

        for (int i = 0; i < side; i++) {
            if (i >= nodes.size()) {
                break;
            }
            Node node = nodes.get(i);
            node.setCenterX(bufferx + spacex * i);
            node.setCenterY(buffery);
        }

        for (int i = 0; i < side; i++) {
            if (i + side >= nodes.size()) {
                break;
            }
            Node node = nodes.get(i + side);
            node.setCenterX(bufferx + spacex * side);
            node.setCenterY(buffery + i * spacey);
        }

        for (int i = 0; i < side; i++) {
            if (i + 2 * side >= nodes.size()) {
                break;
            }
            Node node = nodes.get(i + 2 * side);
            node.setCenterX(bufferx + spacex * (side - i));
            node.setCenterY(buffery + spacey * side);
        }

        for (int i = 0; i < side; i++) {
            if (i + 3 * side >= nodes.size()) {
                break;
            }
            Node node = nodes.get(i + 3 * side);
            node.setCenterX(bufferx);
            node.setCenterY(buffery + spacey * (side - i));
        }
    }

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

    // Passing primaryStage in here so that I can quit the application from a menu item.
    public Pane getRoot(Stage primaryStage) {
        TabPane tabs = new TabPane();

        Result result = getSimulation(new Parameters(), true);
        System.out.println("Simulation done");

        TableView<DataView.DataRow> table = DataView.getTableView(result.dataSet());
        ScrollPane trueGraphScroll = getGraphDisplayScroll(result.graph());

        // Create the menu bar
        MenuBar menuBar = new MenuBar();

        // Create the File menu with some menu items
        Menu fileMenu = new Menu("File");
        MenuItem loadData = new MenuItem("Load Data");
        MenuItem exitItem = new MenuItem("Exit");

        loadData.setOnAction(e -> {
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
        });

        exitItem.setOnAction(e -> {
            System.out.println("Exiting");
            primaryStage.close();
        });

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
}


