# tetrad-fx

We're doing some doodling here to try to make an alternative, 
lightweight, modern [Tetrad](https://github.com/cmu-phil/tetrad) App using JavaFX. 
The idea is to use the Tetrad library (and continue to refine and 
develop that without giving up the Swing app) but present that material 
in a different interface with a new look and feel, taking advantage 
of what's available in JavaFX.

A first draft is done as promised, September 30, 2023, Comments are welcome.
A lot of functionality is still missing, but I do not want to continue
to work on the project unless there's a concensus that I should continue.
So far it's been a fairly minimal amount of work.

An idea we have been pursuing is to shift the focus of
Tetrad away from simulation studies and more toward analysis of particular 
datasets. What we're doing at the moment is making an app that allows 
you to load a dataset, transform the data in various ways, search on
data with tiered knowledge, yielding a graph, and transform that graph in various ways.

We are keeping a running overview of the work here:

https://github.com/cmu-phil/tetrad-fx/blob/main/src/main/java/io/github/cmuphil/tetradfx/ui/ReadMe.md

# Screenshot

Here is a screenshot of the first draft of the project.

![Picture of the current state of the project.](https://github.com/cmu-phil/tetrad-fx/blob/main/src/main/resources/App.Screenshot.2023-9.30.png)

# Some Documentation

Some advice follows.

1. The idea is that you start a project by loading a dataset or making one of a stock set of simulations.
The dataset currently cannot have discrete variables with more than 3 categories.
This data is placed in the Data tab and cannot be removed inside the project.

1. The Knowledge tab currently lets you indicate tiered background knowledge only. The tiers are selected
from the variables in dataset using regex filtering. The tiers can be interpreted as giveing
temporal information.

1. The Insights tab contains some information about variable. Currently this contains a Valence
tool that lets gives the user some information about the variables in the dataset, which is not
currently persisted.

1. The search tab is limited to a restricted set of search algroithms in Tetrad.

1. The Other Graphs tab will contain the true graph if this is a simulation.

1. Datasets can be transformed using right click context menus, as can graphs.

1. Currently there is only one game in the Games tab, to help users learn how permutation
searches work. Have fun with it. If you discover a new permutation algorithm let us know.

1. Saving and loading of the session is just zipping or unzipping the hidden project directory.
All dataset, knowledge files, and graph are stored in text files for which
parsers can easily be written if not already available. To get access to these,
simply save the session and unzip it elsewhere. It has a ".tfx" suffix just so
you know it was generated from Tetrad-FX.

1. The user can take notes on any component in tabs, and these notes are persisted.
When searches are done, parameter settings are saved to the Parameters
tab as well, and can be further annotation by the user. These are also
persisted.

# Future Plans if we Continue

1. We plan to implement a number of games people can play to learn the concepts. (We plan a
constraint-based game and a d-separation game, if we continue--perhaps others.)

1. We plan to expand the simulation options.

1. We plan to make the valence tab much more detailed, so that the use can enter
specific information about each variable. This will be persisted.

1. Also in the Insights tab we plan to add more tools to help the user understand the data
and the search results.

1. We plan to expand the search options.

1. Knowledge needs to be expanded to allow specific required or forbidden edges, and possibly
another type of knowledge editor needs to be added as well for those who find regexes difficult.

1. We will also add a Models tab to let the user to estimations (linear and
multinomial) of data for given DAG models. All of this is functionality
that is currently in the Tetrad library; we just have to build UIs for it.

1. We plan to turn these and other comments into a ReadTheDocs. 

More plans are given at the above link.

# Install

It's easy to install this if you're familiar with JavaFX, and it's easily cross-platform. 
You need a fairly recent version version of Java to run the JavaFX, but if you have one installed,
you simply need to clone the project and run the app.

To clone it, you need Git installed. Then you type:

```
git clone https://github.com/cmu-phil/tetrad-fx
```

To build it, you need to have Maven installed and the JavaFX code installed and type (in the project directory):

```
mvn clean package
```

This should download and install all the necessary libraries, including the Tetrad 7.5.0 library.

Then to run Tetrad-FX at the command line:

```
java -jar target/tetrad-fx-0.1-SNAPSHOT.jar --add-modules javafx.controls,javafx.fxml MyJavaFXApp
```

Alternatively you could package the jar as an app:

```'
cd target
jpackage --input . --name Tetrad-FX --main-jar tetrad-fx-0.1-SNAPSHOT.jar
```

This should make an installer for you on your machine, which you can then install. Then launch the 
app in the usual way.

These instructions have been tested on Intel and M1-Max Macs but not other platforms yet.
On a Mac, it makes a DMG installer file.

If we continue with the project, we will make downloadable signed installer files available for various
platforms.

