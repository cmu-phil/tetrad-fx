package io.github.cmuphil.tetradfx.ui;

import edu.cmu.tetrad.algcomparison.algorithm.oracle.cpdag.Boss;
import edu.cmu.tetrad.algcomparison.graph.RandomForward;
import edu.cmu.tetrad.algcomparison.score.SemBicScore;
import edu.cmu.tetrad.algcomparison.simulation.LeeHastieSimulation;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.RandomGraph;
import edu.cmu.tetrad.sem.LargeScaleSimulation;
import edu.cmu.tetrad.util.Parameters;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import org.jetbrains.annotations.NotNull;

import static io.github.cmuphil.tetradfx.ui.GraphView.squareLayout;

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


