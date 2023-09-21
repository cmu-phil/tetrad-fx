package io.github.cmuphil.tetradfx.ui;

import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.DataWriter;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphSaveLoadUtils;
import edu.cmu.tetrad.util.Parameters;
import io.github.cmuphil.tetradfx.utils.NameUtils;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

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
    private final TextArea parametersArea = new TextArea("");
    private final TextArea notesArea = new TextArea("");
    private final TabPane mainTabPane;
    private final TabPane data = new TabPane();
    private final TabPane valence = new TabPane();
    private final TabPane graphs = new TabPane();
    private final TabPane search = new TabPane();
    private final TabPane games = new TabPane();
    private final TreeItem<String> treeItem;
    private final File dataDir;
    private final File graphDir;
    private final File searchDir;
    private final Map<TableView, DataSet> dataSetMap = new HashMap<>();
    private boolean valenceAdded = false;
    private final String name;
    private final Map<Tab, String> tabsToParameters = new HashMap<>();
    private final Map<Tab, String> tabsToNotes = new HashMap<>();

    /**
     * Creates a new project.
     *
     * @param dataSet     The dataset to add to the session. (This may be null.)
     * @param graph       The graph to add to the session. (This may be null.)
     * @param projectName The name of the project.
     * @param dataName    The name of the dataset. (This may be null.)
     * @param graphName   The name of the graph. (This may be null.)
     * @param dir         The directory to save the project in.
     */
    public Project(DataSet dataSet, Graph graph, String projectName, String dataName, String graphName, File dir) {
        this.mainTabPane = new TabPane();
        this.mainTabPane.setPrefSize(1000, 800);
        this.mainTabPane.setSide(Side.LEFT);
        this.treeItem = new TreeItem<>(projectName);
        this.name = projectName;
        dataTab = new Tab("Data", data);
        Tab valenceTab = new Tab("Valence", valence);
        searchTab = new Tab("Search", search);
        graphTab = new Tab("Other Graphs", graphs);
        gamesTab = new Tab("Games", games);
        dataDir = new File(dir, "data");
        searchDir = new File(dir, "search_graphs");
        graphDir = new File(dir, "other_graphs");
        notesArea.setWrapText(true);
        parametersArea.setWrapText(true);

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

        this.mainTabPane.getTabs().add(dataTab);
        this.mainTabPane.getTabs().add(valenceTab);
        this.mainTabPane.getTabs().add(searchTab);
//        this.mainTabPane.getTabs().add(new Tab("Knowledge", new TextArea()));
        this.mainTabPane.getTabs().add(graphTab);
//        this.mainTabPane.getTabs().add(new Tab("Estimations", new TextArea()));
        this.mainTabPane.getTabs().add(gamesTab);
        dataTab.setClosable(false);
        valenceTab.setClosable(false);
        graphTab.setClosable(false);
        searchTab.setClosable(false);
        gamesTab.setClosable(false);
        dataTab.setOnSelectionChanged(event -> setParametersAndNotesText(data));
        valenceTab.setOnSelectionChanged(event -> setParametersAndNotesText(valence));
        graphTab.setOnSelectionChanged(event -> setParametersAndNotesText(graphs));
        searchTab.setOnSelectionChanged(event -> setParametersAndNotesText(search));
        gamesTab.setOnSelectionChanged(event -> setParametersAndNotesText(games));
        this.data.setSide(Side.TOP);
        this.valence.setSide(Side.TOP);
        this.graphs.setSide(Side.TOP);
        this.search.setSide(Side.TOP);
        this.games.setSide(Side.TOP);
        setParametersAndNotesText();
    }

    /**
     * Returns the main tab pane for this project.
     *
     * @return The main tab pane.
     */
    public TabPane getMainTabPane() {
        return mainTabPane;
    }

    /**
     * Adds a dataset to the data tab.
     *
     * @param name     The name of the dataset.
     * @param dataSet  The dataset.
     * @param nextName Whether to append a number to the name if it already exists.
     * @param closable Whether the tab should be closable.
     */
    public void addDataSet(String name, DataSet dataSet, boolean nextName, boolean closable) {
        if (name == null) {
            throw new NullPointerException("Name cannot be null");
        }

        if (nextName) {
            name = NameUtils.nextName(name, this.getDataNames());
        }

        TableView<DataView.DataRow> tableView = DataView.getTableView(dataSet);
        dataSetMap.put(tableView, dataSet);
        Tab tab = new Tab(name, tableView);
        tab.setClosable(closable);
        this.data.getTabs().add(tab);
        this.mainTabPane.getSelectionModel().select(dataTab);
        this.data.getSelectionModel().select(tab);
        tabsToParameters.put(tab, "");
        tabsToNotes.put(tab, "");
        tab.setOnSelectionChanged(event -> setParametersAndNotesText());
        File file = new File(dataDir, name.replace(' ', '_') + ".txt");

        try {
            try (PrintWriter writer = new PrintWriter(file)) {
                DataWriter.writeRectangularData(dataSet, writer, '\t');
            }
        } catch (IOException e) {
            System.out.println("Could not write data set to file");
        }

        if (!valenceAdded) {
            Tab valence = new Tab("Variables", new VariablesView(dataSet).getTableView());
            valence.setClosable(closable);
            this.valence.getTabs().add(valence);
            valenceAdded = true;

            this.valence.getTabs().add(new Tab("Missing Values", new TextArea()));
            this.valence.getTabs().add(new Tab("Data Set Comments"));
        }

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

    /**
     * Adds a graph to the graph tab.
     * @param name The name of the graph.
     * @param graph The graph.
     * @param nextName Whether to append a number to the name if it already exists.
     * @param closable Whether the tab should be closable.
     */
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
        this.mainTabPane.getSelectionModel().select(graphTab);
        this.graphs.getSelectionModel().select(tab);
        tab.setOnSelectionChanged(event -> setParametersAndNotesText());
        var _name = name.replace(' ', '_') + ".txt";
        var file = new File(graphDir, _name);
        GraphSaveLoadUtils.saveGraph(graph , file, false);

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

    /**
     * Adds a search result to the search tab.
     * @param name The name of the search result.
     * @param graph The graph.
     * @param closable Whether the tab should be closable.
     * @param nextName Whether to append a number to the name if it already exists.
     * @param parameters The parameters used to generate the search result.
     * @param usedParameters The parameters that were actually used to generate the search result.
     */
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
        this.mainTabPane.getSelectionModel().select(searchTab);
        this.search.getSelectionModel().select(tab);
        tabsToParameters.put(tab, "");
        tabsToNotes.put(tab, "");
        tab.setOnSelectionChanged(event -> setParametersAndNotesText());
        setParametersText(tab, parameters, usedParameters);
        var _name = name.replace(' ', '_') + ".txt";
        var file = new File(searchDir, _name);
        GraphSaveLoadUtils.saveGraph(graph , file, false);

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

    /**
     * Adds a game to the games tab.
     * @param name The name of the game.
     * @param pane The pane containing the game.
     * @param nextName Whether to append a number to the name if it already exists.
     */
    public void addGame(String name, Pane pane, boolean nextName) {
        if (name == null) {
            throw new NullPointerException("Name cannot be null");
        }

        if (nextName) {
            name = NameUtils.nextName(name, this.getGameNames());
        }

        Tab tab = new Tab(name, pane);
        this.games.getTabs().add(tab);
        this.mainTabPane.getSelectionModel().select(gamesTab);
        this.games.getSelectionModel().select(tab);
        tabsToParameters.put(tab, "");
        tabsToNotes.put(tab, "");
        tab.setOnSelectionChanged(event -> setParametersAndNotesText());
        tab.setOnClosed(event -> System.out.println(tab.getText() + " was closed."));
    }

    /**
     * Returns the names of the datasets in this project.
     * @return The names of the datasets.
     */
    public Collection<String> getDataNames() {
        List<String> names = new ArrayList<>();

        for (Tab tab : this.data.getTabs()) {
            names.add(tab.getText());
        }

        return names;
    }

    /**
     * Returns the names of the graphs in this project.
     * @return The names of the graphs.
     */
    public Collection<String> getGraphNames() {
        List<String> names = new ArrayList<>();

        for (Tab tab : this.graphs.getTabs()) {
            names.add(tab.getText());
        }

        return names;
    }

    /**
     * Returns the names of the search results in this project.
     * @return The names of the search results.
     */
    public Collection<String> getSearchNames() {
        List<String> names = new ArrayList<>();

        for (Tab tab : this.search.getTabs()) {
            names.add(tab.getText());
        }

        return names;
    }

    /**
     * Returns the names of the games in this project.
     * @return The names of the games.
     */
    public Collection<String> getGameNames() {
        List<String> names = new ArrayList<>();

        for (Tab tab : this.games.getTabs()) {
            names.add(tab.getText());
        }

        return names;
    }

    /**
     * Returns the tree item for this project. This is used to display the project in the project tree and
     * is stored in the project so that it can be reacted to.
     * @return The tree item.
     */
    public TreeItem<String> getTreeItem() {
        return treeItem;
    }

    /**
     * Returns the parameters area, which is stored in this project so that its text can be modified.
     * @return The parameters area.
     */
    public TextArea getParametersArea() {
        return parametersArea;
    }

    /**
     * Returns the notes area, which is stored in this project so that its text can be modified.
     *
     * @return The notes area.
     */
    public TextArea getNotesArea() {
        return notesArea;
    }

    /**
     * Returns the selected dataset for this project.
     * @return The selected dataset.
     */
    public DataSet getSelectedDataSet() {
        Tab selected = data.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Node content1 = selected.getContent();

            if (content1 instanceof TableView content) {
                return dataSetMap.get(content);
            }
        }

        return null;
    }

    /**
     * Returns the name of this project.
     *
     * @return The name of this project.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the text of the parameters and notes areas.
     */
    public void setParametersAndNotesText() {
        TabPane tabPane = (TabPane) mainTabPane.getSelectionModel().getSelectedItem().getContent();
        setParametersAndNotesText(tabPane);
    }

    /**
     * Sets the text of the parameters area.
     * @param parameters     The parameters.
     * @param usedParameters The parameter names that were actually used.
     */
    private void setParametersText(Tab tab, Parameters parameters, List<String> usedParameters) {
        this.parametersArea.clear();

        for (String parameter : usedParameters) {
            String s = parameter + "=" + parameters.get(parameter) + "\n";
            this.parametersArea.appendText(s);
        }

        tabsToParameters.put(tab, parametersArea.getText());
    }

    /**
     * Sets the text of the parameters and notes areas and adds key listeners to the parameters area and notes area so
     * that the text can be saved when the user types.
     *
     * @param tabPane The tab pane to get the selected tab from.
     */
    private void setParametersAndNotesText(TabPane tabPane) {
        Tab selected = tabPane.getSelectionModel().getSelectedItem();
        parametersArea.setText(getParameterString(selected));
        notesArea.setText(getNoteString(selected));

        parametersArea.setOnKeyTyped(event -> {
            tabsToParameters.put(selected, parametersArea.getText());
        });

        notesArea.setOnKeyTyped(event -> {
            tabsToNotes.put(selected, notesArea.getText());
        });
    }

    private String getParameterString(Tab tab) {
        tabsToParameters.putIfAbsent(tab, "");
        return tabsToParameters.get(tab);
    }

    private String getNoteString(Tab selected) {
        tabsToNotes.putIfAbsent(selected, "");
        return tabsToNotes.get(selected);
    }
}

