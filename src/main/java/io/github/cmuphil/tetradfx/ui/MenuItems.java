package io.github.cmuphil.tetradfx.ui;

import edu.cmu.tetrad.algcomparison.algorithm.Algorithm;
import edu.cmu.tetrad.algcomparison.algorithm.oracle.cpdag.*;
import edu.cmu.tetrad.algcomparison.algorithm.oracle.pag.Bfci;
import edu.cmu.tetrad.algcomparison.algorithm.oracle.pag.Fci;
import edu.cmu.tetrad.algcomparison.algorithm.oracle.pag.Gfci;
import edu.cmu.tetrad.algcomparison.algorithm.oracle.pag.GraspFci;
import edu.cmu.tetrad.algcomparison.independence.IndependenceWrapper;
import edu.cmu.tetrad.algcomparison.score.ScoreWrapper;
import edu.cmu.tetrad.algcomparison.utils.HasKnowledge;
import edu.cmu.tetrad.algcomparison.utils.TakesIndependenceWrapper;
import edu.cmu.tetrad.algcomparison.utils.UsesScoreWrapper;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.util.Parameters;
import edu.cmu.tetrad.util.RandomUtil;
import io.github.cmuphil.tetradfx.for751lib.DataTransforms;
import io.github.cmuphil.tetradfx.utils.Utils;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Some menu item builders.
 *
 * @author josephramsey
 */
public class MenuItems {

    /**
     * Creates a list of menu items for the algorithms, searching from the data in the currently
     * selected dataset. Note that is important here that the dataset be found on the fly, since
     * the user may have changed the selected dataset since the last time the menu was opened.
     * @return a list of menu items for the algorithms, searching from the data in the currently
     * selected dataset.
     */
    public static List<MenuItem> searchFromDataMenuItems(Parameters parameters, File sessionDir) {
        List<Class> algorithms = new ArrayList<>();

        // TODO: Add more algorithms here.
        // For this, we need to figure out how to use Jessi's annotations
        // to read in the list automatically. Also, the algorithms can be sorted by type--again, we need
        // to figure out how to read those annotations.
        algorithms.add(Boss.class);
        algorithms.add(Grasp.class);
        algorithms.add(Pc.class);
        algorithms.add(Fges.class);
        algorithms.add(Cpc.class);

        algorithms.add(Fci.class);
        algorithms.add(Gfci.class);
        algorithms.add(Bfci.class);
        algorithms.add(GraspFci.class);

        List<MenuItem> items = new ArrayList<>();

        // Sorry, IntelliJ, but I'm not going to specify a type for the Class variable, since it need
        // to change from algorithm to algorithm.
        for (Class algorithmClass : algorithms) {
            MenuItem item = new MenuItem(algorithmClass.getSimpleName());
            item.setOnAction(e -> {

                // This is currently guaranteed ot be non-null; if at some point is it not, best to display
                // an Alert and return.
                DataSet dataSet = Selected.getSelectedData();

                Algorithm algorithm;

                try {
                    if (UsesScoreWrapper.class.isAssignableFrom(algorithmClass) && (TakesIndependenceWrapper.class.isAssignableFrom(algorithmClass))) {
                        IndependenceWrapper test = DataView.getTest(dataSet);
                        ScoreWrapper score = DataView.getScore(dataSet);
                        algorithm = (Algorithm) algorithmClass.getConstructor(IndependenceWrapper.class, ScoreWrapper.class).newInstance(test, score);
                    } else if (UsesScoreWrapper.class.isAssignableFrom(algorithmClass)) {
                        ScoreWrapper score = DataView.getScore(dataSet);
                        algorithm = (Algorithm) algorithmClass.getConstructor(ScoreWrapper.class).newInstance(score);
                    } else if (TakesIndependenceWrapper.class.isAssignableFrom(algorithmClass)) {
                        IndependenceWrapper test = DataView.getTest(dataSet);
                        algorithm = (Algorithm) algorithmClass.getConstructor(IndependenceWrapper.class).newInstance(test);
                    } else {
                        algorithm = (Algorithm) algorithmClass.getConstructor().newInstance();
                    }
                } catch (Exception ex) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error Dialog");
                    alert.setHeaderText(null); // You can set a header text or keep it null
                    alert.setContentText("Could not instantiate algorithm: " + ex.getMessage());
                    alert.showAndWait();
                    return;
                }

                List<String> myParams = algorithm.getParameters();

                if (algorithm instanceof TakesIndependenceWrapper) {
                    myParams.addAll(((TakesIndependenceWrapper) algorithm).getIndependenceWrapper().getParameters());
                }

                if (algorithm instanceof UsesScoreWrapper) {
                    myParams.addAll(((UsesScoreWrapper) algorithm).getScoreWrapper().getParameters());
                }

                new ParameterDialog(parameters, myParams, sessionDir).showDialog();

                if (algorithm instanceof HasKnowledge && parameters.getBoolean("useKnowledge", false)) {
                    Knowledge knowledge = Session.getInstance().getSelectedProject().getSelectedKnowledge();

                    if (knowledge != null) {
                        ((HasKnowledge) algorithm).setKnowledge(knowledge);
                    }
                }

                Graph graph = algorithm.search(dataSet, parameters);
                Project selected = Session.getInstance().getSelectedProject();
                selected.addSearchResult(algorithm.getClass().getSimpleName(),
                        graph, true, parameters, myParams);
            });

            items.add(item);
        }

