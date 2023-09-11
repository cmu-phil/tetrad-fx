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

public class Games {
    private static final String DRAGGED_STYLE = "-fx-border-color: black; -fx-padding: 10px; -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-color: yellow;";
    private static final String DEFAULT_STYLE = "-fx-border-color: black; -fx-padding: 10px;-fx-font-size: 12px; -fx-font-weight: bold; -fx-background-color: lightblue;";
    private static final String PLACED_STYLE = "-fx-border-color: black; -fx-padding: 10px;-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-color: lightgreen;";


//    public static void baseGamesOnDataset() {
//        NamesToContents.getInstance().getSelectedContents().clearGames();
//
//        addGame("""
//                This the PC Search Game. We are assuming here that the underlying model is a DAG and that there are no latent variables. If you don't think this is true of your data, maybe you shouldn't play this game!
//
//                You are allowed to remove edges or orient colliders based on conditional independence facts that you ascertain from the variables. The graph will start with a complete graph, and you may test conditional independence facts like dsep(A, B | C) by clicking on the nodes A, B, and C in sequence. You will be told whether the independence holds or does not. You may click "Remove edge A--B" or "Orient collider A->B<-C" where you click on the B.
//
//                We will handle the implied orientation rules (Meek rules) for you at each step.
//
//                You're allowed to backtrack, though this will count against your number of steps!
//
//                We will tell you the number of edges in your graph at each step. If a true graph is available, the goal is to get to SHD = 0! Otherwise you're on your own Can you do it in the fewest number of steps? Good luck!""",
//                "PC Search Game", null);
//
//        addGame("""
//                This is the Permutation Search Game! We are assuming that the underlying model is a DAG and that there are no latent variables. Is this a good assumption for your data?
//
//                Each permutation of the graph implies a DAG. We will give you a random permutation, and you need to rearrange the nodes so that the implied DAG is correct!
//
//                We will show you the implied DAG at each step and tell you the number of edges in the graph. Try to get a graph with the minimum number of edges in the fewest number of moves you can!
//
//                Maybe you will come up with a new permutation algorithm!""",
//                "Permutation Search Game", null);
//    }

    static void baseGamesOnGraph(Graph graph) {
        NamesToContents.getInstance().getSelectedContents().clearGames();

//        addGame("""
//                This the D-separation Game. We will give you potential d-separation facts, and you need to say whether the d-separation facts hold in the graph you've selected!
//
//                You get to say how many d-separation facts you want to check. We will keep score for you.
//
//                Can you get all of them right? Good luck! Don't forget to check descendants of colliders!""",
//                "D-separation Game", null);

//        addGame("""
//                This the PC Search Game. We are assuming here that the graph is a DAG and that there are no latent variables. If you don't think this is true, maybe you shouldn't play this game!
//
//                You are allowed to remove edges or orient colliders based on conditional independence facts that you ascertain from the variables. The graph will start with a complete graph, and you may test conditional independence facts like dsep(A, B | C) by clicking on the nodes A, B, and C in sequence. You will be told whether d-separation holds or does not. You may click \\"Remove edge A--B\\" or \\"Orient collider A->B<-C\\" where you click on the B.
//
//                We will handle the implied orientation rules (Meek rules) for you at each step.
//                You're allowed to backtrack!
//                We will tell you the SHD score of your graph at each step. The goal is to get to SHD = 0! Can you do it in the fewest number of steps? Good luck!""",
//                "PC Search Game", null);

        addGame("""
                Welcome to the Permutation Search Game! A true DAG has been selected for you, and the nodes have been put in a random order.
                
                Each ordering of the nodes in the graph implies an estimated DAG, possibly with extra adjacencies. Your task is to rearrange the nodes so that the implied DAG is in the correct Markov equivalence class! 
                
                If you guess wrong you will get extra edges in the graph! So, try to get a graph with the minimum number of edges in the fewest number of moves!
                
                When you find a correct answer, all nodes in the order will flash green.
                
                There is a method to the madness--see if you can figure it out. Maybe you will come up with a new permutation algorithm!""",
                "Permutation Search Game", getPermutationGamePane(graph));
    }

    @NotNull
    private static BorderPane getPermutationGamePane(Graph _graph) {
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

        ScrollPane graphDisplay = GraphView.getGraphDisplay(__graph);
        graphDisplay.setPadding(new Insets(0, 40, 40, 0));
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
            label.setPrefSize(35, 20);
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
                    new KeyFrame(Duration.millis(200), e -> setAll(_graph1, _graph, labelList, PLACED_STYLE)),
                    new KeyFrame(Duration.millis(400), e -> setAll(_graph1, _graph, labelList, DEFAULT_STYLE)),
                    new KeyFrame(Duration.millis(600), e -> setAll(_graph1, _graph, labelList, PLACED_STYLE)),
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
