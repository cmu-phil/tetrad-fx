NEWS 2023-09-23 Added a paarmeter editor for search. Still need to add parameters for test and/or scores.

This package contains the classes that make up the TetradFX UI.

Goals of first draft:

* Allow the user to view even large dense graphs.
* Allow the user to view even large datasets, within the limits of persistence.
* Allow the user to start projects in a session by loading datasets.
* Allow the user to make stock simulations with associated true graphs.
* Allow the user to delete projects in a session.
* Allow the user to delete tabs in a project.
* Allow the user to define background knowledge for a search.
* Allow the user to run select algorithms with and without background knowledge.
* Allow the user to set parameters for each search.
* Allow the user to switch back and forth between analyses for loaded datasets.
* Allow notes to be taken on each component with notes on parameters settings as well.
* Allow graphs to be laid out in various ways.
* Allow transformations of data and graphs.
* Allow some tutorial games to be played.
* Allow the user to enter valence information per variable.
* Allow the user to save out sessions by zipping the contents of the session directory
  and unzip them by unzipping (programmatically). The user is free to unzip these
  themselves and recover easily readable text files defining the session.
* Allow the user to close and re-open the Tetrad-FX app without losing content.

TODO FOR THE FIRST DRAFT BY THE END OF SEPTEMBER, AFTER WHICH WE WILL STOP AND DECIDE WHETHER TO CONTINUE:

1. Add more standard controls to the data loader.
1. Add parameters to parameter editor for test and/or score.
1. Make a Knowledge editor, persist knowledge.
  
POSTPONE FOR IF WE CONTINUE TO PURSUE THIS PROJECT:

1. Make a proper Help menu.
    1. May make a ReadTheDocs.
1. Elaborate on Data/Variables annotator. 
    1. Add a data field to the Variables tab.
    1. Download the trial of Analytica on a Windows laptop and see how they annotate datasets and variables.
    1. Figure out how to persist valence information.
    2. Persist valence. 
1. Insights menu.
    1. Implement an FX interface for the Markov checker for the Insights menu.
    1. Implement graph and data summaries for the Insights menu.
    1. Add graph comparisons to the Insights menu.
1. Graph Viewer
    1. Make the graph viewer selectable.
    1. Rubberband selection.
1. Games
    1. Make PC and d-separation games.
    1. Save and load games to the directory. (As graphs, I guess.)
    1. Persist games.
1. Add model estimation for linear and multinomial.
1. Keep track of parents and children for each component in the session.
1. Adding in annotations to list all Tetrad algorithms and sort them by type, and display
   information about each algorithm.
1. Simulations
    1. Currently there are just a few stock simulation types available, but Tetrad has a robust simulation
       facility. We are not intending to make this simulation facility a centerpiece of this app, but
       there's not reason not to allow the user to use it (or at least some of it) if they want to, so
       maybe adding some parameter editing here might be nice.