        return items;
    }

    @NotNull
    public static ContextMenu getDataContextMenu(TableView<DataView.DataRow> dataTable, DataSet dataSet) {
        var contextMenu = new ContextMenu();

        if (dataSet == null) {
            return contextMenu;
        }

        dataTable.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY ||
                    (event.getButton() == MouseButton.PRIMARY && event.isControlDown())) {
                contextMenu.show(dataTable, event.getScreenX(), event.getScreenY());
            }
        });

        var transformData = new Menu("Transform Data");
        contextMenu.getItems().add(transformData);

        if (dataSet.isContinuous()) {
            var center = new MenuItem("Center");
            center.setOnAction(e -> {
                DataSet filtered = DataTransforms.center(dataSet);
                Session.getInstance().getSelectedProject().addDataSet("Center", filtered, true);
            });
            transformData.getItems().add(center);

            var standardize = new MenuItem("Standardize");
            standardize.setOnAction(e -> {
                DataSet filtered = DataTransforms.standardizeData(dataSet);
                Session.getInstance().getSelectedProject().addDataSet("Standardize", filtered, true);
            });
            transformData.getItems().add(standardize);

            var nonparanormalTransform = new MenuItem("Nonparanormal Transform");
            nonparanormalTransform.setOnAction(e -> {
                DataSet filtered = DataTransforms.getNonparanormalTransformed(dataSet);
                Session.getInstance().getSelectedProject().addDataSet("Nonparanormal Transform", filtered, true);
            });
            transformData.getItems().add(nonparanormalTransform);

            var logTransform = new MenuItem("Log Transform");
            logTransform.setOnAction(e -> {
                DataSet filtered = DataTransforms.logData(dataSet, 10, false, 2);
                Session.getInstance().getSelectedProject().addDataSet("Log Transform", filtered, true);
            });
            transformData.getItems().add(logTransform);
        }

        var removeConstantColumns = new MenuItem("Remove Constant Columns");
        removeConstantColumns.setOnAction(e -> {
            DataSet filtered = DataTransforms.removeConstantColumns(dataSet);
            Session.getInstance().getSelectedProject().addDataSet("Remove Constant Columns", filtered, true);
        });

        transformData.getItems().add(removeConstantColumns);

        var removeDuplicateColumns = new MenuItem("Remove Duplicate Columns");
        removeDuplicateColumns.setOnAction(e -> {
            DataSet filtered = DataTransforms.removeConstantColumns(dataSet);
            Session.getInstance().getSelectedProject().addDataSet("Remove Constant Columns", filtered, true);
        });
        transformData.getItems().add(removeDuplicateColumns);

        var numericalDiscreteToContinuous = new MenuItem("Numerical Discrete to Continuous");
        numericalDiscreteToContinuous.setOnAction(e -> {
            DataSet filtered = DataTransforms.convertNumericalDiscreteToContinuous(dataSet);
            Session.getInstance().getSelectedProject().addDataSet("Numerical Discrete to Continuous", filtered, true);
        });
        transformData.getItems().add(numericalDiscreteToContinuous);

        var discretizeToBinary = new MenuItem("Discretize to Binary");
        discretizeToBinary.setOnAction(e -> {
            DataSet filtered = DataTransforms.discretize(dataSet, 2, true);
            Session.getInstance().getSelectedProject().addDataSet("Discretize to Binary", filtered, true);
        });
        transformData.getItems().add(discretizeToBinary);

        var discretize = new MenuItem("Discretize to Trinary");
        discretize.setOnAction(e -> {
            DataSet filtered = DataTransforms.discretize(dataSet, 3, true);
            Session.getInstance().getSelectedProject().addDataSet("Discretize to Trinary", filtered, true);
        });
        transformData.getItems().add(discretize);

        var restrictToMeasured = new MenuItem("Restrict to Measured");
        restrictToMeasured.setOnAction(e -> {
            DataSet filtered = DataTransforms.restrictToMeasured(dataSet);
            Session.getInstance().getSelectedProject().addDataSet("Restrict to Measured", filtered, true);
        });
        transformData.getItems().add(restrictToMeasured);

        var shuffleColumns = new MenuItem("Shuffle Columns");
        shuffleColumns.setOnAction(e -> {
            DataSet filtered = DataTransforms.shuffleColumns(dataSet);
            Session.getInstance().getSelectedProject().addDataSet("Shuffle Columns", filtered, true);
        });
        transformData.getItems().add(shuffleColumns);

        return contextMenu;
    }

    @NotNull
    public static Menu getGameMenu() {
        Menu games = new Menu("Games");
        games.getItems().addAll(getGameMenuItems());
        return games;
    }

    @NotNull
    public static List<MenuItem> getGameMenuItems() {
        List<MenuItem> items = new ArrayList<>();

        Menu permutationGames = new Menu("Permutation Games");

        MenuItem basedOnGraph_4_4 = new MenuItem("Make a random game with 4 nodes and 4 edges");
        MenuItem basedOnGraph_5_5 = new MenuItem("Make a random game with 5 nodes and 5 edges");
        MenuItem basedOnGraph_6_6 = new MenuItem("Make a random game with 6 nodes and 6 edges");
        MenuItem basedOnGraph_7_7 = new MenuItem("Make a random game with 7 nodes and 7 edges");
        MenuItem basedOnGraph_10_10 = new MenuItem("Make a random game with 10 nodes and 10 edges");
        MenuItem basedOnGraph_10_15 = new MenuItem("Make a random game with 10 nodes and 15 edges");
        MenuItem surpriseMe = new MenuItem("Surprise me!");

        basedOnGraph_4_4.setOnAction(e -> Games.baseGamesOnGraph(Utils.randomDag(4, 4)));
        basedOnGraph_5_5.setOnAction(e -> Games.baseGamesOnGraph(Utils.randomDag(5, 5)));
        basedOnGraph_6_6.setOnAction(e -> Games.baseGamesOnGraph(Utils.randomDag(6, 6)));
        basedOnGraph_7_7.setOnAction(e -> Games.baseGamesOnGraph(Utils.randomDag(7, 7)));
        basedOnGraph_10_10.setOnAction(e -> Games.baseGamesOnGraph(Utils.randomDag(10, 10)));
        basedOnGraph_10_15.setOnAction(e -> Games.baseGamesOnGraph(Utils.randomDag(10, 15)));
        surpriseMe.setOnAction(e -> Games.baseGamesOnGraph(Utils.randomDag(RandomUtil.getInstance().nextInt(6) + 5,
                RandomUtil.getInstance().nextInt(10) + 3)));

        permutationGames.getItems().addAll(basedOnGraph_4_4, basedOnGraph_5_5, basedOnGraph_6_6, basedOnGraph_7_7,
                basedOnGraph_10_10, basedOnGraph_10_15, surpriseMe);

        items.add(permutationGames);
        return items;
    }
}
