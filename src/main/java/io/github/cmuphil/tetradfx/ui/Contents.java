package io.github.cmuphil.tetradfx.ui;

import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.DataWriter;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphSaveLoadUtils;
import javafx.geometry.Side;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.Pane;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
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

    private final File dataDir;
    private final File graphDir;
    private final File knowledgeDir;
    private final File searchDir;
    private final File gamesDir;

    public Contents(DataSet dataSet, Graph graph, String contentsName, String dataName, String graphName, File dir) {
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

        dataDir = new File(dir, "data");
        graphDir = new File(dir, "graph");
        knowledgeDir = new File(dir, "knowledge");
        searchDir = new File(dir, "search");
        gamesDir = new File(dir, "games");

        if (!dataDir.exists()) {
            boolean made = dataDir.mkdir();

            if (!made) {
                throw new IllegalArgumentException("Could not make directory " + dataDir.getPath());
            }
        }

        if (!graphDir.exists()) {
            boolean made = graphDir.mkdir();

            if (!made) {
                throw new IllegalArgumentException("Could not make directory " + graphDir.getPath());
            }
        }

        if (!knowledgeDir.exists()) {
            boolean made = knowledgeDir.mkdir();

            if (!made) {
                throw new IllegalArgumentException("Could not make directory " + knowledgeDir.getPath());
            }
        }

        if (!searchDir.exists()) {
            boolean made = searchDir.mkdir();

            if (!made) {
                throw new IllegalArgumentException("Could not make directory " + searchDir.getPath());
            }
        }

        if (!gamesDir.exists()) {
            boolean made = gamesDir.mkdir();

            if (!made) {
                throw new IllegalArgumentException("Could not make directory " + gamesDir.getPath());
            }
        }


        if (dataSet == null && graph == null) {
            this.main.getTabs().add(dataTab);
            this.main.getTabs().add(graphTab);
        } else if (dataSet != null && graph == null) {
            this.main.getTabs().add(dataTab);
            this.main.getTabs().add(graphTab);
            addDataSet(dataName, dataSet, false, true);
        } else if (dataSet == null) {
            this.main.getTabs().add(dataTab);
            this.main.getTabs().add(graphTab);
            addGraph(graphName, graph, false, true);
        } else {
            this.main.getTabs().add(dataTab);
            this.main.getTabs().add(graphTab);
            addDataSet(dataName, dataSet, false, true);
            addGraph(graphName, graph, false, true);
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

    public Contents(String contentsName, File dir) {
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

        dataDir = new File(dir, "data");
        graphDir = new File(dir, "graph");
        knowledgeDir = new File(dir, "knowledge");
        searchDir = new File(dir, "search");
        gamesDir = new File(dir, "games");

        if (!dataDir.exists()) {
            boolean made = dataDir.mkdir();

            if (!made) {
                throw new IllegalArgumentException("Could not make directory " + dataDir.getPath());
            }
        }

        if (!graphDir.exists()) {
            boolean made = graphDir.mkdir();

            if (!made) {
                throw new IllegalArgumentException("Could not make directory " + graphDir.getPath());
            }
        }

        if (!knowledgeDir.exists()) {
            boolean made = knowledgeDir.mkdir();

            if (!made) {
                throw new IllegalArgumentException("Could not make directory " + knowledgeDir.getPath());
            }
        }

        if (!searchDir.exists()) {
            boolean made = searchDir.mkdir();

            if (!made) {
                throw new IllegalArgumentException("Could not make directory " + searchDir.getPath());
            }
        }

        if (!gamesDir.exists()) {
            boolean made = gamesDir.mkdir();

            if (!made) {
                throw new IllegalArgumentException("Could not make directory " + gamesDir.getPath());
            }
        }

        this.main.getTabs().add(dataTab);
        this.main.getTabs().add(graphTab);
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

    public void addDataSet(String name, DataSet dataSet, boolean closable, boolean nextName) {
        if (name == null) {
            throw new NullPointerException("Name cannot be null");
        }

        if (nextName) {
            name = Utils.nextName(name, this.getDataNames());
        }

        Tab tab = new Tab(name, DataView.getTableView(dataSet));
        tab.setClosable(closable);
        this.data.getTabs().add(tab);
        this.main.getSelectionModel().select(dataTab);
        this.data.getSelectionModel().select(tab);

        try {
            File file = new File(dataDir,  name.replace(' ', '_') + ".txt");

            try (PrintWriter writer = new PrintWriter(file)) {
                DataWriter.writeRectangularData(dataSet, writer, '\t');
            }

        } catch (IOException e) {
            System.out.println("Could not write data set to file");
        }
    }

    public void addGraph(String name, Graph graph, boolean closable, boolean nextName) {
        if (name == null) {
            throw new NullPointerException("Name cannot be null");
        }

        if (nextName) {
            name = Utils.nextName(name, this.getGraphNames());
        }

        Tab tab = new Tab(name, GraphView.getGraphDisplay(graph));
        tab.setClosable(closable);
        this.graphs.getTabs().add(tab);
        this.main.getSelectionModel().select(graphTab);
        this.graphs.getSelectionModel().select(tab);

        String _name = name.replace(' ', '_') + ".txt";
        GraphSaveLoadUtils.saveGraph(graph , new File(graphDir, _name), false);
    }

    public void addSearchResult(String name, Graph graph, boolean closable, boolean nextName) {
        if (name == null) {
            throw new NullPointerException("Name cannot be null");
        }

        if (nextName) {
            name = Utils.nextName(name, this.getSearchNames());
        }

        Tab tab = new Tab(name, GraphView.getGraphDisplay(graph));
        tab.setClosable(closable);
        this.search.getTabs().add(tab);
        this.main.getSelectionModel().select(searchTab);
        this.search.getSelectionModel().select(tab);

        String _name = name.replace(' ', '_') + ".txt";
        GraphSaveLoadUtils.saveGraph(graph , new File(searchDir, _name), false);
    }

    public void addGame(String name, Pane pane, boolean nextName) {
        if (name == null) {
            throw new NullPointerException("Name cannot be null");
        }

        if (nextName) {
            name = Utils.nextName(name, this.getGameNames());
        }

        Tab tab = new Tab(name, pane);
        this.games.getTabs().add(tab);
        this.main.getSelectionModel().select(gamesTab);
        this.games.getSelectionModel().select(tab);
    }

    public Collection<String> getDataNames() {
        List<String> names = new ArrayList<>();

        for (Tab tab : this.data.getTabs()) {
            names.add(tab.getText());
        }

        return names;
    }

    public Collection<String> getGraphNames() {
        List<String> names = new ArrayList<>();

        for (Tab tab : this.graphs.getTabs()) {
            names.add(tab.getText());
        }

        return names;
    }

    public Collection<String> getSearchNames() {
        List<String> names = new ArrayList<>();

        for (Tab tab : this.search.getTabs()) {
            names.add(tab.getText());
        }

        return names;
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

