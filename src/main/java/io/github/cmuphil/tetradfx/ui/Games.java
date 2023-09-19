package io.github.cmuphil.tetradfx.ui;

import edu.cmu.tetrad.graph.EdgeListGraph;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.search.score.GraphScore;
import edu.cmu.tetrad.search.test.MsepTest;
import edu.cmu.tetrad.search.utils.TeyssierScorer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.InnerShadow;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.util.Duration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.github.cmuphil.tetradfx.ui.GraphView.addGame;

/**
 * Implements some tutorial games.
 *
 * @author josephramsey
 */
public class Games {
    private static final String DRAGGED_STYLE = "-fx-border-color: darkblue; -fx-padding: 10px; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-color: yellow;";
    private static final String DEFAULT_STYLE = "-fx-border-color: darkblue; -fx-padding: 10px;-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-color: lightblue;";
    private static final String PLACED_STYLE = "-fx-border-color: darkblue; -fx-padding: 10px;-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-color: lightgreen;";
    private static final String PLACED_STYLE_BRIGHT = "-fx-border-color: darkblue; -fx-padding: 10px;-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-color: green;";


    static void baseGamesOnGraph(Graph graph) {
        addGame("""
                Welcome to the Permutation Game. A true DAG has been selected for you, and the nodes have been put in a random order.
                
                Each ordering of the nodes in the graph implies a unique DAG, possibly with adjacencies that aren't in the true DAG and perhaps some misorientations. Your task is to rearrange the nodes so that the implied DAG is in the correct Markov equivalence class.
                
                If you guess wrong you will get extra adjacencies in the graph. So, try to get a graph with the minimum number of edges in the fewest number of moves.
                
                When you find a correct answer, all nodes in the order will flash green.
                
                There is a method to the madness--see if you can figure it out. Good luck!""",
                "Permutation Game", getPermutationGamePane(graph));
    }

    @NotNull
    private static BorderPane getPermutationGamePane(Graph _graph) {
        List<Node> nodes = _graph.getNodes();
        Graph graph1 = new EdgeListGraph(_graph);
        int numEdges;

        do {
            TeyssierScorer scorer = new TeyssierScorer(new MsepTest(graph1), new GraphScore(graph1));
            Collections.shuffle(nodes);
            scorer.score(nodes);
            numEdges = scorer.getNumEdges();
        }  while (numEdges == graph1.getNumEdges());

        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            node.setName("" + (i + 1));
        }

        final Graph graph = new EdgeListGraph(_graph);

        TeyssierScorer scorer = new TeyssierScorer(new MsepTest(graph), new GraphScore(graph));
        scorer.setUseScore(false);

        scorer.score(nodes);

        Graph __graph = scorer.getGraph(false);

        BorderPane main = new BorderPane();

        TilePane tilePane = new TilePane();
        tilePane.setHgap(7);
        tilePane.setVgap(10);
        tilePane.setPrefColumns(graph.getNumNodes());
        tilePane.setStyle("-fx-padding: 5px;");
        tilePane.setPadding(new Insets(10, 0, 0, 0));

        ScrollPane graphDisplay = GraphView.getGraphDisplay(__graph);
        graphDisplay.setPadding(new Insets(1, 40, 40, 1));
        main.setCenter(graphDisplay);

        HBox hbox = new HBox();
        hbox.setAlignment(Pos.CENTER);
        hbox.getChildren().add(tilePane);

        main.setBottom(hbox);

        tilePane.setOrientation(javafx.geometry.Orientation.HORIZONTAL);
        List<Label> labelList = new ArrayList<>();

        for (int i = 1; i <= nodes.size(); i++) {
            Label label = new Label(nodes.get(i - 1).getName());
            label.setStyle(DEFAULT_STYLE);
            labelList.add(label);
        }

        setAll(__graph, graph, labelList, DEFAULT_STYLE);
        success(__graph, _graph, labelList);

