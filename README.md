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

# Picture

Here is a screenshot of the current state of the project. I guess I could have done the borders better.
The little green G is Grammarly correcting my text, sorry.

![Picture of the current state of the project.](https://github.com/cmu-phil/tetrad-fx/blob/main/src/main/resources/App.Screenshot.2023-9.30.png)

# Install

It's easy to install this if you're familiar with Java. You need a fairly
recent version version of Java to run the JavaFX, but if you have one installed,
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

