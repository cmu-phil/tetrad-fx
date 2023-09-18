# tetrad-fx

We're doing some doodlings here to try to make an alternative, 
lightweight [Tetrad](https://github.com/cmu-phil/tetrad) UI using JavaFX. The idea is to use the 
Tetrad library (and continue to refine and develop it without 
giving up the Swing app) but present that material in a perhaps 
different interface with a new look and feel, taking advantage 
of what's available in JavaFX (which is quite a lot and is 
strongly supported by the community).

One idea we had, which we're currently pursuing, is to shift the focus of Tetrad away from simulation studies and more toward analysis of particular datasets, so what we're doing at the moment is making an app that allows you to load a dataset, transform the data in various ways, do a search on data yielding a graph, transform that graph in various ways. Some graph layouts are included. Currently to search or do other operations you need ot use the context menus.

We added a simple (yet effective) facility for saving and loading 
datasets and graphs. We'd had problems with Java serialization before
with the Swing app where new versions broke old serialziations in
irretrievable ways. So we're using text-based serialization here.
Data files are saved as flat tab-delimited files. Graphs are saved
in Tetrad's text format. Saving and loading sessions amounts to
zipping and unzipping this material.

Knowledge has not yet been added, since we need to code up a 
knowledge editor, and also we need a parameter editor.

We added a gadget to list the variables and their types, give 
basic infomration about them, and allow the user to take notes 
on particular variables. This may morph an may not be kept.

The menus don't all do the right things yet.

This project is a bit of a mess at the moment but will hopefully 
clear up in a few days. 2023-9-16. We are aiming for a first 
draft of this project (without any modeling facilities) with 
at least a workable subset of functionality by the end of 
September. Please bear with.
