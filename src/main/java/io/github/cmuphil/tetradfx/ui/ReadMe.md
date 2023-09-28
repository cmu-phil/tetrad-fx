## This package contains the classes that make up the TetradFX UI.

[A lot of the boilerplate code here was done by ChatGPT and GitHub's Copilot. Everything's been cross-checked.]

TODO FOR THE FIRST DRAFT BY THE END OF SEPTEMBER, AFTER WHICH WE WILL STOP AND DECIDE WHETHER TO CONTINUE:

_This is done now, 2023-9-25, but I want to go through and make things smoother and more robust._

## Goals of the first draft. (These are not in any particular order.)

* Allow the user to load datasets.
* Allow the user to load graphs.
* Allow the user to view even large, dense graphs.
* Allow the user to view even large datasets within the persistence limits.
* Allow the user to start projects in a session by loading datasets.
* Allow the user to make stock simulations with associated true graphs.
* Allow the user to delete projects in a session.
* Allow the user to delete tabs in a project.
* Allow the user to define background knowledge for a search.
* Allow the user to run select algorithms with and without background knowledge.
* Allow the user to set parameters for each search.
* Allow the user to switch back and forth between analyses for loaded datasets.
* Allow notes to be taken on each component with notes on parameter settings.
* Allow graphs to be laid out in various ways.
* Allow transformations of data and graphs.
* Allow some tutorial games to be played.
* Allow the user to enter valence information per variable.
* Allow the user to save sessions by zipping the session directory's contents
  and unzipping them (programmatically). The user can unzip these
  and recover easily readable text files defining the session.
* Allow the user to close and re-open the Tetrad-FX app without losing content.

## Some goals suggested by GitHub's Copilot:

1. Make the UI more responsive.
2. Make the UI more intuitive.
3. Make the UI more consistent.
4. Make the UI more attractive.
5. Make the UI more informative.
6. Make the UI more efficient.
7. Make the UI more robust.
8. Make the UI more persistent.
9. Make the UI more flexible.
10. Make the UI more complete.
11. Make the UI more correct.
12. Make the UI more useful.
13. Make the UI more fun.
14. Make the UI more educational.
15. Make the UI more accessible.
16. Make the UI more secure.
17. Make the UI more portable.
18. Make the UI more scalable.
19. Make the UI more maintainable.
20. Make the UI more testable.
21. Make the UI more debuggable.
22. Make the UI more configurable.
23. Make the UI more extensible.
24. Make the UI more reusable.
25. Make the UI more portable.
26. Make the UI more distributable.
27. Make the UI more installable.
28. Make the UI more deployable.

## POSTPONE FOR IF WE CONTINUE TO PURSUE THIS PROJECT:

1. Make a proper Help menu.
    1. May make a ReadTheDocs.
2. Elaborate on the Data/Variables annotator.
    1. Add a data field to the Variables tab.
    2. Download the trial of Analytica on a Windows laptop and see how they annotate datasets and variables.
    3. Figure out how to persist valence information.
    4. Persist valence.
3. Insights menu.
    1. Compare graphs side by side.
    2. Graph comparisons.
    2. Markov checker.
    2. Implement graph and data summaries for the Insights menu.
4. Graph Viewer
    1. Make the graph viewer selectable.
    2. Rubberband selection.
5. Games
    1. Make PC and d-separation games.
    2. Save and load games to the directory. (As graphs, I guess.)
    3. Persist games.
6. Add model estimation for linear and multinomial.
7. Keep track of parents and children for each component in the session. 8\
8. Adding in annotations to list all Tetrad algorithms, sort them by type, and display
   information about each algorithm.
9. Simulations
    1. Currently, just a few stock simulation types are available, but Tetrad has a robust simulation
       facility. We do not intend to make. This simulation facility is a centerpiece of this app, but
       there's no reason not to allow the user to use it (or at least some of it) if they want to, so
       adding parameter editing here might be nice.
10. Add oracle independence searches for graphs.
    1 Persist valence tabs.
1. Persist game tabs.
1. The user may need to increase RAM, as large datasets stored and loaded in memory can take
   up a lot of space. So they need to be told how to do this and maybe given a friendly
   interface.
