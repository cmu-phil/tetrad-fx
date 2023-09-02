# tetrad-fx

We're doing some doodlings here to try to make a lightweight [Tetrad](https://github.com/cmu-phil/tetrad) UI using JavaFX, which should make for a better and more flexible interface than Swing. So far we have a data display (which FX basically provides, just needed a little coaxing) and a graph display. The data display is uneditable and may remain so as editing datasets in Tetrad has never really been done. (Data transformations are done but not editing; we'll add the data transformations.) But it scales to thousands of variables (given enough memory). The graph display however does need to be editable, and needs more functionality (coloring and such and more layouts), so that will come. But it also scales well to large, dense graphs, a desideratum for us, as we are trying to push these boundaries for causal search.

We started with these two components since they are the most complicated components in the Tetrad interface. As time permits we will add more. FX allows for flexible styling, so eventually we should even be able to add some of that to make it a nice interface.

We are not committed to making an FX interface, just fooling around right now, but if you have comments one way or the other feel free to leave them in the Issues list.

##### So far:

Here’s what I have in tetrad-fx so far:
* Data display
* Graph display for all simple Tetrad graph types.
    * Can drag the edges around.
    * Can do some different layouts, circle, square, force.
* Programmatic algorithm result, now BFCI.
* Menu item to load a dataset in a simple format.
* Menu item to exit.
* Parameter definitions are now in the lib jar where they belong, which is one step closer to having a legit tetrad lib.
* Added rudimentary search--you right-click on a dataset, select a search, and it runs the search using default test, score, and parameters and adds the tab for the graph. Just a couple of algorithms currently, BOSS and BFCI.

##### Plans.

* Tne next thing I want to do is add a rudimentary search menu.
* I also wan t to hook this up with JPackage to make self-contained apps.
