# tetrad-fx

We're doing some doodlings here to try to make a Tetrad UI using JavaFX, which should make for a better and more flexible interface than Swing. So far we have a data display (which FX basically provides, just needed a little coaxing) and a graph display. The data display is uneditable and may remain so as editing datasets in Tetrad has never really been done. (Data transformations are done but not editing; we'll add the data transformations.) The graph display however does need to be editable, and more functionality (coloring and such and more layouts), so that will come.

We started with these two components since they are the most complicated components in the Tetrad interface. As time permits we will add more. FX allows for flexible styling, so eventually we should even be able to add some of that to make it a nice interface.

We are not committed to making an FX interface, just fooling around right now, but if you have comments one way or the other feel free to leave them in the Issues list.

##### Holy crap you guys are cloning me already! Awesome! But I _just made this_ :-D It's very preliminary. But please, if you have ideas for how thie project should proceed, please let me know! Or feel free to steal anything!

##### Well if you guys are going to clone it, let me at least tell you what my upcoming plans are.
* The next thing I want to do is flesh out the graph display. I'd like to allow it to represent PAGs for one thing, and do the Tetrad-style PAG coloring. I guess that's my first goal.
* Then I'd like to make the graph editable.
* Maybe if I can do it I'd like to have a feature to let you render a graph using Graphviz.
* I'm not sure yet whether I want to make a session graph as in the Swing Tetrad app, still thinking about it. Maybe I want a different kind of interface. FX is pretty flexible for this sort of thing, much moreso than Swing so far as I can tell.
* The data table currently can display both continuous and discrete columns, so I'd like to add some more search methods. Not sure exactly how I'm going to do that interface yet, but it will be lightweight.
