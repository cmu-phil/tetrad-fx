package io.github.cmuphil.tetradfx;

import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.RandomGraph;
import io.github.cmuphil.tetradfx.ui.KnowledgeEditor;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.*;

public class Example extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    private final VBox tierPanelContainer = new VBox(10);
    private final List<TextArea> displayAreas = new ArrayList<>();
    private final TextArea unmatchedVarsArea = new TextArea();
    private final Map<Integer, String> rememberedRegexes = new HashMap<>();

    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox(10);
        ScrollPane scrollPane = new ScrollPane(root); // Wrap the VBox inside a ScrollPane
        scrollPane.setFitToWidth(true); // To ensure it takes the width of VBox
        root.setPadding(new Insets(10));

        Graph graph = RandomGraph.randomGraphRandomForwardEdges(10, 3, 10, 100, 100,
                100, false);
        Knowledge knowledge = new Knowledge(graph.getNodeNames());

        scrollPane.setContent(new KnowledgeEditor(knowledge).makeRegexFilter());

        primaryStage.setScene(new Scene(scrollPane, 600, 600));
        primaryStage.setTitle("Dynamic T    ier Filter");
        primaryStage.show();
    }
}

