package io.github.cmuphil.tetradfx.ui;

import edu.cmu.tetrad.data.DataSet;

public class Selected {
    public static DataSet getSelectedData () {
        return NamesToProjects.getInstance().getSelectedProject().getSelectedDataSet();
    }
}
