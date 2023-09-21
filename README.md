# tetrad-fx

We're doing some doodlings here to try to make an alternative, 
lightweight [Tetrad](https://github.com/cmu-phil/tetrad) UI using JavaFX. 
The idea is to use the Tetrad library (and continue to refine and 
develop it without giving up the Swing app) but present that material 
in a different interface with a new look and feel, taking advantage 
of what's available in JavaFX (which is quite a lot and is 
strongly supported by the community).

This project is a bit of a mess at the moment (2023-9-20) but will hopefully 
clear up in a few days. We are aiming for a first 
draft of this project (without any modeling facilities) with 
at least a workable subset of search functionality by the end of 
September, 2023, or early October at the latest, and then we can decide whether 
to continue the project or scrap it. Please bear with. Once the first draft is
available, of course, any comments will be welcome.

The names of classes and package structure are still in flux, so please 
don't expect stability quite yet.

One idea we had, which we're currently pursuing, is to shift the focus of
Tetrad away from simulation studies and more toward analysis of particular datasets,
so what we're doing at the moment is making an app that allows you to load a dataset,
transform the data in various ways, do a search on data yielding a graph, and transform 
that graph in various ways. Some graph layouts are included. We have moved the
Search menu to top-level finally, where searches are done now on the currently
selected dataset. 

I am keeping a running to-do list here, which will be kept up to date:

https://github.com/cmu-phil/tetrad-fx/blob/main/src/main/java/io/github/cmuphil/tetradfx/ui/ReadMe.md


