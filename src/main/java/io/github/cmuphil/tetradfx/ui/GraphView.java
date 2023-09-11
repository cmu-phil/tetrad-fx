package io.github.cmuphil.tetradfx.ui;

import edu.cmu.tetrad.graph.*;
import io.github.cmuphil.tetradfx.for751lib.ChangedStuffINeed;
import io.github.cmuphil.tetradfx.for751lib.GraphTransforms;
import javafx.geometry.Pos;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ZoomEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

/**
 * <p>Displays a Tetrad graph in a ScrollPane with a Pane. The graph is laid out using a layout
 * algorithm from Tetrad. The nodes can be dragged around with the mouse, and the edges will follow.
 * Currently only for DAGs and CPDAGs</p>
 *
 * <p>Scales well to large, dense graphs.</p>
 *
 * @author josephramsey
 */
public class GraphView extends Pane {
    private final HashMap<Node, DisplayNode> displayNodes;
    private final HashMap<Edge, DisplayEdge> displayEdges;
    private double offsetX1, offsetY1;

    // These are some colors from the Swing Tetrad app, just pulled them over.
    private static final Color NODE_FILL_COLOR = Color.rgb(148, 198, 226);
    private static final Color NODE_EDGE_COLOR = Color.rgb(146, 154, 166);
//    private static final Color NODE_SELECTED_FILL_COLOR = Color.rgb(244, 219, 110);
//    private static final Color NODE_SELECTED_EDGE_COLOR = Color.rgb(215, 193, 97);
//    private static final Color NODE_TEXT_COLOR = Color.rgb(0, 1, 53);d
    private static final Color LINE_COLOR = Color.rgb(26, 113, 169);
//    private static final Color SELECTED_LINE_COLOR = Color.rgb(244, 0, 20);
//    private static final Color HIGHLIGHTED_LINE_COLOR = Color.rgb(238, 180, 34);

