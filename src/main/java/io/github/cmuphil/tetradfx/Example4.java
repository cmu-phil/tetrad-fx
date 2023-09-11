package io.github.cmuphil.tetradfx;

import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.graph.RandomGraph;
import io.github.cmuphil.tetradfx.ui.GraphView;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.TilePane;
import javafx.stage.Stage;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.TilePane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Example4 extends Application {

    private static final String DRAGGED_STYLE = "-fx-border-color: black; -fx-padding: 10px; -fx-background-color: yellow;";
    private static final String DEFAULT_STYLE = "-fx-border-color: black; -fx-padding: 10px; -fx-background-color: lightgray;";
    private static final String PLACED_STYLE = "-fx-border-color: black; -fx-padding: 10px; -fx-background-color: green;";

    @Override
    public void start(Stage primaryStage) {
        Graph graph = RandomGraph.randomGraphRandomForwardEdges(10, 0,
                10, 100, 100, 100, false);
        List<Node> nodes = graph.getNodes();

        Collections.shuffle(nodes);

        BorderPane bp = new BorderPane();

        TilePane tilePane = new TilePane();

        bp.setCenter(GraphView.getGraphDisplay(grh));

        bp.setBottom(tilePane);

        tilePane.setOrientation(javafx.geometry.Orientation.HORIZONTAL);
        List<Label> labelList = new ArrayList<>();

        for (int i = 1; i <= 10; i++) {
            Label label = new Label(nodes.get(i - 1).getName());
            labelList.add(label);
        }

        for (int i = 1; i <= 10; i++) {
            Label label = labelList.get(i - 1);
            label.setPrefSize(50, 30);
            label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            label.setStyle(DEFAULT_STYLE);
            tilePane.getChildren().add(label);

            // Drag detection
            label.setOnDragDetected(event -> {
                for (Label label1 : labelList) {
                    label1.setStyle(DEFAULT_STYLE);
                }

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

                    success = true;
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

        Scene scene = new Scene(bp, 800, 600);
        primaryStage.setTitle("Horizontal Drag and Drop with Color Change After Drop Example");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}





