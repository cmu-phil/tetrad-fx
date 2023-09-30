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
data yielding a graph and transforming that graph in various ways.

We are keeping a running overview of the work here:

https://github.com/cmu-phil/tetrad-fx/blob/main/src/main/java/io/github/cmuphil/tetradfx/ui/ReadMe.md

# Some Documentation

Here is a screenshot of the first draft of the project. The little green G is Grammarly correcting my text,
sorry. Though honestly it is kind of nice to have that available.

![Picture of the current state of the project.](https://github.com/cmu-phil/tetrad-fx/blob/main/src/main/resources/App.Screenshot.2023-9.30.png)

Currently the idea is you start a project by loading a dataset or making one of a stock set of simulations.
The dataset currently cannot have discrete variables with more than 3 categories. The simulation function will
be elaborated eventually if we continue the project.

The Valence tab contains some information about variable and eventually will allow the user to enter
information about each varaible. Currently this is not persisted.

The search tab is limited to a restricted set of search algroithms in Tetrad; thsi will be 
expanded if we continue to allow all search algrothms.

The knowledge tab currently lets you indicate tiered knowledge only. The tiers are selected
from the variables in dataset using regex filtering. This needs to be expanded and possibly
another type of knowledge editor added as well for those who find regex difficult.

The other graphs will contain the true graph if this is a simulation.

Datasets can be transformed using right click context menus, as can graphs.

We plan to implement a number of games people can play to learn the concepts. Currently
there is only one game, to help users learn how permutation searches work. Have fun
with it. If you discover a new permutation algorithm let us know. (We also plan a
constraint-based game and a d-separation game, if we continue.)

Saving and loading is just zipping or unzipping the hidden project directory.
All dataset, knowledge files, and graph are stored in text files for which
parsers can easily be written if not already available. To get access to these,
simply save the session and unzip it elsewhere. It has a ".tfx" suffix just so
you know it was generated from Tetrad-FX.

The user can take notes on any component, and these notes are persisted.
When searches are done, parameter settings are saved to the Parameters
tab as well, and can be further annotation by the user. These are also
persisted.

If we continue with the project, we will turn these and other notes into a
ReadTheDocs. Also, if we continus with the project we will add an Insignts
tab to help the user understand the results (compare graphs, Markov checker,
etc.) and a Models tab to let the user to estimations (linear and
multinomial) of data for given DAG models.

# Install

It's easy to install this if you're familiar with Java, and it's easily cross-platform. 
You need a fairly recent version version of Java to run the JavaFX, but if you have one installed,
you simply need to clone the project and run the app.

To clone it, you need Git installed. Then you type:

```
git clone https://github.com/cmu-phil/tetrad-fx
```

To build it, you need to have Maven installed, and type (in the project directory):

```
mvn clean package
cd target
jpackage --input . --name Tetrad-FX --main-jar tetrad-fx-0.1-SNAPSHOT.jar 
```

This should make an installer for you on your machine, which you can then install. Then launch the 
app in the usual way.

These instructions have been tested on Intel and M1-Max Macs but not other platforms yet.
On a Mac, it makes a DMG installer file.

If we continue with the project, we will make downloadable signed installer files available for various
platforms.

