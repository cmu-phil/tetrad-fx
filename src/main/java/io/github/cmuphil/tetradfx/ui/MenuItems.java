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
import javafx.scene.control.MenuItem;

import java.util.ArrayList;
import java.util.List;

public class MenuItems {
    public static List<MenuItem> searchMenuItems() {
        DataSet dataSet = Selected.selectedData;

        if (dataSet == null) {
            return new ArrayList<>();
        }

        List<Class> algorithms = new ArrayList<>();

        algorithms.add(Boss.class);
        algorithms.add(Grasp.class);
        algorithms.add(Pc.class);
        algorithms.add(Fges.class);
        algorithms.add(Cpc.class);

        algorithms.add(Fci.class);
        algorithms.add(Gfci.class);
        algorithms.add(Bfci.class);
        algorithms.add(GraspFci.class);

        return DataView.getSearchMenuItems(dataSet, algorithms);
    }
}
