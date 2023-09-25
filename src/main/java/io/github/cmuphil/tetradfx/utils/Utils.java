package io.github.cmuphil.tetradfx.utils;

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

            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
