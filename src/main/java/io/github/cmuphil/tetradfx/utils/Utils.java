package io.github.cmuphil.tetradfx.utils;

import com.google.gson.Gson;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.RandomGraph;
import edu.cmu.tetrad.util.Parameters;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.EnumSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

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

    public static void jsonFromJava(Object object, File file) {
        Gson gson = new Gson();

        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(object, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object javaFromJson(File file, Class clazz) {
        Gson gson = new Gson();

        try (FileReader reader = new FileReader(file)) {
            return gson.fromJson(reader, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void zip(File dir, File zipFile) {
        Path sourceDir = dir.toPath(); // Replace with the path to your directory

        try (FileOutputStream fos = new FileOutputStream(zipFile);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            Files.walkFileTree(sourceDir, EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE,
                    new SimpleFileVisitor<>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            // Only regular files should be added to the zip
                            if (attrs.isRegularFile()) {
                                // Get the relative path to the source directory
                                Path relativePath = sourceDir.relativize(file);
                                zos.putNextEntry(new ZipEntry(relativePath.toString()));
                                byte[] bytes = Files.readAllBytes(file);
                                zos.write(bytes, 0, bytes.length);
                                zos.closeEntry();
                            }
                            return FileVisitResult.CONTINUE;
                        }
                    });

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void unzipDirectory(String zipFilePath, String destDirectory) throws IOException {
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdir();
        }
        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry entry = zipIn.getNextEntry();
            while (entry != null) {
                String filePath = destDirectory + File.separator + entry.getName();
                File newFile = new File(filePath);
                if (entry.isDirectory()) {
                    // If the ZipEntry is a directory, create the directory.
                    newFile.mkdirs();
                } else {
                    // For files, ensure parent directory exists before extracting
                    File parentDir = newFile.getParentFile();
                    if (!parentDir.exists()) {
                        parentDir.mkdirs();
                    }
                    extractFile(zipIn, filePath);
                }
                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
        }
    }

    private static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath))) {
            byte[] bytesIn = new byte[4096];
            int read;
            while ((read = zipIn.read(bytesIn)) != -1) {
                bos.write(bytesIn, 0, read);
            }
        }
    }

    public static void deleteDirectory(Path dir) throws IOException {
        Files.walkFileTree(dir, EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE, new FileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);  // Delete the file
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.TERMINATE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (exc == null) {
                    Files.delete(dir);  // Delete the directory
                    return FileVisitResult.CONTINUE;
                } else {
                    throw exc;
                }
            }
        });
    }

    public static void saveParameters(File file, Parameters parameters) {
        jsonFromJava(parameters, file);
    }

    public static Parameters loadParameters(File file) {
        if (file.exists()) {
            return (Parameters) javaFromJson(file, Parameters.class);
        } else {
            return new Parameters();
        }
    }
}
