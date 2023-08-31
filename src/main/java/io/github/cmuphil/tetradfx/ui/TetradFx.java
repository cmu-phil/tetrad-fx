package io.github.cmuphil.tetradfx.ui;

import edu.cmu.tetrad.algcomparison.graph.RandomForward;
import edu.cmu.tetrad.algcomparison.simulation.LeeHastieSimulation;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.graph.RandomGraph;
import edu.cmu.tetrad.sem.LargeScaleSimulation;
import edu.cmu.tetrad.util.Parameters;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.apache.commons.math3.util.FastMath;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * <p></p>The main display for Tetrad-FX. Currently displays a graph, a data set, and a search result.<p></p>
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
        Pane graphView = new GraphView(graph);
        HBox hBox = new HBox(graphView);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(hBox);

        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
//        scrollPane.setPrefSize(400, 400);
//        scrollPane.setLayoutX(410);  // positioned to the right of scrollPane1
//        scrollPane.setLayoutY(10);


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
    private static Result getResult(Parameters parameters) {
        DataSet dataSet;
        Graph graph;
        if (true) {
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

    public Pane getRoot(Stage primaryStage) {
        Result result = getResult(new Parameters());
        System.out.println("Simulation done");

        TableView<DataView.DataRow> table = DataView.getTableView(result.dataSet());
        ScrollPane trueGraphScroll = getGraphDisplayScroll(result.graph());

        // Create the menu bar
        MenuBar menuBar = new MenuBar();

        // Create the File menu with some menu items
        Menu fileMenu = new Menu("File");
        MenuItem newItem = new MenuItem("New");
        MenuItem openItem = new MenuItem("Open");
        MenuItem saveItem = new MenuItem("Save");
        MenuItem exitItem = new MenuItem("Exit");

        newItem.setOnAction(e -> System.out.println("New File"));
        openItem.setOnAction(e -> System.out.println("Open File"));
        saveItem.setOnAction(e -> System.out.println("Save File"));
        exitItem.setOnAction(e -> {
            System.out.println("Exiting");
            primaryStage.close();
        });

        fileMenu.getItems().addAll(newItem, openItem, saveItem, new SeparatorMenuItem(), exitItem);

        // Create the Edit menu with some menu items
        Menu editMenu = new Menu("Edit");
        MenuItem cutItem = new MenuItem("Cut");
        MenuItem copyItem = new MenuItem("Copy");
        MenuItem pasteItem = new MenuItem("Paste");

        cutItem.setOnAction(e -> System.out.println("Cut"));
        copyItem.setOnAction(e -> System.out.println("Copy"));
        pasteItem.setOnAction(e -> System.out.println("Paste"));

        editMenu.getItems().addAll(cutItem, copyItem, pasteItem);

        // Add menus to the menu bar
        menuBar.getMenus().addAll(fileMenu, editMenu);

        // Add the menu bar to the main scene
        BorderPane root = new BorderPane();
        root.setTop(menuBar);

        TextArea area1 = new TextArea();
        area1.setPrefSize(800, 800);
        ScrollPane scroll1 = new ScrollPane(area1);

        TextArea area2 = new TextArea();
        area2.setPrefSize(800, 800);
        ScrollPane scroll2 = new ScrollPane(area2);

        TabPane tabs = new TabPane();
        tabs.getTabs().add(new Tab("Tab 1", table));
        tabs.getTabs().add(new Tab("Tab 2", trueGraphScroll));

        tabs.setPrefSize(300, 300);

        root.setCenter(tabs);

        return root;
    }

    private record Result(Graph graph, DataSet dataSet) {
    }
}


