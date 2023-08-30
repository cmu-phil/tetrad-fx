package io.github.cmuphil.tetradfx.ui;

import edu.cmu.tetrad.algcomparison.algorithm.oracle.cpdag.Boss;
import edu.cmu.tetrad.algcomparison.graph.RandomForward;
import edu.cmu.tetrad.algcomparison.score.SemBicScore;
import edu.cmu.tetrad.algcomparison.simulation.LeeHastieSimulation;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.graph.RandomGraph;
import edu.cmu.tetrad.sem.LargeScaleSimulation;
import edu.cmu.tetrad.util.Parameters;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import org.apache.commons.math3.util.FastMath;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * The main display for Tetrad-FX. Currently displays a graph, a data set, and a
 * search result.
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
        scrollPane.setPrefSize(400, 400);
        scrollPane.setLayoutX(410);  // positioned to the right of scrollPane1
        scrollPane.setLayoutY(10);
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

    public TabPane getPane() {
        Graph graph;
        DataSet dataSet;

        Parameters parameters = new Parameters();

        if (false) {
            LeeHastieSimulation simulation = new LeeHastieSimulation(new RandomForward());
            simulation.createData(parameters, true);
            graph = simulation.getTrueGraph(0);
            dataSet = (DataSet) simulation.getDataModel(0);
        } else {
            graph = RandomGraph.randomGraphRandomForwardEdges(50, 0, 100,
                    500, 100, 1000, false);

            LargeScaleSimulation simulation = new LargeScaleSimulation(graph);
            simulation.setCoefRange(0, 0.5);
            simulation.setSelfLoopCoef(0.1);
            dataSet = simulation.simulateDataReducedForm(1000);
        }

        System.out.println("Simulation done");

        layout(graph);

        TableView<DataView.DataRow> table = DataView.getTableView(dataSet);
        table.setPrefSize(400, 400);
        table.setLayoutX(10);
        table.setLayoutY(10);

        ScrollPane trueGraphScroll = getGraphDisplayScroll(graph);

        Boss boss = new Boss(new SemBicScore());
        Graph estGraph = boss.search(dataSet, parameters);
        layout(estGraph);
        ScrollPane estGraphScroll = getGraphDisplayScroll(estGraph);

        TabPane tabPane = new TabPane();

        // Create the second tab
        Tab tab1 = new Tab();
        tab1.setText("True Graph");
        tab1.setContent(trueGraphScroll);

        // First Tab
        Tab tab2 = new Tab();
        tab2.setText("Simulated Data");
        tab2.setContent(table);

        // Create the third tab
        Tab tab3 = new Tab();
        tab3.setText("BOSS");
        tab3.setContent(estGraphScroll);

        // Adding tabs to the TabPane
        tabPane.getTabs().add(tab1);
        tabPane.getTabs().add(tab2);
        tabPane.getTabs().add(tab3);

        return tabPane;
    }
}


