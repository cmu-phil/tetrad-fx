package io.github.cmuphil.tetradfx.utils;

import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.RandomGraph;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Files;
import java.util.Collection;

public class Utils {
    public static String nextName(String name, Collection<String> names) {
        for (int i = 1; i < 1000; i++) {
            String _name = name + " " + i;

            if (!names.contains(_name)) {
                return _name;
            }
        }

        throw new IllegalArgumentException("Too many names");
    }

    public static PrintWriter saveTextToFile(File file, String text) {
        PrintWriter out;

        try {
            out = new PrintWriter(Files.newOutputStream(file.toPath()));
            out.print(text);
            out.flush();
            out.close();
        } catch (IOException e1) {
            throw new IllegalArgumentException("Output file could not be opened: " + file);
        }
        return out;
    }

    public static String loadTextFromFile(File file) {
        StringBuilder sb = new StringBuilder();

        try {
            Reader in1 = new FileReader(file);
            try (BufferedReader in = new BufferedReader(in1)) {
                for (String line = in.readLine(); line != null; line = in.readLine()) {
                    line = line.trim();
                    sb.append(line).append("\n");
                }
            }

            in1.close();

            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Tab getTabByName(TabPane tabPane, String name) {
        for (Tab tab : tabPane.getTabs()) {
            if (name.equals(tab.getText())) {
                return tab;
            }
        }
        return null;
    }

    public static void removeAllFilesWithPrefix(File dir, String __name) {
        for (File _file : dir.listFiles()) {
            if (_file.getName().startsWith(__name)) {
                if (_file.exists()) {
                    if (_file.delete()) {
                        System.out.println("File deleted successfully");
                    } else {
                        System.out.println("Failed to delete the file");
                    }
                } else {
                    System.out.println("File does not exist");
                }
            }
        }
    }

    /**
     * Creates a random DAG.
     *
     * @param numNodes The number of nodes.
     * @param numEdges The number of edges.
     * @return The random DAG.
     */
    @NotNull
    public static Graph randomDag(int numNodes, int numEdges) {
        return RandomGraph.randomGraph(numNodes, 0, numEdges, 100, 100,
                100, false);
    }
}
