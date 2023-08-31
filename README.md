# tetrad-fx

We're doing some doodlings here to try to make a Tetrad UI using JavaFX, which should make for a better and more flexible interface than Swing. So far we have a data display (which FX basically provides, just needed a little coaxing) and a graph display. The data display is uneditable and may remain so as editing datasets in Tetrad has never really been done. (Data transformations are done but not editing; we'll add the data transformations.) The graph display however does need to be editable, and more functionality (coloring and such and more layouts), so that will come.

We started with these two components since they are the most complicated components in the Tetrad interface. As time permits we will add more. FX allows for flexible styling, so eventually we should even be able to add some of that to make it a nice interface.

We are not committed to making an FX interface, just fooling around right now, but if you have comments one way or the other feel free to leave them in the Issues list.

##### Got some cloners, wow! It's very preliminary at the moment. Let me at least tell you what my upcoming plans are.
* Next, I'm going to make a data loader UI in FX. The data loading code is fine, but it would be nice to have a lightweight UI.
* I want to flesh out the graph display. I'd like to allow it to represent PAGs for one thing, so I'll need to add circle endpoints, and I'd like to do the Tetrad-style PAG coloring.
* I'd like to add some more search method and maybe translate the gadgets that let you choose algorithm and set parameters. Not sure exactly how I'm going to do that interface yet, but it will be lightweight.
* Eventually I'd like to allow the user to represent different kinds of nodes in their own configurable way and choose colors for the graph interface. Ths should be fairly easy to do in FX.
* Oh, also, I want to hook this up with JPackage to make self-contained apps. I may as well do that up front.

There are a lot of ideas I draw on from the Swing app, though it's not clear to me yet that we just want a translation of the Swing app and not something new. This will all take me a few days.
