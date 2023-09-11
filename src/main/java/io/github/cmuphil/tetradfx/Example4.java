package io.github.cmuphil.tetradfx;

import edu.cmu.tetrad.graph.EdgeListGraph;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.graph.RandomGraph;
import edu.cmu.tetrad.search.score.GraphScore;
import edu.cmu.tetrad.search.test.MsepTest;
import edu.cmu.tetrad.search.utils.TeyssierScorer;
import io.github.cmuphil.tetradfx.ui.GraphView;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.InnerShadow;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Example4 extends Application {

    private static final String DRAGGED_STYLE = "-fx-border-color: black; -fx-padding: 10px; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-color: yellow;";
    private static final String DEFAULT_STYLE = "-fx-border-color: black; -fx-padding: 10px;-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-color: lightblue;";
    private static final String PLACED_STYLE = "-fx-border-color: black; -fx-padding: 10px;-fx-font-size: 16px; -fx-font-weight: bold; -fx-background-color: lightgreen;";
    private static final String FRUGAL_STYLE = "-fx-border-color: black; -fx-padding: 10px;-fx-font-size: 16px; -fx-font-weight: bold; -fx-background-color: lightred;";

    @Override
    public void start(Stage primaryStage) {
        BorderPane main = getPermutationGamePane(4, 4);
        primaryStage.setTitle("Permuation Game");
        Scene scene = new Scene(main, 400, 350);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @NotNull
    private BorderPane getPermutationGamePane(int numNodes, int numEdges) {
        Graph _graph = RandomGraph.randomGraphRandomForwardEdges(numNodes, 0,
                numEdges, 100, 100, 100, false);
        List<Node> nodes = _graph.getNodes();

        Collections.shuffle(nodes);

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
        tilePane.setHgap(10);
        tilePane.setVgap(10);
        tilePane.setPrefColumns(graph.getNumNodes());
        tilePane.setStyle("-fx-padding: 5px;");

        main.setCenter(GraphView.getGraphDisplay(__graph));

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

        if (__graph.getNumEdges() == graph.getNumEdges()) {
            for (Label label1 : labelList) {
                label1.setStyle(PLACED_STYLE);
            }
        }

        for (int i = 1; i <= nodes.size(); i++) {
            Label label = labelList.get(i - 1);
            label.setPrefSize(30, 20);
            label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            label.setStyle(DEFAULT_STYLE);

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

                System.out.println("Drag detected");

                label.setStyle(DRAGGED_STYLE);
                Dragboard db = label.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent cc = new ClipboardContent();
                cc.putString(label.getText());
                db.setContent(cc);
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
                            new KeyFrame(Duration.millis(200), e -> draggedNode.setStyle(PLACED_STYLE)),
                            new KeyFrame(Duration.millis(400), e -> draggedNode.setStyle(DEFAULT_STYLE)),
                            new KeyFrame(Duration.millis(600), e -> draggedNode.setStyle(PLACED_STYLE)),
                            new KeyFrame(Duration.millis(800), e -> draggedNode.setStyle(DEFAULT_STYLE)),
                            new KeyFrame(Duration.millis(1000), e -> draggedNode.setStyle(PLACED_STYLE))
                    );
                    flashTimeline.play();

                    java.awt.Toolkit.getDefaultToolkit().beep();

                    List<Node> newOrder = new ArrayList<>();

                    for (Label label1 : getLabelsInOrder(tilePane)) {
                        String name = label1.getText();
                        Node node = graph.getNode(name);
                        newOrder.add(node);
                    }

                    scorer.score(newOrder);
                    Graph _graph1 = scorer.getGraph(false);
                    main.setCenter(GraphView.getGraphDisplay(_graph1));
                    success = true;

                    if (_graph1.getNumEdges() == _graph.getNumEdges()) {
                        for (Label label1 : labelList) {
                            label1.setStyle(PLACED_STYLE);
                        }
                    }
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

    public List<Label> getLabelsInOrder(TilePane tilePane) {
        List<Label> labels = new ArrayList<>();
        for (javafx.scene.Node node : tilePane.getChildren()) {
            if (node instanceof Label) {
                labels.add((Label) node);
            }
        }
        return labels;
    }

    public static void main(String[] args) {
        launch(args);
    }
}





