package io.github.cmuphil.tetradfx.utils;

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
}
