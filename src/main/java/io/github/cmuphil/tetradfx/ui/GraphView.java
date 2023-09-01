package io.github.cmuphil.tetradfx.ui;

import edu.cmu.tetrad.graph.*;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>Displays a Tetrad graph in a ScrollPane with a Pane. The graph is laid out using a layout
 * algorithm from Tetrad. The nodes can be dragged around with the mouse, and the edges will follow.
 * Currently only for DAGs and CPDAGs</p>
 *
 * TODO: need to add circle endpoints and represent PAGs.
 *
 * @author josephramsey
 */
public class GraphView extends Pane {
    private double offsetX1, offsetY1;

    private GraphView(Graph graph) {
        Pane content = new Pane();

        Map<Node, DisplayNode> displayNodes = new HashMap<>();
        Map<Edge, DisplayEdge> displayEdges = new HashMap<>();

        for (Node node : graph.getNodes()) {
            displayNodes.put(node, makeDisplayNode(node, graph, displayNodes, displayEdges));
        }

        for (Edge edge : graph.getEdges()) {
            DisplayEdge _edge = new DisplayEdge();
            displayEdges.put(edge, _edge);
            content.getChildren().addAll(_edge.getLine(), _edge.getArrowHead1(), _edge.getArrowHead2());
            updateLineAndArrow(edge, _edge.getLine(),
                    _edge.getArrowHead2(), _edge.getArrowHead2(),
                    displayNodes.get(edge.getNode1()).getShape(),
                    displayNodes.get(edge.getNode2()).getShape());
        }

        for (Node node : graph.getNodes()) {
            Text text = displayNodes.get(node).getText();
            text.setFont(new Font("Arial", 16));
            content.getChildren().addAll(displayNodes.get(node).getShape(), text);
        }

        getChildren().add(content);

        setPrefHeight(content.getPrefHeight());
        setPrefWidth(content.getPrefWidth());
    }

    /**
     * Static method to get a ScrollPane with a graph display.
     * @param graph The graph to display.
     * @return A ScrollPane with the graph display.
     */
    @NotNull
    public static ScrollPane getGraphDisplay(Graph graph) {
        layout(graph);
        Pane graphView = new GraphView(graph);
        HBox hBox = new HBox(graphView);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(hBox);

        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        return scrollPane;
    }

    // This will become a popup menu for the graph view.
    private static void layout(Graph graph) {
        LayoutUtil.circleLayout(graph);
//        LayoutUtil.squareLayout(graph);
//        LayoutUtil.fruchtermanReingoldLayout(graph);
    }

    // Makes a display node for a node in the graph and sets up its event handlers so it can
    // be dragged around with attached edges.
    private DisplayNode makeDisplayNode(Node node, Graph graph, Map<Node, DisplayNode> displayNodes,
                                        Map<Edge, DisplayEdge> displayEdges) {
        String name = node.getName();
        Text text = new Text(name);
        text.setFont(Font.font(20));
        Shape shape = getShape(node, text);

        shape.setFill(Color.WHITE);
        shape.setStroke(Color.BLACK);
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
                Node n1 = Edges.getDirectedEdgeTail(edge);
                Node n2 = Edges.getDirectedEdgeHead(edge);

                updateLineAndArrow(edge, displayEdges.get(edge).getLine(),
                        displayEdges.get(edge).getArrowHead1(), displayEdges.get(edge).getArrowHead2(),
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
                Node n1, n2;

                if (edge.isDirected()) {
                    n1 = Edges.getDirectedEdgeTail(edge);
                    n2 = Edges.getDirectedEdgeHead(edge);
                } else {
                    n1 = edge.getNode1();
                    n2 = edge.getNode2();
                }

                updateLineAndArrow(edge, displayEdges.get(edge).getLine(),
                        displayEdges.get(edge).getArrowHead1(), displayEdges.get(edge).getArrowHead2(),
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
    private void updateLineAndArrow(Edge edge, Line line, Polygon arrowhead1, Polygon arrowhead2,
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

        double arrowSize = 10;
        double angle = Math.atan2(line.getStartY() - line.getEndY(), line.getStartX() - line.getEndX());

        arrowhead1.getPoints().clear();
        arrowhead2.getPoints().clear();

        double v1 = arrowSize * Math.cos(angle - Math.PI / 6.);
        double v3 = arrowSize * Math.cos(angle + Math.PI / 6.);

        double v2 = arrowSize * Math.sin(angle - Math.PI / 6.);
        double v4 = arrowSize * Math.sin(angle + Math.PI / 6.);

        if (edge.getEndpoint1() == Endpoint.ARROW) {
            arrowhead1.getPoints().addAll(
                    line.getStartX() + v1,
                    line.getStartY() + v2,
                    line.getStartX(),
                    line.getStartX(),
                    line.getStartX() + v3,
                    line.getStartX() + v4
            );
        }

        if (edge.getEndpoint2() == Endpoint.ARROW) {
            arrowhead2.getPoints().addAll(
                    line.getEndX() + v1,
                    line.getEndY() + v2,
                    line.getEndX(),
                    line.getEndY(),
                    line.getEndX() + v3,
                    line.getEndY() + v4
            );
        }
    }

    // Use binary search to find the intersection of a line with a Shape to a center point.
    // Works for arbitrary Shapes.
    private double[] findShapeIntersection(Shape Shape, double startX, double startY, double endX, double endY) {
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
        private final Polygon arrowHead1;
        private final Polygon arrowHead2;

        public DisplayEdge() {
            Line line = new Line();
            line.setStroke(Color.BLACK);
            this.line = line;

            Polygon arrowHead1 = new Polygon();
            arrowHead1.setStroke(Color.BLACK);
            arrowHead1.setFill(Color.BLACK);

            this.arrowHead1 = arrowHead1;

            Polygon arrowHead2 = new Polygon();
            arrowHead2.setStroke(Color.BLACK);
            arrowHead2.setFill(Color.BLACK);

            this.arrowHead2 = arrowHead2;
        }

        public Line getLine() {
            return line;
        }

        public Polygon getArrowHead1() {
            return arrowHead1;
        }

        public Polygon getArrowHead2() {
            return arrowHead2;
        }
    }

    // This is just a ordinary ellips which already has a center point, no problem.
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

