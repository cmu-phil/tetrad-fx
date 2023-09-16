package io.github.cmuphil.tetradfx.ui;

import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.DataWriter;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.util.Parameters;
import io.github.cmuphil.tetradfx.for751lib.ChangedStuffINeed;
import io.github.cmuphil.tetradfx.utils.NameUtils;
import javafx.geometry.Side;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.Pane;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * <p>Stores all of the tabbed panes for a given project in a session.</p>
 *
 * @author josephramsey
 */
public class Project {
    private final Tab dataTab;
    private final Tab graphTab;
    private final Tab searchTab;
    private final Tab gamesTab;
    private final TextArea parametersArea = new TextArea("Parameters will be displayed here when available.");

    private final TabPane main;

    private final TabPane data = new TabPane();
    private final TabPane variables = new TabPane();
    private final TabPane graphs = new TabPane();
    private final TabPane search = new TabPane();
    private final TabPane games = new TabPane();
    private final TreeItem<String> treeItem;

    private final File dataDir;
    private final File graphDir;
    private final File searchDir;

    public Project(DataSet dataSet, Graph graph, String projectName, String dataName, String graphName, File dir) {
        this.main = new TabPane();
        this.main.setPrefSize(1000, 800);
        this.main.setSide(Side.LEFT);
        this.treeItem = new TreeItem<>(projectName);

        dataTab = new Tab("Data", data);
        searchTab = new Tab("Search Graphs", search);
        graphTab = new Tab("Other Graphs", graphs);
        gamesTab = new Tab("Games", games);

        dataDir = new File(dir, "data");
        searchDir = new File(dir, "search_graphs");
        graphDir = new File(dir, "other_graphs");

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

        if (!searchDir.exists()) {
            boolean made = searchDir.mkdir();

            if (!made) {
                throw new IllegalArgumentException("Could not make directory " + searchDir.getPath());
            }
        }

        if (dataSet != null) {
            addDataSet(dataName, dataSet, false, false);
        }

        if (graph != null) {
            addGraph(graphName, graph, true, false);
        }

//        Tab variablesTab = new Tab("Variables", variables);

        this.main.getTabs().add(dataTab);
//        this.main.getTabs().add(variablesTab);
        this.main.getTabs().add(searchTab);
        this.main.getTabs().add(graphTab);
        this.main.getTabs().add(gamesTab);

//        variablesTab.setClosable(false);
        dataTab.setClosable(false);
        graphTab.setClosable(false);
        searchTab.setClosable(false);
        gamesTab.setClosable(false);

        this.data.setSide(Side.TOP);
        this.graphs.setSide(Side.TOP);
        this.search.setSide(Side.TOP);
        this.games.setSide(Side.TOP);
    }

    public TabPane getMain() {
        return main;
    }

    public void addDataSet(String name, DataSet dataSet, boolean nextName, boolean closable) {
        if (name == null) {
            throw new NullPointerException("Name cannot be null");
        }

        if (nextName) {
            name = NameUtils.nextName(name, this.getDataNames());
        }

        Tab tab = new Tab(name, DataView.getTableView(dataSet));
        tab.setClosable(closable);
        this.data.getTabs().add(tab);
        this.main.getSelectionModel().select(dataTab);
        this.data.getSelectionModel().select(tab);

        File file = new File(dataDir, name.replace(' ', '_') + ".txt");

        try {
            try (PrintWriter writer = new PrintWriter(file)) {
                DataWriter.writeRectangularData(dataSet, writer, '\t');
            }

        } catch (IOException e) {
            System.out.println("Could not write data set to file");
        }

        this.variables.getTabs().clear();
        Tab tab2 = new Tab(name, DataView.getTableView(dataSet));
        tab2.setClosable(closable);

        // It's important that this not be closable. The user may have put a lot of work into it, and
        // it should not be accidentally deleted.
        Tab _variables = new Tab("Variables", new VariableView(dataSet).getTableView());
        _variables.setClosable(closable);
        this.data.getTabs().add(_variables);

        tab.setOnClosed(event -> {
            if (file.exists()) {
                if (file.delete()) {
                    System.out.println("File deleted successfully");
                } else {
                    System.out.println("Failed to delete the file");
                }
            } else {
                System.out.println("File does not exist");
            }
        });
    }

    public void addGraph(String name, Graph graph, boolean nextName, boolean closable) {
        if (name == null) {
            throw new NullPointerException("Name cannot be null");
        }

        if (nextName) {
            name = NameUtils.nextName(name, this.getGraphNames());
        }

        Tab tab = new Tab(name, GraphView.getGraphDisplay(graph));
        tab.setClosable(closable);
        this.graphs.getTabs().add(tab);
        this.main.getSelectionModel().select(graphTab);
        this.graphs.getSelectionModel().select(tab);

        String _name = name.replace(' ', '_') + ".json";
        File file = new File(graphDir, _name);
        ChangedStuffINeed.jsonFromJava(graph, file);

//        GraphSaveLoadUtils.saveGraph(graph , new File(graphDir, _name), false);

        tab.setOnClosed(event -> {
            if (file.exists()) {
                if (file.delete()) {
                    System.out.println("File deleted successfully");
                } else {
                    System.out.println("Failed to delete the file");
                }
            } else {
                System.out.println("File does not exist");
            }
        });
    }

    public void addSearchResult(String name, Graph graph, boolean closable, boolean nextName, Parameters parameters,
                                List<String> usedParameters) {
        if (name == null) {
            throw new NullPointerException("Name cannot be null");
        }

        if (nextName) {
            name = NameUtils.nextName(name, this.getSearchNames());
        }

        Tab tab = new Tab(name, GraphView.getGraphDisplay(graph));
        tab.setClosable(closable);
        this.search.getTabs().add(tab);
        this.main.getSelectionModel().select(searchTab);
        this.search.getSelectionModel().select(tab);

        setParametersText(parameters, usedParameters);

        String _name = name.replace(' ', '_') + ".json";
        File file = new File(searchDir, _name);
        ChangedStuffINeed.jsonFromJava(graph, file);


//        String _name = name.replace(' ', '_') + ".txt";
//        GraphSaveLoadUtils.saveGraph(graph , new File(searchDir, _name), false);

        tab.setOnClosed(event -> {
            if (file.exists()) {
                if (file.delete()) {
                    System.out.println("File deleted successfully");
                } else {
                    System.out.println("Failed to delete the file");
                }
            } else {
                System.out.println("File does not exist");
            }
        });
    }

    private void setParametersText(Parameters parameters, List<String> usedParameters) {
        this.parametersArea.clear();

        for (String parameter : usedParameters) {
            this.parametersArea.appendText(parameter + "=" + parameters.get(parameter) + "\n");
        }
    }

    public void addGame(String name, Pane pane, boolean nextName) {
        if (name == null) {
            throw new NullPointerException("Name cannot be null");
        }

        if (nextName) {
            name = NameUtils.nextName(name, this.getGameNames());
        }

        Tab tab = new Tab(name, pane);
        this.games.getTabs().add(tab);
        this.main.getSelectionModel().select(gamesTab);
        this.games.getSelectionModel().select(tab);

        tab.setOnClosed(event -> {
            System.out.println(tab.getText() + " was closed.");
        });
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

    public TextArea getParametersArea() {
        return parametersArea;
    }
}

