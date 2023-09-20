package io.github.cmuphil.tetradfx.ui;

import edu.cmu.tetrad.data.DataSet;

/**
 * Keeps track of selected things, like datasets. (Currenly only datasets.)
 *
 * @author josephramsey
 */
public class Selected {
    public static DataSet getSelectedData () {
        Project selectedProject = Session.getInstance().getSelectedProject();
        DataSet selectedDataSet = selectedProject.getSelectedDataSet();

        System.out.println("Selected project: " + selectedProject.getName());
        System.out.println("Selected dataset: " + selectedDataSet.getName());

        return selectedDataSet;
    }
}
