package io.github.cmuphil.tetradfx.ui;

import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.Graph;
import javafx.geometry.Side;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * <p>Stores all of the tabbed panes for a given dataset.</p>
 *
 * @author josephramsey
 */
public class Contents {
    private final Tab dataTab;
    private final Tab graphTab;
    private final Tab knowledgeTab;
    private final Tab searchTab;
    private final Tab gamesTab;

    private final TabPane main;

    private final TabPane data = new TabPane();
    private final TabPane graphs = new TabPane();
    private final TabPane knowledge = new TabPane();
    private final TabPane search = new TabPane();
    private final TabPane games = new TabPane();
    private final TreeItem<String> treeItem;

    public Contents(DataSet dataSet, Graph graph, String contentsName, String dataName, String graphName) {
        this.main = new TabPane();
        this.main.setPrefSize(1000, 800);
        this.main.setSide(Side.LEFT);
        this.treeItem = new TreeItem<>(contentsName);

        this.treeItem.getChildren().add(new TreeItem<>("Data"));
        this.treeItem.getChildren().add(new TreeItem<>("Graph"));
        this.treeItem.getChildren().add(new TreeItem<>("Knowledge"));
        this.treeItem.getChildren().add(new TreeItem<>("Search"));
        this.treeItem.getChildren().add(new TreeItem<>("Insights"));
        this.treeItem.getChildren().add(new TreeItem<>("Games"));

        dataTab = new Tab("Data", data);
        graphTab = new Tab("Graph", graphs);
        knowledgeTab = new Tab("Knowledge", knowledge);
        searchTab = new Tab("Search", search);
        gamesTab = new Tab("Games", games);

        if (dataSet == null && graph == null) {
            this.main.getTabs().add(dataTab);
            this.main.getTabs().add(graphTab);
        } else if (dataSet != null && graph == null) {
            this.main.getTabs().add(dataTab);
            this.main.getTabs().add(graphTab);
            addDataSet(dataName, dataSet, false);
        } else if (dataSet == null) {
            this.main.getTabs().add(dataTab);
            this.main.getTabs().add(graphTab);
             addGraph(graphName, graph, false);
        } else {
            this.main.getTabs().add(dataTab);
            this.main.getTabs().add(graphTab);
            addDataSet(dataName, dataSet, false);
            addGraph(graphName, graph, false);
        }

        this.main.getTabs().add(knowledgeTab);
        this.main.getTabs().add(searchTab);
        this.main.getTabs().add(gamesTab);

        dataTab.setClosable(false);
        graphTab.setClosable(false);
        knowledgeTab.setClosable(false);
        searchTab.setClosable(false);
        gamesTab.setClosable(false);

        this.data.setSide(Side.TOP);
        this.graphs.setSide(Side.TOP);
        this.knowledge.setSide(Side.TOP);
        this.search.setSide(Side.TOP);
        this.games.setSide(Side.TOP);
    }

    public TabPane getMain() {
        return main;
    }

    public void addDataSet(String name, DataSet dataSet, boolean closable) {
        Tab tab = new Tab(name, DataView.getTableView(dataSet));
        tab.setClosable(closable);
        this.data.getTabs().add(tab);
        this.main.getSelectionModel().select(dataTab);
        this.data.getSelectionModel().select(tab);
    }

    public void addGraph(String name, Graph graph, boolean closable) {
        Tab tab = new Tab(name, GraphView.getGraphDisplay(graph));
        tab.setClosable(closable);
        this.graphs.getTabs().add(tab);
        this.main.getSelectionModel().select(graphTab);
        this.graphs.getSelectionModel().select(tab);
    }

    public void addSearchResults(String name, Graph graph, boolean closable) {
        Tab tab = new Tab(name, GraphView.getGraphDisplay(graph));
        tab.setClosable(closable);
        this.search.getTabs().add(tab);
        this.main.getSelectionModel().select(searchTab);
        this.graphs.getSelectionModel().select(tab);
    }

    public void addGame(String name, Pane pane) {
        String _name = Utils.nextName(name, this.getGameNames());

        Tab tab = new Tab(_name, pane);
        this.games.getTabs().add(tab);
        this.main.getSelectionModel().select(gamesTab);
        this.games.getSelectionModel().select(tab);
    }

    public Collection<String> getGameNames() {
        List<String> names = new ArrayList<>();

        for (Tab tab : this.games.getTabs()) {
            names.add(tab.getText());
        }

        return names;
    }

    public TreeItem<String> getTreeItem() {
        return treeItem;
    }
}

