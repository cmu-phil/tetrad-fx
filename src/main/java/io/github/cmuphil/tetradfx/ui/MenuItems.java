package io.github.cmuphil.tetradfx.ui;

import edu.cmu.tetrad.algcomparison.algorithm.Algorithm;
import edu.cmu.tetrad.algcomparison.algorithm.oracle.cpdag.*;
import edu.cmu.tetrad.algcomparison.algorithm.oracle.pag.Bfci;
import edu.cmu.tetrad.algcomparison.algorithm.oracle.pag.Fci;
import edu.cmu.tetrad.algcomparison.algorithm.oracle.pag.Gfci;
import edu.cmu.tetrad.algcomparison.algorithm.oracle.pag.GraspFci;
import edu.cmu.tetrad.algcomparison.independence.IndependenceWrapper;
import edu.cmu.tetrad.algcomparison.score.ScoreWrapper;
import edu.cmu.tetrad.algcomparison.utils.TakesIndependenceWrapper;
import edu.cmu.tetrad.algcomparison.utils.UsesScoreWrapper;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.util.Parameters;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuItem;

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
    public static List<MenuItem> searchFromDataMenuItems() {
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

                Graph graph = algorithm.search(dataSet, new Parameters());
                Project selected = Session.getInstance().getSelectedProject();
                selected.addSearchResult(algorithm.getClass().getSimpleName(),
                        graph, true, true, new Parameters(), algorithm.getParameters());
            });

            items.add(item);
        }

        return items;
    }

}
