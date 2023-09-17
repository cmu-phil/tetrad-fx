package io.github.cmuphil.tetradfx.for751lib;

import com.google.gson.Gson;
import edu.cmu.tetrad.data.DataModel;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.util.DataConvertUtils;
import edu.pitt.dbmi.data.reader.Data;
import edu.pitt.dbmi.data.reader.DataColumn;
import edu.pitt.dbmi.data.reader.Delimiter;
import edu.pitt.dbmi.data.reader.tabular.TabularColumnFileReader;
import edu.pitt.dbmi.data.reader.tabular.TabularColumnReader;
import edu.pitt.dbmi.data.reader.tabular.TabularDataFileReader;
import edu.pitt.dbmi.data.reader.tabular.TabularDataReader;
import org.apache.commons.math3.util.FastMath;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ChangedStuffINeed {
    /**
     * Arranges the nodes in the graph in a circle.
     */
    // From LayoutUtils
    public static void circleLayout(Graph graph) {
        if (graph == null) {
            return;
        }

        int centerx = 120 + 7 * graph.getNumNodes();
        int centery = 120 + 7 * graph.getNumNodes();
        int radius = centerx - 50;

        List<Node> nodes = graph.getNodes();
        Collections.sort(nodes);

        double rad = 6.28 / nodes.size();
        double phi = .75 * 6.28;    // start from 12 o'clock.

        for (Node node : nodes) {
            int centerX = centerx + (int) (radius * FastMath.cos(phi));
            int centerY = centery + (int) (radius * FastMath.sin(phi));

            node.setCenterX(centerX);
            node.setCenterY(centerY);

            phi += rad;
        }
    }

    // From LayoutUtils
    public static void squareLayout(Graph graph) {
        List<Node> nodes = graph.getNodes();
        Collections.sort(nodes);

//        Collections.sort(nodes);

        int bufferx = 70;
        int buffery = 50;
        int spacex = 70;
        int spacey = 50;

//        int side = (int) ceil(nodes.size() / 4.0);
        int side = nodes.size() / 4;

        if (nodes.size() % 4 != 0) {
            side++;
        }

        for (int i = 0; i < side; i++) {
            if (i >= nodes.size()) {
                break;
            }
            Node node = nodes.get(i);
            node.setCenterX(bufferx + spacex * i);
            node.setCenterY(buffery);
        }

        for (int i = 0; i < side; i++) {
            if (i + side >= nodes.size()) {
                break;
            }
            Node node = nodes.get(i + side);
            node.setCenterX(bufferx + spacex * side);
            node.setCenterY(buffery + i * spacey);
        }

        for (int i = 0; i < side; i++) {
            if (i + 2 * side >= nodes.size()) {
                break;
            }
            Node node = nodes.get(i + 2 * side);
            node.setCenterX(bufferx + spacex * (side - i));
            node.setCenterY(buffery + spacey * side);
        }

        for (int i = 0; i < side; i++) {
            if (i + 3 * side >= nodes.size()) {
                break;
            }
            Node node = nodes.get(i + 3 * side);
            node.setCenterX(bufferx);
            node.setCenterY(buffery + spacey * (side  - i));
        }
    }

    public static void layoutByCausalOrder(Graph graph) {
        List<List<Node>> tiers = getTiers(graph);

        int y = 0;

        for (List<Node> tier : tiers) {
            y += 60;

            if (tier.isEmpty()) continue;

            Node node = tier.get(0);

            int width = 80;

            int x = width / 2 + 10;

            node.setCenterX(x);
            node.setCenterY(y);

            int lastHalf = width / 2;

            for (int i = 1; i < tier.size(); i++) {
                node = tier.get(i);
                int thisHalf = width / 2;
                x += lastHalf + thisHalf + 5;
                node.setCenterX(x);
                node.setCenterY(y);
                lastHalf = thisHalf;
            }
        }
    }

    /**
     * Finds the set of nodes which have no children, followed by the set of their parents, then the set of the parents'
     * parents, and so on.  The result is returned as a List of Lists.
     *
     * @return the tiers of this digraph.
     */
    private static List<List<Node>> getTiers(Graph graph) {
        Set<Node> found = new HashSet<>();
        List<List<Node>> tiers = new LinkedList<>();

        // first copy all the nodes into 'notFound'.
        Set<Node> notFound = new HashSet<>(graph.getNodes());

        // repeatedly run through the nodes left in 'notFound'.  If any node
        // has all of its parents already in 'found', then add it to the
        // getModel tier.
        while (!notFound.isEmpty()) {
            List<Node> thisTier = new LinkedList<>();

            for (Node node : notFound) {
                if (found.containsAll(graph.getParents(node))) {
                    thisTier.add(node);
                }
            }

            if (thisTier.isEmpty()) {
                tiers.add(new ArrayList<>(notFound));
                break;
            }

            // shift all the nodes in this tier from 'notFound' to 'found'.
            thisTier.forEach(notFound::remove);
            found.addAll(thisTier);

            // add the getModel tier to the list of tiers.
            tiers.add(thisTier);
        }

        return tiers;
    }


    /**
     * Loads a continuous dataset from a file.
     *
     * @param file               The text file to load the data from.
     * @param commentMarker      The comment marker as a string--e.g., "//".
     * @param quoteCharacter     The quote character, e.g., '\"'.
     * @param missingValueMarker The missing value marker as a string--e.g., "NA".
     * @param hasHeader          True if the first row of the data contains variable names.
     * @param delimiter          One of the options in the Delimiter enum--e.g., Delimiter.TAB.
     * @param excludeFirstColumn If the first column should be excluded from the data.
     * @return The loaded DataSet.
     * @throws IOException If an error occurred in reading the file.
     */
    // From SimpleDataLoader
    @NotNull
    public static DataSet loadContinuousData(File file, String commentMarker, char quoteCharacter,
                                             String missingValueMarker, boolean hasHeader, Delimiter delimiter,
                                             boolean excludeFirstColumn)
            throws IOException {
        TabularColumnReader columnReader = new TabularColumnFileReader(file.toPath(), delimiter);
        DataColumn[] dataColumns = columnReader.readInDataColumns(excludeFirstColumn ?
                new int[]{1} : new int[]{}, false);

        columnReader.setCommentMarker(commentMarker);

        TabularDataReader dataReader = new TabularDataFileReader(file.toPath(), delimiter);

        // Need to specify commentMarker, .... again to the TabularDataFileReader
        dataReader.setCommentMarker(commentMarker);
        dataReader.setMissingDataMarker(missingValueMarker);
        dataReader.setQuoteCharacter(quoteCharacter);

        Data data = dataReader.read(dataColumns, hasHeader);
        DataModel dataModel = DataConvertUtils.toDataModel(data);
        dataModel.setName(file.getName());

        return (DataSet) dataModel;
    }


    /**
     * Loads a discrete dataset from a file.
     *
     * @param file               The text file to load the data from.
     * @param commentMarker      The comment marker as a string--e.g., "//".
     * @param quoteCharacter     The quote character, e.g., '\"'.
     * @param missingValueMarker The missing value marker as a string--e.g., "NA".
     * @param hasHeader          True if the first row of the data contains variable names.
     * @param delimiter          One of the options in the Delimiter enum--e.g., Delimiter.TAB.
     * @param excludeFirstColumn If the first columns should be excluded from the data.
     * @return The loaded DataSet.
     * @throws IOException If an error occurred in reading the file.
     */
    // From SimpleDataLoader
    @NotNull
    public static DataSet loadDiscreteData(File file, String commentMarker, char quoteCharacter,
                                           String missingValueMarker, boolean hasHeader, Delimiter delimiter,
                                           boolean excludeFirstColumn)
            throws IOException {
        TabularColumnReader columnReader = new TabularColumnFileReader(file.toPath(), delimiter);
        DataColumn[] dataColumns = columnReader.readInDataColumns(excludeFirstColumn ?
                new int[]{1} : new int[]{}, true);

        columnReader.setCommentMarker(commentMarker);

        TabularDataReader dataReader = new TabularDataFileReader(file.toPath(), delimiter);

        // Need to specify commentMarker, .... again to the TabularDataFileReader
        dataReader.setCommentMarker(commentMarker);
        dataReader.setMissingDataMarker(missingValueMarker);
        dataReader.setQuoteCharacter(quoteCharacter);

        Data data = dataReader.read(dataColumns, hasHeader);
        DataModel dataModel = DataConvertUtils.toDataModel(data);
        dataModel.setName(file.getName());

        return (DataSet) dataModel;
    }


    /**
     * Loads a mixed dataset from a file.
     *
     * @param file               The text file to load the data from.
     * @param commentMarker      The comment marker as a string--e.g., "//".
     * @param quoteCharacter     The quote character, e.g., '\"'.
     * @param missingValueMarker The missing value marker as a string--e.g., "NA".
     * @param hasHeader          True if the first row of the data contains variable names.
     * @param maxNumCategories   The maximum number of distinct entries in a columns alloed in order for the column to
     *                           be parsed as discrete.
     * @param delimiter          One of the options in the Delimiter enum--e.g., Delimiter.TAB.
     * @param excludeFirstColumn If the first columns should be excluded from the data set.
     * @return The loaded DataSet.
     * @throws IOException If an error occurred in reading the file.
     */
    // From SimpleDataLoader
    @NotNull
    public static DataSet loadMixedData(File file, String commentMarker, char quoteCharacter,
                                        String missingValueMarker, boolean hasHeader, int maxNumCategories,
                                        Delimiter delimiter, boolean excludeFirstColumn)
            throws IOException {
        TabularColumnReader columnReader = new TabularColumnFileReader(file.toPath(), delimiter);
        DataColumn[] dataColumns = columnReader.readInDataColumns(excludeFirstColumn ?
                new int[]{1} : new int[]{}, false);

        columnReader.setCommentMarker(commentMarker);

        TabularDataReader dataReader = new TabularDataFileReader(file.toPath(), delimiter);

        // Need to specify commentMarker, .... again to the TabularDataFileReader
        dataReader.setCommentMarker(commentMarker);
        dataReader.setMissingDataMarker(missingValueMarker);
        dataReader.setQuoteCharacter(quoteCharacter);
        dataReader.determineDiscreteDataColumns(dataColumns, maxNumCategories, hasHeader);

        Data data = dataReader.read(dataColumns, hasHeader);
        DataModel dataModel = DataConvertUtils.toDataModel(data);
        dataModel.setName(file.getName());

        return (DataSet) dataModel;
    }

    public static void jsonFromJava(Object object, File file) {
        Gson gson = new Gson();

        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(object, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }

//        try {
//            Gson gson = new GsonBuilder().setPrettyPrinting().create();
//            String text = gson.toJson(object);
//            PrintWriter out = new PrintWriter(file);
//            out.println(text);
//            Preferences.userRoot().put("fileSaveLocation", file.getParent());
//            out.close();
//        } catch (FileNotFoundException e1) {
//            e1.printStackTrace();
//            throw new RuntimeException("Not a directed graph.", e1);
//        } catch (IllegalArgumentException e1) {
//            throw new RuntimeException(e1);
//        }
    }

    public static Object javaFromJson(File file, Class clazz) {
        Gson gson = new Gson();

        try (FileReader reader = new FileReader(file)) {
            return gson.fromJson(reader, clazz);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

//    public static void saveToJson(Object object, File file) {
//        try {
//            Gson gson = new GsonBuilder().setPrettyPrinting().create();
//            String text = gson.toJson(object);
//            PrintWriter out = new PrintWriter(file);
//            out.println(text);
//            Preferences.userRoot().put("fileSaveLocation", file.getParent());
//            out.close();
//        } catch (FileNotFoundException e1) {
//            e1.printStackTrace();
//            throw new RuntimeException("Not a directed graph.", e1);
//        } catch (IllegalArgumentException e1) {
//            e1.printStackTrace();
//            throw new RuntimeException(e1);
//        }
//    }

//    public static Graph loadGraphJson(File file) {
//        try {
//            Reader in1 = new FileReader(file);
//            return readerToGraphJson(in1);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        throw new IllegalStateException();
//    }
//
//    public static Graph readerToGraphJson(Reader reader) throws IOException {
//        BufferedReader in = new BufferedReader(reader);
//
//        StringBuilder json = new StringBuilder();
//        String line;
//
//        while ((line = in.readLine()) != null) {
//            json.append(line.trim());
//        }
//
//        return JsonUtils.parseJSONObjectToTetradGraph(json.toString());
//    }

    public static void zip(File dir, File zipFile) {
        Path sourceDir = dir.toPath(); // Replace with the path to your directory
//        String zipFileName = "output.zip"; // Name of the output zip file

        try (FileOutputStream fos = new FileOutputStream(zipFile);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            Files.walkFileTree(sourceDir, EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE,
                    new SimpleFileVisitor<Path>() {
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
            e.printStackTrace();
        }
    }

    public static void unzip(File zipFilePath, File destDirectory) throws IOException {
        if (!destDirectory.exists()) {
            destDirectory.mkdir();
        }

        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry entry = zipIn.getNextEntry();

            // Iterates over entries in the zip file
            while (entry != null) {
                String filePath = destDirectory + File.separator + entry.getName();
                if (!entry.isDirectory()) {
                    // If the entry is a file, extracts it
                    extractFile(zipIn, filePath);
                } else {
                    // If the entry is a directory, make the directory
                    File dir = new File(filePath);
                    dir.mkdir();
                }
                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
        }
    }

    private static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath))) {
            byte[] bytesIn = new byte[4096];
            int read = 0;
            while ((read = zipIn.read(bytesIn)) != -1) {
                bos.write(bytesIn, 0, read);
            }
        }
    }
}
