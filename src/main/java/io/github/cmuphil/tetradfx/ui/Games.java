package io.github.cmuphil.tetradfx.ui;

import static io.github.cmuphil.tetradfx.ui.GraphView.addGame;

public class Games {
    public static void baseGamesOnDataset() {
        NamesToContents.getInstance().getSelectedContents().clearGames();

        addGame("""
                This the PC Search Game. We are assuming here that the underlying model is a DAG and that there are no latent variables. If you don't think this is true of your data, maybe you shouldn't play this game!\n
                You are allowed to remove edges or orient colliders based on conditional independence facts that you ascertain from the variables. The graph will start with a complete graph, and you may test conditional independence facts like dsep(A, B | C) by clickin on the nodes A, B, and C in sequence. You will be told whether the independence holds or does not. You may click "Remove edge A--B" or "Orient collider A->B<-C" where you click on the B.\n
                We will handle the implied orientation rules (Meek rules) for you at each step.\n
                You're allowed to backtrack, though this will count against your number of steps!\n
                We will tell you the number of edges in your graph at each step. If a true graph is available, the goal is to get to SHD = 0! Otherwise you're on your own Can you do it in the fewest number of steps? Good luck!""",
                "PC Search Game");

        addGame("""
                This is the Permutation Search Game! We are assuming that the underlying model is a DAG and that there are no latent variables. Is this a good assumption for your data?\n
                Each permutation of the graph implies a DAG. We will give you a random permutation, and you need to rearrange the nodes so that the implied DAG is correct!\n
                We will show you the implied DAG at each step and tell you the number of edges in the graph. Try to get a graph with the minimum number of edges in the fewest number of moves you can!\n
                Maybe you will come up with a new permutation algorithm!""",
                "Permutation Search Game");
    }

    static void baseGamesOnGraph() {
        NamesToContents.getInstance().getSelectedContents().clearGames();

        addGame("""
                This the D-separation Game. We will give you potential d-separation facts, and you need to say whether the d-separation facts hold in the graph you've selectd!\n
                You get to say how many d-separation facts you want to check. We will keep score for you.\n
                Can you get all of them right? Good luck! Don't forget to check descendants of colliders!""",
                "D-separation Game");

        addGame("""
                This the PC Search Game. We are assuming here that the graph is a DAG and that there are no latent variables. If you don't think this is true, maybe you shouldn't play this game!\n
                You are allowed to remove edges or orient colliders based on conditional independence facts that you ascertain from the variables. The graph will start with a complete graph, and you may test conditional independence facts like dsep(A, B | C) by clickin on the nodes A, B, and C in sequence. You will be told whether d-separation holds or does not. You may click \\"Remove edge A--B\\" or \\"Orient collider A->B<-C\\" where you click on the B.\n
                We will handle the implied orientation rules (Meek rules) for you at each step.
                You're allowed to backtrack!
                We will tell you the SHD score of your graph at each step. The goal is to get to SHD = 0! Can you do it in the fewest number of steps? Good luck!""",
                "PC Search Game");

        addGame("""
                This is the Permutation Search Game! We are assuming that the correct model is a DAG and that there are no latent variables. Is this a good assumption for your graph?\n
                Each permutation of the graph implies a DAG. We will give you a random permutation, and you need to rearrange the nodes so that the implied DAG is correct!\n
                We will show you the implied DAG at each step and tell you the number of edges in the graph. Try to get a graph with the minimum number of edges int he fewest number of moves you can!\n
                Maybe you will come up with a new permutation algorithm!""",
                "Permutation Search Game");
    }
}