    private GraphView(Graph graph) {
        Pane content = new Pane();

        displayNodes = new HashMap<>();
        displayEdges = new HashMap<>();

        for (Node node : graph.getNodes()) {
            displayNodes.put(node, makeDisplayNode(node, graph));
        }

        for (Edge edge : graph.getEdges()) {
            DisplayEdge _edge = new DisplayEdge();
            displayEdges.put(edge, _edge);
            content.getChildren().addAll(_edge.getLine(), _edge.getEdgemark1(), _edge.getEdgemark2());

            updateLineAndArrow(edge, _edge.getLine(),
                    _edge.getEdgemark1(), _edge.getEdgemark2(),
                    displayNodes.get(edge.getNode1()).getShape(),
                    displayNodes.get(edge.getNode2()).getShape());
        }

        for (Node node : graph.getNodes()) {
            Text text = displayNodes.get(node).getText();
            text.setFont(new Font("Arial", 16));
            content.getChildren().addAll(displayNodes.get(node).getShape(), text);
        }

        getChildren().add(content);

        ContextMenu contextMenu = getContextMenu(content, graph);

        // Show context menu on right-click on the label
        content.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.SECONDARY ||
                    (event.getButton() == MouseButton.PRIMARY && event.isControlDown())) {
                contextMenu.show(content, event.getScreenX(), event.getScreenY());
            }
        });
    }

    /**
     * Static method to get a ScrollPane with a graph display.
     * @param graph The graph to display.
     * @return A ScrollPane with the graph display.
     */
    @NotNull
    public static ScrollPane getGraphDisplay(Graph graph) {
        ChangedStuffINeed.circleLayout(graph);
        Pane graphView = new GraphView(graph);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(graphView);

        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        graphView.setOnZoom(e -> handleZoom(e, graphView));

        return scrollPane;
    }

    private static void handleZoom(ZoomEvent e, Pane graphView) {
        double zoomFactor = e.getZoomFactor();

        // Adjust the scale based on the zoom factor
        graphView.setScaleX(graphView.getScaleX() * zoomFactor);
        graphView.setScaleY(graphView.getScaleY() * zoomFactor);

        e.consume();
    }

    public static void addGame(String s, String name, Pane pane) {
        Text text = new Text(s);
        text.setWrappingWidth(600);
        text.setFont(new Font("Arial", 16));
        text.setStyle("-fx-border-color: black; -fx-border-width: 2px;");
        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(pane != null ? pane : text);

        HBox hBox = new HBox(borderPane);
        hBox.setAlignment(Pos.CENTER);
        VBox vBox = new VBox(hBox);
        vBox.setAlignment(Pos.CENTER);

        NamesToContents.getInstance().getSelectedContents().addGame(name, vBox);
    }

    @NotNull
    private ContextMenu getContextMenu(Pane pane, Graph graph) {
        // Create a context menu
        ContextMenu contextMenu = new ContextMenu();

        Menu layout = new Menu("Layout");

        // Create menu items
        MenuItem item1 = new MenuItem("Circle");
        item1.setOnAction(e -> layout(graph, 1));

        MenuItem item2 = new MenuItem("Square");
        item2.setOnAction(e -> layout(graph, 2));

        MenuItem item3 = new MenuItem("Force");
        item3.setOnAction(e -> layout(graph, 3));

        MenuItem item4 = new MenuItem("Causal Order");
        item4.setOnAction(e -> layout(graph, 4));

        // Add menu items to the context menu
        layout.getItems().addAll(item1, item2, item3, item4);
        contextMenu.getItems().addAll(layout);

        Menu transform = new Menu("Transform");

        MenuItem dagToCPDAG = new MenuItem("DAG to CPDAG");

        dagToCPDAG.setOnAction(e -> {
            Graph dag = GraphTransforms.cpdagForDag(graph);
            NamesToContents.getInstance().getSelectedContents().addGraph("DAG to CPPAG", dag, true);
        });

        MenuItem dagToPag = new MenuItem("DAG to PAG");

        dagToPag.setOnAction(e -> {
            Graph dag = GraphTransforms.dagToPag(graph);
            NamesToContents.getInstance().getSelectedContents().addGraph("DAG to PAG", dag, true);
        });

        MenuItem dagFromCPDAG = new MenuItem("DAG from CPDAG");

        dagFromCPDAG.setOnAction(e -> {
            Graph dag = GraphTransforms.dagFromCPDAG(graph);
            NamesToContents.getInstance().getSelectedContents().addGraph("DAG from CPDAG", dag, true);
        });

        MenuItem magFromPag = new MenuItem("MAG from PAG");

        magFromPag.setOnAction(e -> {
            Graph mag = GraphTransforms.pagToMag(graph);
            NamesToContents.getInstance().getSelectedContents().addGraph("MAG from PAG", mag, true);
        });

        transform.getItems().addAll(dagToCPDAG, dagToPag, dagFromCPDAG, magFromPag);
        contextMenu.getItems().add(transform);

        Menu model = new Menu("Model");
        model.getItems().add(new MenuItem("Make a model based on this graph and the selected data"));
        contextMenu.getItems().add(model);

        Menu search = new Menu("Do an search using this graph as an oracle");
        search.getItems().add(new MenuItem("Do an search using this graph as an oracle"));
        contextMenu.getItems().add(search);

        MenuItem saveGraph = new MenuItem("Save Graph");
        contextMenu.getItems().add(saveGraph);

        MenuItem games = new MenuItem("Base Games on this Graph!");
        games.setOnAction(e -> Games.baseGamesOnGraph());
        contextMenu.getItems().add(games);

        // Show context menu on right-click on the pane
        pane.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY ||
                    (event.getButton() == MouseButton.PRIMARY && event.isControlDown())) {
                contextMenu.show(pane, event.getScreenX(), event.getScreenY());
            }
        });

        return contextMenu;
    }

    // This will become a popup menu for the graph view.
    private void layout(Graph graph, int layoutType) {
        switch (layoutType) {
            case 1 -> ChangedStuffINeed.circleLayout(graph);
            case 2 -> ChangedStuffINeed.squareLayout(graph);
            case 3 -> LayoutUtil.fruchtermanReingoldLayout(graph);
            case 4 -> ChangedStuffINeed.layoutByCausalOrder(graph);
            default -> throw new IllegalArgumentException("That layout type is not configured: " + layoutType);
        }

        for (Node node : graph.getNodes()) {
            double newX = node.getCenterX();
            double newY = node.getCenterY();

            Shape shape = displayNodes.get(node).getShape();
            Text text = displayNodes.get(node).getText();

            ((CenteredShape) shape).setCenterX(newX);
            ((CenteredShape) shape).setCenterY(newY);
            text.setX(newX - text.getLayoutBounds().getWidth() / 2);
            text.setY(newY + text.getLayoutBounds().getHeight() / 4);

            for (Edge edge : graph.getEdges(node)) {
                Node n1 = edge.getNode1();
                Node n2 = edge.getNode2();

                updateLineAndArrow(edge, displayEdges.get(edge).getLine(),
                        displayEdges.get(edge).getEdgemark1(), displayEdges.get(edge).getEdgemark2(),
                        displayNodes.get(n1).getShape(), displayNodes.get(n2).getShape());
            }
        }
    }

    // Makes a display node for a node in the graph and sets up its event handlers, so it can
    // be dragged around with attached edges.
    private DisplayNode makeDisplayNode(Node node, Graph graph) {
        String name = node.getName();
        Text text = new Text(name);
        text.setFont(Font.font(20));
        Shape shape = getShape(node, text);

        shape.setFill(NODE_FILL_COLOR);
        shape.setStroke(LINE_COLOR);
        shape.setStrokeWidth(2);

        text.setX(((CenteredShape) shape).getCenterX() - text.getLayoutBounds().getWidth() / 2 + 2);
        text.setY(((CenteredShape) shape).getCenterY() + text.getLayoutBounds().getHeight() / 4);

        shape.setOnMousePressed(event -> {
            offsetX1 = event.getSceneX() - ((CenteredShape) shape).getCenterX();
            offsetY1 = event.getSceneY() - ((CenteredShape) shape).getCenterY();
        });

        shape.setOnMouseDragged(event -> {
            double newX = event.getSceneX() - offsetX1;
            double newY = event.getSceneY() - offsetY1;
            ((CenteredShape) shape).setCenterX(newX);
            ((CenteredShape) shape).setCenterY(newY);
            text.setX(newX - text.getLayoutBounds().getWidth() / 2);
            text.setY(newY + text.getLayoutBounds().getHeight() / 4);

            for (Edge edge : graph.getEdges(node)) {
                Node n1 = edge.getNode1();
                Node n2 = edge.getNode2();

                updateLineAndArrow(edge, displayEdges.get(edge).getLine(),
                        displayEdges.get(edge).getEdgemark1(), displayEdges.get(edge).getEdgemark2(),
                        displayNodes.get(n1).getShape(), displayNodes.get(n2).getShape());
            }
        });

        text.setOnMousePressed(event -> {
            offsetX1 = event.getSceneX() - ((CenteredShape) shape).getCenterX();
            offsetY1 = event.getSceneY() - ((CenteredShape) shape).getCenterY();
        });

        text.setOnMouseDragged(event -> {
            double newX = event.getSceneX() - offsetX1;
            double newY = event.getSceneY() - offsetY1;
            ((CenteredShape) shape).setCenterX(newX);
            ((CenteredShape) shape).setCenterY(newY);
            text.setX(newX - text.getLayoutBounds().getWidth() / 2);
            text.setY(newY + text.getLayoutBounds().getHeight() / 4);

            for (Edge edge : graph.getEdges(node)) {
                Node n1 = edge.getNode1();
                Node n2 = edge.getNode2();

                updateLineAndArrow(edge, displayEdges.get(edge).getLine(),
                        displayEdges.get(edge).getEdgemark1(), displayEdges.get(edge).getEdgemark2(),
                        displayNodes.get(n1).getShape(), displayNodes.get(n2).getShape());
            }
        });

        return new DisplayNode(shape, text);
    }

    @NotNull
    private static Shape getShape(Node node, Text text) {
        Shape shape;
        if (node.getNodeType() == NodeType.MEASURED) {
            shape = new Rectangle(node.getCenterX(), node.getCenterY(),
                    text.getLayoutBounds().getWidth() + 12,
                    text.getLayoutBounds().getHeight() + 2);
        } else if (node.getNodeType() == NodeType.LATENT || node.getNodeType() == NodeType.ERROR) {
            shape = new Ellipse(node.getCenterX(), node.getCenterY(),
                    text.getLayoutBounds().getWidth() / 2 + 12,
                    text.getLayoutBounds().getHeight() / 2 + 2);
        } else {
            throw new IllegalArgumentException("That node type is not configured: " + node.getNodeType());
        }
        return shape;
    }

    // Currently only support edges with possible arrow endpoints. So, DAGs and CPDAGs. Still
    // need to implement circle endpoints for PAGs.
    private void updateLineAndArrow(Edge edge, Line line, Polygon edgemark1, Polygon edgemark2,
                                    Shape startShape, Shape endShape) {
        double startX = ((CenteredShape) startShape).getCenterX();
        double startY = ((CenteredShape) startShape).getCenterY();
        double endX = ((CenteredShape) endShape).getCenterX();
        double endY = ((CenteredShape) endShape).getCenterY();

        double[] startIntersection = findShapeIntersection(startShape, startX, startY, endX, endY);
        double[] endIntersection = findShapeIntersection(endShape, endX, endY, startX, startY);

        line.setStartX(startIntersection[0]);
        line.setStartY(startIntersection[1]);
        line.setEndX(endIntersection[0]);
        line.setEndY(endIntersection[1]);

        edgemark1.getPoints().clear();
        edgemark2.getPoints().clear();

        double lineStartX = line.getStartX();
        double lineStartY = line.getStartY();
        double lineEndX = line.getEndX();
        double lineEndY = line.getEndY();

        if (edge.getEndpoint1() == Endpoint.ARROW) {
            createArrowhead(edgemark1, lineEndX, lineEndY, lineStartX, lineStartY);
        } else if (edge.getEndpoint1() == Endpoint.CIRCLE) {
            createCircle(edgemark1, lineStartX, lineStartY, lineEndX, lineEndY);
        }

        if (edge.getEndpoint2() == Endpoint.ARROW) {
            createArrowhead(edgemark2, lineStartX, lineStartY, lineEndX, lineEndY);
        } else if (edge.getEndpoint2() == Endpoint.CIRCLE) {
            createCircle(edgemark2, lineEndX, lineEndY, lineStartX, lineStartY);
        }
    }

    /**
     * Changes the edge mark polygon to be an arrowhead.
     */
    private static void createArrowhead(Polygon edgemark, double lineStartX, double lineStartY, double lineEndX, double lineEndY) {
        double angle = Math.atan2(lineStartY - lineEndY, lineStartX - lineEndX);
        double arrowSize = 10;

        edgemark.getPoints().addAll(
                lineEndX + arrowSize * Math.cos(angle - Math.PI / 6.),
                lineEndY + arrowSize * Math.sin(angle - Math.PI / 6.),
                lineEndX,
                lineEndY,
                lineEndX + arrowSize * Math.cos(angle + Math.PI / 6.),
                lineEndY + arrowSize * Math.sin(angle + Math.PI / 6.)
        );

        edgemark.setFill(LINE_COLOR);
        edgemark.setStroke(LINE_COLOR);
        edgemark.setStrokeWidth(2);
    }

    /**
     * Changes the edge mark polygon to be a circle.
     */
    private void createCircle(Polygon edgemark, double startX, double starty,
                              double endX, double endY) {
        double radius = 5;
        double sides = 10;

        double sqrt = Math.sqrt(Math.pow(endX - startX, 2) + Math.pow(endY - starty, 2));
        double centerX = startX + radius * (endX - startX) / sqrt;
        double centerY = starty + radius * (endY - starty) / sqrt;

        final double ANGLE_STEP = 360.0 / sides;

        for (int i = 0; i < sides; i++) {
            double angle = i * ANGLE_STEP;
            double x = centerX + radius * Math.cos(Math.toRadians(angle));
            double y = centerY + radius * Math.sin(Math.toRadians(angle));
            edgemark.getPoints().addAll(x, y);
        }

        edgemark.setFill(Color.WHITE);
        edgemark.setStroke(NODE_EDGE_COLOR);
        edgemark.setStrokeWidth(2);
    }

    // Use binary search to find the intersection of a line with a Shape to a center point.
    // Works for arbitrary Shapes.
    private double[] findShapeIntersection(Shape Shape, double startX, double startY, double endX,
                                           double endY) {
        double[] intersection = new double[2];

        int iterations = 15;
        for (int i = 0; i < iterations; i++) {
            double midX = (startX + endX) / 2;
            double midY = (startY + endY) / 2;

            if (Shape.contains(midX, midY)) {
                startX = midX;
                startY = midY;
            } else {
                intersection[0] = midX;
                intersection[1] = midY;
                endX = midX;
                endY = midY;
            }
        }

        return intersection;
    }

    // Represents a node in the graph display.
    private static class DisplayNode {
        private final Shape shape;
        private final Text text;

        public DisplayNode(Shape Shape, Text text) {
            this.shape = Shape;
            this.text = text;
        }

        public Shape getShape() {
            return shape;
        }

        public Text getText() {
            return text;
        }
    }

    // Represents an edge in the graph display.
    private static class DisplayEdge {
        private final Line line;
        private final Polygon edgemark1;
        private final Polygon edgemark2;

        public DisplayEdge() {
            Line line = new Line();
            line.setStroke(LINE_COLOR);
            this.line = line;

            Polygon edgemark1 = new Polygon();

            edgemark1.setStroke(LINE_COLOR);
            edgemark1.setFill(LINE_COLOR);

            this.edgemark1 = edgemark1;

            Polygon edgemark2 = new Polygon();
            edgemark2.setStroke(LINE_COLOR);
            edgemark2.setFill(LINE_COLOR);

            this.edgemark2 = edgemark2;
        }

        public Line getLine() {
            return line;
        }

        public Polygon getEdgemark1() {
            return edgemark1;
        }

        public Polygon getEdgemark2() {
            return edgemark2;
        }
    }

    // This is just an ordinary ellipse which already has a center point, no problem.
    private static class Ellipse extends javafx.scene.shape.Ellipse implements CenteredShape {
        public Ellipse(int centerX, int centerY, double radiusX, double radiusY) {
            setRadiusX(radiusX);
            setRadiusY(radiusY);
            super.setCenterX(centerX);
            setCenterY(centerY);
        }
    }

    // This is just an ordinary rectangle, to which we add a center point.
    private static class Rectangle extends javafx.scene.shape.Rectangle implements CenteredShape {
        public Rectangle(int centerX, int centerY, double width, double height) {
            setWidth(width);
            setHeight(height);
            setCenterX(centerX);
            setCenterY(centerY);
        }

        @Override
        public void setCenterX(double centerX) {
            setX(centerX - getWidth() / 2);
        }

        @Override
        public void setCenterY(double centerY) {
            setY(centerY - getHeight() / 2);
        }

        @Override
        public double getCenterX() {
            return getX() + getWidth() / 2;
        }

        @Override
        public double getCenterY() {
            return getY() + getHeight() / 2;
        }
    }
}