        for (int i = 1; i <= nodes.size(); i++) {
            Label label = labelList.get(i - 1);
            label.setPrefSize(40, 20);
            label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            label.setStyle(DEFAULT_STYLE);
            label.setAlignment(Pos.CENTER);

            InnerShadow innerShadow = new InnerShadow();
            innerShadow.setOffsetX(2);
            innerShadow.setOffsetY(2);
            label.setEffect(innerShadow);

            tilePane.getChildren().add(label);

            // Drag detection
            label.setOnDragDetected(event -> {
                for (Label label1 : labelList) {
                    if (label1 != label) {
                        label1.setStyle(DEFAULT_STYLE);
                    }
                }

                label.setStyle(DRAGGED_STYLE);
                Dragboard db = label.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent cc = new ClipboardContent();
                cc.putString(label.getText());
                db.setContent(cc);
                db.setDragView(label.snapshot(null, null));
                event.consume();
            });

            // Drag over
            label.setOnDragOver(event -> {
                if (event.getDragboard().hasString()) {
                    event.acceptTransferModes(TransferMode.MOVE);
                }
                event.consume();
            });

            // Drag dropped
            label.setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                boolean success = false;

                if (db.hasString()) {
                    String draggedText = db.getString();
                    int draggedIdx = tilePane.getChildren().indexOf(tilePane.getChildren().stream().filter(node -> node instanceof Label && ((Label) node).getText().equals(draggedText)).findFirst().orElse(null));
                    int thisIdx = tilePane.getChildren().indexOf(label);
                    Label draggedNode = (Label) tilePane.getChildren().remove(draggedIdx);
                    tilePane.getChildren().add(thisIdx, draggedNode);


                    // Set the style of the node after it has been placed
//                    draggedNode.setStyle(PLACED_STYLE);

                    Timeline flashTimeline = new Timeline(
                            new KeyFrame(Duration.millis(0), e -> draggedNode.setStyle(DEFAULT_STYLE)),
                            new KeyFrame(Duration.millis(200), e -> draggedNode.setStyle(PLACED_STYLE_BRIGHT)),
                            new KeyFrame(Duration.millis(400), e -> draggedNode.setStyle(DEFAULT_STYLE)),
                            new KeyFrame(Duration.millis(600), e -> draggedNode.setStyle(PLACED_STYLE_BRIGHT)),
                            new KeyFrame(Duration.millis(800), e -> draggedNode.setStyle(DEFAULT_STYLE)),
                            new KeyFrame(Duration.millis(1000), e -> draggedNode.setStyle(PLACED_STYLE))
                    );
                    flashTimeline.play();

//                    java.awt.Toolkit.getDefaultToolkit().beep();

                    List<Node> newOrder = new ArrayList<>();

                    for (Label label1 : getLabelsInOrder(tilePane)) {
                        String name = label1.getText();
                        Node node = graph.getNode(name);
                        newOrder.add(node);
                    }

                    scorer.score(newOrder);
                    Graph _graph1 = scorer.getGraph(false);
                    ScrollPane graphDisplay1 = GraphView.getGraphDisplay(_graph1);
                    graphDisplay1.setPadding(new Insets(0, 40, 40, 0));
                    main.setCenter(graphDisplay1);
                    success = true;

                    success(_graph1, _graph, labelList);
                }

                event.setDropCompleted(success);
                event.consume();
            });

            // Drag done
            label.setOnDragDone(event -> {
                // Reset the style of the dragged node if not placed
                if (event.getTransferMode() != TransferMode.MOVE) {
                    label.setStyle(DEFAULT_STYLE);
                }
            });
        }
        return main;
    }

    private static void setAll(Graph _graph1, Graph _graph, List<Label> labelList, String style) {
        if (_graph1.getNumEdges() == _graph.getNumEdges()) {
            for (Label label1 : labelList) {
                label1.setStyle(style);
            }
        }
    }

    private static void success(Graph _graph1, Graph _graph, List<Label> labelList) {
        if (_graph1.getNumEdges() == _graph.getNumEdges()) {
            Timeline flashTimeline2 = new Timeline(
                    new KeyFrame(Duration.millis(0), e -> setAll(_graph1, _graph, labelList, DEFAULT_STYLE)),
                    new KeyFrame(Duration.millis(200), e -> setAll(_graph1, _graph, labelList, PLACED_STYLE_BRIGHT)),
                    new KeyFrame(Duration.millis(400), e -> setAll(_graph1, _graph, labelList, DEFAULT_STYLE)),
                    new KeyFrame(Duration.millis(600), e -> setAll(_graph1, _graph, labelList, PLACED_STYLE_BRIGHT)),
                    new KeyFrame(Duration.millis(800), e -> setAll(_graph1, _graph, labelList, DEFAULT_STYLE)),
                    new KeyFrame(Duration.millis(1000), e -> setAll(_graph1, _graph, labelList, PLACED_STYLE))
            );

            flashTimeline2.play();
        }
    }

    private static List<Label> getLabelsInOrder(TilePane tilePane) {
        List<Label> labels = new ArrayList<>();
        for (javafx.scene.Node node : tilePane.getChildren()) {
            if (node instanceof Label) {
                labels.add((Label) node);
            }
        }
        return labels;
    }
}
