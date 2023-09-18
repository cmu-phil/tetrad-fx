package io.github.cmuphil.tetradfx.ui;

import edu.cmu.tetrad.algcomparison.algorithm.Algorithm;
import edu.cmu.tetrad.algcomparison.algorithm.oracle.cpdag.*;
import edu.cmu.tetrad.algcomparison.algorithm.oracle.pag.Bfci;
import edu.cmu.tetrad.algcomparison.algorithm.oracle.pag.Fci;
import edu.cmu.tetrad.algcomparison.algorithm.oracle.pag.Gfci;
import edu.cmu.tetrad.algcomparison.algorithm.oracle.pag.GraspFci;
import edu.cmu.tetrad.algcomparison.independence.IndependenceWrapper;
import edu.cmu.tetrad.data.DataSet;
import javafx.scene.control.Alert;
import javafx.scene.control.Menu;

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

        var items = DataView.getMenuItems(dataSet, algorithms);

        menu.getItems().addAll(items);

    }
}
