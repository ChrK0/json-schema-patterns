import de.uni_passau.sds.ecmascript2brics.*;
import org.apache.commons.text.StringEscapeUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Main {

    private static int countOfFiles;
    private static int countOfFilesWithPatterns;
    private static int countPatternsTotal;
    private static int countPatternPropertiesTotal;
    private static int countPatternsUnique;
    private static int countPatternPropertiesUnique;
    private static List<String> patterns = new LinkedList<>();
    private static final List<String> invalidPatterns = new LinkedList<>();
    private static final List<String> notSupportedPatterns = new LinkedList<>();
    private static final List<String> notConvertablePatterns = new LinkedList<>();
    private static final List<String> anchoredPatterns = new LinkedList<>();
    private static final List<String> anchoredPatternsInside = new LinkedList<>();
    private static final List<String> unanchoredPatterns = new LinkedList<>();
    private static final List<String> nullablePatterns = new LinkedList<>();
    private static final String SUMMARY_FILE = "summary.csv";
    private static final String PATTERN_FILE = "patterns.csv";
    private static final String CONVERSION_FILE = "conversions.csv";
    private static final String DETAILED_FOLDER = "detailed";

    public static void main(String[] args) throws IOException {
        String jsonFiles = args[0];
        String resultsFolder = args[1];

        if (!new File(resultsFolder).mkdir()) {
            throw new RuntimeException("Folder " + resultsFolder + " already exists.");
        }

        extractPatterns(jsonFiles, resultsFolder + "/" + PATTERN_FILE);
        createStatistics(resultsFolder + "/" + SUMMARY_FILE, resultsFolder + "/" + CONVERSION_FILE);
        writePatternsToFiles(resultsFolder + "/" + DETAILED_FOLDER);
    }

    private static void extractPatterns(String jsonFiles, String patternFile) throws IOException {
        PatternExtractor pe = new PatternExtractor(jsonFiles);
        System.out.print("Create output");
        countOfFiles = pe.getCountOfFiles();
        countOfFilesWithPatterns = pe.getCountOfFilesWithPatterns();
        patterns = pe.getSortedPatterns();
        countPatternsTotal = pe.getCountPatternsTotal();
        countPatternPropertiesTotal = pe.getCountPatternPropertiesTotal();
        countPatternsUnique = pe.getCountPatternsUnique();
        countPatternPropertiesUnique = pe.getCountPatternPropertiesUnique();
        Map<String, Set<String>> patterns = pe.getPatternsWithFilename();
        List<String> lines = new LinkedList<>();

        for (Map.Entry<String, Set<String>> entry : patterns.entrySet()) {
            StringBuilder builder = new StringBuilder();
            builder.append("\"").append(StringEscapeUtils.escapeJson(entry.getKey())).append("\"");

            for (String file : entry.getValue()) {
                builder.append(";").append(file);
            }

            lines.add(builder.toString());
        }

        Collections.sort(lines);
        Path file = Paths.get(patternFile);
        Files.write(file, lines);
    }

    private static void createStatistics(String summaryFile, String conversionFile) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(conversionFile))) {
            writer.write("ECMAScript;brics\n");

            for (String pattern : patterns) {
                ExpressionTree tree;
                String bricsRegex;

                try {
                    tree = Builder.buildExpressionTree(pattern);
                    bricsRegex = EcmaToBricsExpressionConverter.convert(pattern);
                    writer.write(String.format("\"%s\";\"%s\"%n", StringEscapeUtils.escapeJson(pattern), StringEscapeUtils.escapeJava(bricsRegex)));
                } catch (ECMAScriptNotSupportedException e) {
                    notSupportedPatterns.add(pattern);
                    continue;
                } catch (ECMAScriptSyntaxException e) {
                    invalidPatterns.add(pattern);
                    continue;
                } catch (RuntimeException e) {
                    notConvertablePatterns.add(pattern);
                    continue;
                }

                ExpressionTree.Node root = tree.getRoot();

                if (root.isNullable()) {
                    nullablePatterns.add(pattern);
                }

                if (root.containsHat() || root.containsDollar()) {
                    anchoredPatterns.add(pattern);

                    if (root.containsHatAfterBegin() || root.containsDollarBeforeEnd()) {
                        anchoredPatternsInside.add(pattern);
                    }

                    continue;
                }

                unanchoredPatterns.add(pattern);
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(summaryFile))) {
            writer.write(String.format("Files total;%d%n", countOfFiles));
            writer.write(String.format("Files with patterns;%d%n", countOfFilesWithPatterns));
            writer.write(String.format("\"pattern\" count total;%d%n", countPatternsTotal));
            writer.write(String.format("\"patternProperties\" count total;%d%n", countPatternPropertiesTotal));
            writer.write(String.format("\"pattern\" count unique;%d%n", countPatternsUnique));
            writer.write(String.format("\"patternProperties\" count unique;%d%n", countPatternPropertiesUnique));
            writer.write(String.format("Unique patterns;%d%n", patterns.size()));
            writer.write(String.format("Invalid patterns;%d%n", invalidPatterns.size()));
            writer.write(String.format("Not supported patterns;%d%n", notSupportedPatterns.size()));
            writer.write(String.format("Unexpectedly not convertable patterns;%d%n", notConvertablePatterns.size()));
            writer.write(String.format("Anchored patterns;%d%n", anchoredPatterns.size()));
            writer.write(String.format("Inside anchored patterns;%d%n", anchoredPatternsInside.size()));
            writer.write(String.format("Non-anchored patterns;%d%n", unanchoredPatterns.size()));
            writer.write(String.format("Nullable patterns;%d%n", nullablePatterns.size()));
        }
    }

    private static void writePatternsToFiles(String folder) throws IOException {
        if (!new File(folder).mkdir()) {
            throw new RuntimeException("Cannot create folder " + folder + ".");
        }

        writeEscapedPatternsToFile(folder + "/invalidPatterns.csv", invalidPatterns);
        writeEscapedPatternsToFile(folder + "/notSupportedPatterns.csv", notSupportedPatterns);
        writeEscapedPatternsToFile(folder + "/notConvertablePatterns.csv", notConvertablePatterns);
        writeEscapedPatternsToFile(folder + "/anchoredPatterns.csv", anchoredPatterns);
        writeEscapedPatternsToFile(folder + "/anchoredInsidePatterns.csv", anchoredPatternsInside);
        writeEscapedPatternsToFile(folder + "/unanchoredPatterns.csv", unanchoredPatterns);
        writeEscapedPatternsToFile(folder + "/nullablePatterns.csv", nullablePatterns);
        System.out.println(", done.");
    }

    private static void writeEscapedPatternsToFile(String file, List<String> patterns) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (String pattern : patterns) {
                writer.write(String.format("\"%s\"%n", StringEscapeUtils.escapeJson(pattern)));
            }
        }
    }

}
