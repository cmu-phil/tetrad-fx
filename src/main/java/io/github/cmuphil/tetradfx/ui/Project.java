package io.github.cmuphil.tetradfx.ui;

import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.DataWriter;
import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphSaveLoadUtils;
import edu.cmu.tetrad.util.Parameters;
import io.github.cmuphil.tetradfx.utils.Utils;
import javafx.collections.ObservableList;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
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
    private final Tab knowledgeTab;
    private final Tab gamesTab;

    private final TextArea parametersArea = new TextArea("");
    private final TextArea notesArea = new TextArea("");

    private final TabPane sessionTabPane;
    private final TabPane data = new TabPane();
    private final TabPane insights = new TabPane();
    private final TabPane graphs = new TabPane();
    private final TabPane search = new TabPane();
    private final TabPane knowledge = new TabPane();
    private final TabPane games = new TabPane();

    private final TreeItem<String> treeItem;

    private final File dataDir;
    private final File graphDir;
    private final File searchDir;
    private final File knowledgeDir;
    private final File gamesDir;

    private final Map<Tab, String> tabsToParameters = new HashMap<>();
    private final Map<Tab, String> tabsToNotes = new HashMap<>();

    private final Map<Tab, String> tabsToParametersPrompts = new HashMap<>();
    private final Map<Tab, String> tabsToNotesPrompts = new HashMap<>();

    private final Map<Tab, Object> dataSetMap = new HashMap<>();
    private final Map<Tab, Object> knowledgeMap = new HashMap<>();
    private boolean valenceAdded = false;

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
        this.treeItem = new TreeItem<>(projectName);

        this.sessionTabPane = new TabPane();
        this.sessionTabPane.setPrefSize(1000, 800);
        this.sessionTabPane.setSide(Side.LEFT);
        this.sessionTabPane.setTabDragPolicy(TabPane.TabDragPolicy.REORDER);

        data.setTabDragPolicy(TabPane.TabDragPolicy.REORDER);
        insights.setTabDragPolicy(TabPane.TabDragPolicy.REORDER);
        search.setTabDragPolicy(TabPane.TabDragPolicy.REORDER);
        knowledge.setTabDragPolicy(TabPane.TabDragPolicy.REORDER);
        graphs.setTabDragPolicy(TabPane.TabDragPolicy.REORDER);
        games.setTabDragPolicy(TabPane.TabDragPolicy.REORDER);

        // Set the various tabs in the session tab pane.
        dataTab = new Tab("Data", data);
        dataDir = new File(dir, "data");

        if (!dataDir.exists()) {
            boolean made2 = dataDir.mkdir();

            if (!made2) {
                throw new IllegalArgumentException("Could not make directory " + dataDir.getPath());
            }
        }

        Tab insightsTab = new Tab("Insignts", insights);

        searchTab = new Tab("Search", search);

        VBox nodeSearch = new VBox();
        Button buttonSearch = new Button("New Search");
        nodeSearch.getChildren().add(buttonSearch);

        Tab plusTab = managePlusTab1(search, nodeSearch);

        search.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            if (newTab == plusTab) {
                System.out.println("Selected tab: " + newTab.getText());

                Tab tab = Utils.getTabByName(search, "New Tab");

                if (tab != null) {
                    search.getSelectionModel().select(tab);
                    return;
                }

                ObservableList<Tab> tabs = search.getTabs();
                Tab lastTab = tabs.get(tabs.size() - 1);

                Tab newTab1 = new Tab("New Tab", nodeSearch);
                newTab1.setClosable(true);
                tabs.add(tabs.indexOf(lastTab), newTab1);
                search.getSelectionModel().select(newTab1);
            }
        });

        buttonSearch.setOnMousePressed(event -> {
            ContextMenu contextMenu = new ContextMenu();
            List<MenuItem> c = MenuItems.searchFromDataMenuItems(Session.getInstance().getParameters(), Session.getInstance().getSessionDir());
            contextMenu.getItems().addAll(c);
            contextMenu.show(buttonSearch, event.getScreenX(), event.getScreenY());
        });

        searchDir = new File(dir, "search");

        if (!searchDir.exists()) {
            boolean made = searchDir.mkdir();

            if (!made) {
                throw new IllegalArgumentException("Could not make directory " + searchDir.getPath());
            }
        }

        knowledgeTab = new Tab("Knowledge", knowledge);
        knowledgeDir = new File(dir, "knowledge");

        if (!knowledgeDir.exists()) {
            boolean made = knowledgeDir.mkdir();

            if (!made) {
                throw new IllegalArgumentException("Could not make directory " + knowledgeDir.getPath());
            }
        }

        VBox nodeKnowledge = new VBox();
        Button buttonKnowledge = new Button("New Knowledge");
        nodeKnowledge.getChildren().add(buttonKnowledge);

        Tab plusTabKnowledge = managePlusTab1(knowledge, nodeKnowledge);

        knowledge.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            if (newTab == plusTabKnowledge) {
                System.out.println("Selected tab: " + newTab.getText());

                Tab tab = Utils.getTabByName(knowledge, "New Tab");

                if (tab != null) {
                    knowledge.getSelectionModel().select(tab);
                    return;
                }

                ObservableList<Tab> tabs = knowledge.getTabs();
                Tab lastTab = tabs.get(tabs.size() - 1);

                Tab newTab1 = new Tab("New Tab", nodeKnowledge);
                newTab1.setClosable(true);
                tabs.add(tabs.indexOf(lastTab), newTab1);
                knowledge.getSelectionModel().select(newTab1);
            }
        });

        buttonKnowledge.setOnAction(e -> {
            List<String> variableNames = getSelectedDataSet().getVariableNames();
            Knowledge knowledge1 = new Knowledge(variableNames);
            addKnowledge("Knowledge", knowledge1, true);
        });

        graphTab = new Tab("Other Graphs", graphs);
        graphDir = new File(dir, "other_graphs");

        if (!graphDir.exists()) {
            boolean made1 = graphDir.mkdir();

            if (!made1) {
                throw new IllegalArgumentException("Could not make directory " + graphDir.getPath());
            }
        }

        gamesTab = new Tab("Games", games);
        gamesDir = new File("games");

        VBox nodeGames = new VBox();
        Button buttonGames = new Button("New Game");
        nodeGames.getChildren().add(buttonGames);

        Tab plusTabGames = managePlusTab1(games, nodeGames);

        games.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            if (newTab == plusTabGames) {
                Tab tab = Utils.getTabByName(games, "New Tab");

                if (tab != null) {
                    games.getSelectionModel().select(tab);
                    return;
                }

                ObservableList<Tab> tabs = games.getTabs();
                Tab lastTab = tabs.get(tabs.size() - 1);

                Tab newTab1 = new Tab("New Tab", nodeGames);
                newTab1.setClosable(true);
                tabs.add(tabs.indexOf(lastTab), newTab1);
                games.getSelectionModel().select(newTab1);
            }
        });

        buttonGames.setOnMousePressed(event -> {
            ContextMenu contextMenu = new ContextMenu();
            List<MenuItem> items = MenuItems.getGameMenuItems();
            contextMenu.getItems().addAll(items);
            contextMenu.show(buttonGames, event.getScreenX(), event.getScreenY());
        });

        notesArea.setWrapText(true);
        parametersArea.setWrapText(true);

        if (dataSet != null) {
            addDataSet(dataName, dataSet, false);
        }

        if (graph != null) {
            addGraph(graphName, graph, false);
        }

        setUpTabPane(sessionTabPane, dataTab, data);
        setUpTabPane(sessionTabPane, insightsTab, insights);
        setUpTabPane(sessionTabPane, knowledgeTab, knowledge);
        setUpTabPane(sessionTabPane, searchTab, search);
        setUpTabPane(sessionTabPane, graphTab, graphs);
        setUpTabPane(sessionTabPane, gamesTab, games);

        setParametersAndNotesText();

        selectIfNonempty(dataTab);
    }


    /**
     * Returns the main tab pane for this project.
     *
     * @return The main tab pane.
     */
    public TabPane getSessionTabPane() {
        return sessionTabPane;
    }

    /**
     * Adds a dataset to the data tab.
     *
     * @param name     The name of the dataset.
     * @param dataSet  The dataset.
     * @param nextName Whether to append a number to the name if it already exists.
     */
    public void addDataSet(String name, DataSet dataSet, boolean nextName) {
        if (name == null) {
            throw new NullPointerException("Name cannot be null");
        }

        if (nextName) {
            name = Utils.nextName(name, this.getDataNames());
        }

        String prefix = name.replace(' ', '_');

        TableView<DataView.DataRow> editor = DataView.getTableView(dataSet);
        Tab tab = new Tab(name, editor);
        dataSetMap.put(tab, dataSet);

        if (!valenceAdded) {
            Tab valence = new Tab("Variables", new VariablesView(dataSet).getTableView());
            valence.setClosable(false);
            this.insights.getTabs().add(valence);
            valenceAdded = true;
        }

        File file = new File(dataDir, prefix + ".txt");

        try {
            try (PrintWriter writer = new PrintWriter(file)) {
                DataWriter.writeRectangularData(dataSet, writer, '\t');
            }
        } catch (IOException e) {
            System.out.println("Could not write data set to file");
        }

        addTab(this.data, tab, dataDir, false);
        addHandling(name, data, dataTab, dataSetMap, dataDir, tab, prefix, !"Data".equals(name));
    }

    /**
     * Adds a graph to the graph tab.
     *
     * @param name     The name of the graph.
     * @param graph    The graph.
     * @param nextName Whether to append a number to the name if it already exists.
     */
    public void addGraph(String name, Graph graph, boolean nextName) {
        if (name == null) {
            throw new NullPointerException("Name cannot be null");
        }

        if (nextName) {
            name = Utils.nextName(name, this.getGraphNames());
        }

        String prefix = name.replace(' ', '_');

        Tab tab = new Tab(name, GraphView.getGraphDisplay(graph));

        tab.setOnClosed(event -> {
            selectIfNonempty(graphTab);

            if (new File(graphDir, prefix + ".txt").exists()) {
                if (new File(graphDir, prefix + ".txt").delete()) {
                    System.out.println("File deleted successfully");
                } else {
                    System.out.println("Failed to delete the file");
                }
            } else {
                System.out.println("File does not exist");
            }
        });

        addTab(this.graphs, tab,graphDir, false);
//        graphs.getTabs().add(tab);
        addHandling(name, graphs, graphTab, null, graphDir, tab, prefix, !"True Graph".equals(name));
        GraphSaveLoadUtils.saveGraph(graph, new File(graphDir, prefix + ".txt"), false);
    }

    /**
     * Adds a search result to the search tab.
     *
     * @param name           The name of the tab.
     * @param graph          The graph.
     * @param nextName       Whether to append a number to the name if it already exists.
     * @param parameters     The parameters used to generate the search result.
     * @param usedParameters The parameters that were actually used to generate the search result.
     */
    public void addSearchResult(String name, Graph graph, boolean nextName, Parameters parameters, List<String> usedParameters) {
        if (name == null) {
            throw new NullPointerException("Name cannot be null");
        }

        if (nextName) {
            name = Utils.nextName(name, this.getSearchNames());
        }

        String prefix = name.replace(' ', '_');

        this.sessionTabPane.getSelectionModel().select(dataTab);

        GraphSaveLoadUtils.saveGraph(graph, new File(this.searchDir, prefix + ".txt"),
                false);

        Tab tab = Utils.getTabByName(search, "New Tab");

        if (tab == null) {
            tab = new Tab(name, GraphView.getGraphDisplay(graph));
            search.getTabs().add(search.getTabs().size() - 1, tab);
        } else {
            tab.setText(name);
            tab.setContent(GraphView.getGraphDisplay(graph));
            writeTabOrder(search, searchDir);
        }

        addHandling(name, search, searchTab, null, searchDir, tab, prefix, true);
        setParametersText(tab, parameters, usedParameters);
        managePlusTab2(this.sessionTabPane, this.search, this.searchTab, new File(this.searchDir,
                name.replace(' ', '_') + ".txt"));
    }

    public void addKnowledge(String name, Knowledge knowledge, boolean nextName) {
        if (name == null) {
            throw new NullPointerException("Name cannot be null");
        }

        if (nextName) {
            name = Utils.nextName(name, this.getKnowledgeNames());
        }

        String prefix = name.replace(' ', '_');
        var file = new File(knowledgeDir, prefix + ".txt");

        Node editor = new RegexFilter(knowledge, file).getEditor();

        try {
            DataWriter.saveKnowledge(knowledge, new FileWriter(file));
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Dialog");
            alert.setHeaderText(null); // You can set a header text or keep it null
            alert.setContentText("Could not save knowledge: " + e.getMessage());
            alert.showAndWait();
        }

        Tab tab = Utils.getTabByName(this.knowledge, "New Tab");

        if (tab == null) {
            tab = new Tab(name, editor);
            addTab(this.knowledge, tab, knowledgeDir, true);
        } else {
            tab.setText(name);
            tab.setContent(editor);
            writeTabOrder(this.knowledge, knowledgeDir);
        }

        knowledgeMap.put(tab, knowledge);
        addHandling(name, this.knowledge, knowledgeTab, knowledgeMap, knowledgeDir, tab, prefix, true);
        managePlusTab2(this.sessionTabPane, this.knowledge, this.knowledgeTab, new File(this.knowledgeDir,
                name.replace(' ', '_') + ".txt"));
    }

    /**
     * Adds a game to the games tab.
     *
     * @param name     The name of the game.
     * @param pane     The pane containing the game.
     * @param nextName Whether to append a number to the name if it already exists.
     */
    public void addGame(String name, Pane pane, boolean nextName) {
        if (name == null) {
            throw new NullPointerException("Name cannot be null");
        }

        if (nextName) {
            name = Utils.nextName(name, this.getGameNames());
        }

        String prefix = name.replace(' ', '_');

        this.sessionTabPane.getSelectionModel().select(gamesTab);

//        GraphSaveLoadUtils.saveGraph(graph, new File(this.gameDir, prefix + ".txt"),
//                false);

        Tab tab = Utils.getTabByName(games, "New Tab");

        if (tab == null) {
            tab = new Tab(name, pane);
            addTab(this.games, tab, gamesDir, true);
        } else {
            tab.setText(name);
            tab.setContent(pane);
            writeTabOrder(this.games, gamesDir);
        }

        addHandling(name, games, gamesTab, null, graphDir, tab, prefix, true);
        managePlusTab2(this.sessionTabPane, this.games, this.gamesTab, new File(this.gamesDir,
                name.replace(' ', '_') + ".txt"));
    }

    private static void addTab(TabPane typeTabPane, Tab tab, File typeDir, boolean plugTabConfighuration) {
        if (plugTabConfighuration) {
            typeTabPane.getTabs().add(typeTabPane.getTabs().size() - 1, tab);
        } else {
            typeTabPane.getTabs().add(tab);
        }

        // Need to write out the order of the tabs to a file.
        // This is so that the tabs can be restored in the same order
        writeTabOrder(typeTabPane, typeDir);
    }

    private static void writeTabOrder(TabPane typeTabPane, File typeDir) {
        try {
            File file = new File(typeDir, "taborder.txt");
            FileWriter fileWriter = new FileWriter(file);

            for (Tab tab1 : typeTabPane.getTabs()) {
                if (!tab1.getText().equals(" + ")) {
                    fileWriter.write(tab1.getText() + "\n");
                }
            }

            fileWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void addHandling(String name, TabPane typeTabPane, Tab typeTab, Map<Tab, Object> typeTabMap,
                             File typeDir, Tab tab, String prefix, boolean closable) {
        this.sessionTabPane.getSelectionModel().select(typeTab);
        typeTabPane.getSelectionModel().select(tab);
        tabClosedAction(typeTabPane, typeTab, typeTabMap, typeDir, tab, prefix);
        selectIfNonempty(typeTab);
        persistNotes(tab, typeDir, name);
        readNotes(tab, typeDir, name);
        tab.setClosable(closable);
        tabsToParameters.put(tab, "");
        tabsToNotes.put(tab, "");
        this.search.getSelectionModel().select(tab);
        tab.setOnSelectionChanged(event -> setParametersAndNotesText());
        readNotes(tab, typeDir, name);
        persistNotes(tab, typeDir, name);
    }

    private void tabClosedAction(TabPane typeTabPane, Tab typeTab, Map<Tab, Object> typeTabMap, File typeDir, Tab thisTab,
                                 String prefix) {
        thisTab.setOnClosed(event -> {
            if (typeTabMap != null) {
                typeTabMap.remove(thisTab);
            }

            tabsToNotes.remove(thisTab);
            tabsToParameters.remove(thisTab);
            Utils.removeAllFilesWithPrefix(typeDir, prefix);
            selectIfNonempty(typeTab);

            // Need to write out the order of the tabs to a file.
            // This is so that the tabs can be restored in the same order
            writeTabOrder(typeTabPane, typeDir);
        });
    }

    /**
     * Returns the names of the datasets in this project.
     *
     * @return The names of the datasets.
     */
    public Collection<String> getDataNames() {
        List<String> names = new ArrayList<>();

        for (Tab tab : this.data.getTabs()) {
            names.add(tab.getText());
        }

        return names;
    }

    @NotNull
    private static Tab managePlusTab1(TabPane _tabPane, VBox node) {

        Tab plusTab = new Tab(" + ", new HBox());
        plusTab.setClosable(false);
        _tabPane.getTabs().add(plusTab);

        if (_tabPane.getTabs().size() == 1) {
            ObservableList<Tab> tabs = _tabPane.getTabs();
            Tab newTab1 = new Tab("New Tab", node);
            newTab1.setClosable(true);
            tabs.add(0, newTab1);
            _tabPane.getSelectionModel().select(newTab1);
        }

        return plusTab;
    }

    private void managePlusTab2(TabPane _sessionTabPane, TabPane _search, Tab _tab, File _file) {
        _sessionTabPane.getSelectionModel().select(_tab);
        _search.getSelectionModel().select(_tab);
        tabsToParameters.put(_tab, "");
        tabsToNotes.put(_tab, "");
        _tab.setOnSelectionChanged(event -> setParametersAndNotesText());

        _tab.setOnClosed(event -> {
            selectIfNonempty(_tab);

            if (_file.exists()) {
                if (_file.delete()) {
                    System.out.println("File deleted successfully");
                } else {
                    System.out.println("Failed to delete the file");
                }
            } else {
                System.out.println("File does not exist");
            }
        });
    }

    private void readNotes(Tab tab, File dir, String name) {
        String _filename1 = dir + File.separator + name.replace(' ', '_') + ".notes" + ".txt";

        tabsToNotes.putIfAbsent(tab, "");
        tabsToParameters.putIfAbsent(tab, "");
        tabsToNotesPrompts.put(tab, "Notes for " + name + ":");
        tabsToParametersPrompts.put(tab, "Parameters for " + name + ":");

        if (new File(_filename1).exists()) {
            tabsToNotes.put(tab, Utils.loadTextFromFile(new File(_filename1)));
            notesArea.setText(tabsToNotes.get(tab));
        } else {
            tabsToNotes.put(tab, "");//""Notes for " + name + ":");
            notesArea.setText(tabsToNotes.get(tab));
            notesArea.setPromptText(tabsToNotesPrompts.get(tab));
            Utils.saveTextToFile(new File(_filename1), tabsToNotes.get(tab));
        }

        String _filename2 = dir + File.separator + name.replace(' ', '_') + ".paramsNote" + ".txt";

        if (new File(_filename2).exists()) {
            tabsToParameters.put(tab, Utils.loadTextFromFile(new File(_filename2)));
            parametersArea.setText(tabsToParameters.get(tab));
        } else {
            if (tabsToParameters.get(tab).isEmpty()) {
                tabsToParameters.put(tab, "");//""Parameters for " + name + ":");
            }
            parametersArea.setPromptText(tabsToParametersPrompts.get(tab));
            Utils.saveTextToFile(new File(_filename2), tabsToParameters.get(tab));
        }
    }

    private void persistNotes(Tab tab, File dir, String _name) {
        notesArea.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_EXITED, event -> {
            String filename = dir + File.separator + _name.replace(' ', '_') + ".notes" + ".txt";
            Utils.saveTextToFile(new File(filename), tabsToNotes.get(tab));
        });

        parametersArea.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_EXITED, event -> {
            String filename = dir + File.separator + _name.replace(' ', '_') + ".paramsNote" + ".txt";
            Utils.saveTextToFile(new File(filename), tabsToParameters.get(tab));
        });

    }

    /**
     * Returns the names of the graphs in this project.
     *
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
     *
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
     * Returns the names of the knowledge files in this project.
     *
     * @return The names of the knowledge files.
     */
    public Collection<String> getKnowledgeNames() {
        List<String> names = new ArrayList<>();

        for (Tab tab : this.knowledge.getTabs()) {
            names.add(tab.getText());
        }

        return names;
    }

    /**
     * Returns the names of the games in this project.
     *
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
     * Returns the tree item for this project. This is used to display the project in the project tree and is stored in
     * the project so that it can be reacted to.
     *
     * @return The tree item.
     */
    public TreeItem<String> getTreeItem() {
        return treeItem;
    }

    /**
     * Returns the parameters area, which is stored in this project so that its text can be modified.
     *
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
     *
     * @return The selected dataset.
     */
    public DataSet getSelectedDataSet() {
        Tab selected = data.getSelectionModel().getSelectedItem();
        if (selected != null) {
            return (DataSet) dataSetMap.get(selected);
        }

        return null;
    }

    /**
     * Returns the selected knowledge for this project.
     *
     * @return The selected knowledge.
     */
    public Knowledge getSelectedKnowledge() {
        Tab selected = knowledge.getSelectionModel().getSelectedItem();
        if (selected != null) {
            return (Knowledge) knowledgeMap.get(selected);
        }

        return null;
    }

    /**
     * Sets the text of the parameters and notes areas.
     */
    public void setParametersAndNotesText() {
        Tab selectedItem = sessionTabPane.getSelectionModel().getSelectedItem();
        if (selectedItem == null) return;
        Node content = selectedItem.getContent();
        TabPane tabPane = (TabPane) content;
        setParametersAndNotesText(tabPane);
    }

    /**
     * Sets the text of the parameters area.
     *
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

        parametersArea.setPromptText(getParameterPromptString(selected));
        notesArea.setPromptText(getNotePromptString(selected));

        parametersArea.setOnKeyTyped(event -> tabsToParameters.put(selected, parametersArea.getText()));

        notesArea.setOnKeyTyped(event -> tabsToNotes.put(selected, notesArea.getText()));
    }

    private String getParameterString(Tab tab) {
        tabsToParameters.putIfAbsent(tab, "");
        return tabsToParameters.get(tab);
    }

    private String getNoteString(Tab selected) {
        tabsToNotes.putIfAbsent(selected, "");
        return tabsToNotes.get(selected);
    }

    private String getParameterPromptString(Tab tab) {
        tabsToParametersPrompts.putIfAbsent(tab, "");
        return tabsToParametersPrompts.get(tab);
    }

    private String getNotePromptString(Tab selected) {
        tabsToNotesPrompts.putIfAbsent(selected, "");
        return tabsToNotesPrompts.get(selected);
    }

    /**
     * Sets up a tab pane.
     *
     * @param _mainTabPane The main tab pane.
     * @param _dataTab     The data tab.
     * @param _data        The data tab pane.
     */
    private void setUpTabPane(TabPane _mainTabPane, Tab _dataTab, TabPane _data) {
        _mainTabPane.getTabs().add(_dataTab);
        _dataTab.setClosable(false);
        _dataTab.setOnSelectionChanged(event -> setParametersAndNotesText(_data));
        _data.setSide(Side.TOP);
    }

    private void selectIfNonempty(Tab tab) {
        TabPane tabPane = (TabPane) tab.getContent();
        if (!tabPane.getTabs().isEmpty()) {
            sessionTabPane.getSelectionModel().select(tab);
        }
    }
}

