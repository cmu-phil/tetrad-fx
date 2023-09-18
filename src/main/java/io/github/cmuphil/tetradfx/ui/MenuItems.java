package io.github.cmuphil.tetradfx.ui;

import edu.cmu.tetrad.algcomparison.algorithm.Algorithm;
import edu.cmu.tetrad.algcomparison.algorithm.oracle.cpdag.*;
import edu.cmu.tetrad.algcomparison.algorithm.oracle.pag.Bfci;
import edu.cmu.tetrad.algcomparison.algorithm.oracle.pag.Fci;
import edu.cmu.tetrad.algcomparison.algorithm.oracle.pag.Gfci;
import edu.cmu.tetrad.algcomparison.algorithm.oracle.pag.GraspFci;
import edu.cmu.tetrad.algcomparison.independence.IndependenceWrapper;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.util.Parameters;
import javafx.scene.control.Alert;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

import java.util.ArrayList;
import java.util.List;

public class MenuItems {
    public static void searchMenuItems(DataSet dataSet, Menu menu) {
        List<Algorithm> algorithms = new ArrayList<>();
        var score = DataView.getScore(dataSet);

        algorithms.add(new Boss(score));
        IndependenceWrapper test = DataView.getTest(dataSet);
        algorithms.add(new Grasp(test, score));
        algorithms.add(new Pc(test));
        algorithms.add(new Fges(score));
        algorithms.add(new Cpc(test));

        algorithms.add(new Fci(test));
        algorithms.add(new Gfci(test, score));
        algorithms.add(new Bfci(test, score));
        algorithms.add(new GraspFci(test, score));

        List<MenuItem> items = new ArrayList<>();

        for (Algorithm algorithm : algorithms) {
            MenuItem item = new MenuItem(algorithm.getDescription());
            item.setOnAction(e -> {
                if (dataSet == null) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Information Dialog");
                    alert.setHeaderText(null); // You can set a header text or keep it null
                    alert.setContentText("Please select a dataset to do a search on.");

                    alert.showAndWait();
                    return;
                }
                Graph graph = algorithm.search(dataSet, new Parameters());
                Project selected = NamesToProjects.getInstance().getSelectedProject();
                selected.addSearchResult(algorithm.getClass().getSimpleName(),
                        graph, true, true, new Parameters(), algorithm.getParameters());
            });

            items.add(item);
        }

        menu.getItems().addAll(items);
    }
}
