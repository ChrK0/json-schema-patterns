import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PatternExtractor {

    private final List<Path> files;
    private int countOfFilesWithPatterns;
    private final Map<String, Set<String>> patternsWithFilename;
    private final List<String> sortedPatterns;
    private int countPatternsTotal;
    private int countPatternPropertiesTotal;
    private int countPatternsUnique;
    private int countPatternPropertiesUnique;

    public PatternExtractor(final String directory) throws IOException {
        try (Stream<Path> fileStream = Files.walk(Paths.get(directory))) {
            files = fileStream.filter(Files::isRegularFile).collect(Collectors.toList());
        }

        patternsWithFilename = extractPatternsWithFilename();
        sortedPatterns = new LinkedList<>(patternsWithFilename.keySet());
        Collections.sort(sortedPatterns);
    }

    public int getCountOfFiles() {
        return files.size();
    }

    public int getCountOfFilesWithPatterns() {
        return countOfFilesWithPatterns;
    }

    public Map<String, Set<String>> getPatternsWithFilename() {
        return patternsWithFilename;
    }

    public List<String> getSortedPatterns() {
        return sortedPatterns;
    }

    public int getCountPatternsTotal() {
        return countPatternsTotal;
    }

    public int getCountPatternPropertiesTotal() {
        return countPatternPropertiesTotal;
    }

    public int getCountPatternsUnique() {
        return countPatternsUnique;
    }

    public int getCountPatternPropertiesUnique() {
        return countPatternPropertiesUnique;
    }

    public Map<String, Set<String>> extractPatternsWithFilename() throws IOException {
        Map<String, Set<String>> patternMap = new HashMap<>();
        Set<String> patternProperties = new HashSet<>();
        int count = 1;
        int total = files.size();

        for (Path file : files) {
            System.out.printf("\rExtracting patterns from files: %d%% (%d/%d)", count * 100 / total, count++, total);
            String content = new String(Files.readAllBytes(file));

            switch (content.charAt(0)) {
                case '{':
                    if (extractFromJsonObjectWithFilename(patternMap, patternProperties, new JSONObject(content), file.getFileName().toString())) {
                        countOfFilesWithPatterns++;
                    }
                    break;
                case '[':
                    if (extractFromJsonArrayWithFilename(patternMap, patternProperties, new JSONArray(content), file.getFileName().toString())) {
                        countOfFilesWithPatterns++;
                    }
                    break;
                default:
                    throw new JSONException(".json file must begin with '{' or '['");
            }
        }

        System.out.println(", done.");
        return new TreeMap<>(patternMap);
    }

    private boolean extractFromJsonObjectWithFilename(Map<String, Set<String>> patternMap, Set<String> patternProperties, JSONObject obj, String filename) {
        boolean hasPattern = false;

        if (obj.has("pattern") && obj.get("pattern") instanceof String) {
            countPatternsTotal++;
            hasPattern = true;
            String key = obj.getString("pattern");

            if (!patternMap.containsKey(key)) {
                countPatternsUnique++;
                patternMap.put(key, new HashSet<>());
            }

            patternMap.get(key).add(filename);
        }

        if (obj.has("patternProperties")) {
            countPatternPropertiesTotal++;
            hasPattern = true;
            Set<String> patterns = obj.getJSONObject("patternProperties").keySet();

            for (String pattern : patterns) {

                if (!patternMap.containsKey(pattern)) {
                    patternMap.put(pattern, new HashSet<>());
                }

                if (patternProperties.add(pattern)) {
                    countPatternPropertiesUnique++;
                }

                patternMap.get(pattern).add(filename);
            }
        }

        Iterator<String> keys = obj.keys();

        while (keys.hasNext()) {
            String key = keys.next();
            Object value = obj.get(key);

            if (value instanceof JSONObject) {
                if (extractFromJsonObjectWithFilename(patternMap, patternProperties, (JSONObject) value, filename)) {
                    hasPattern = true;
                }
            } else if (value instanceof JSONArray) {
                if (extractFromJsonArrayWithFilename(patternMap, patternProperties, (JSONArray) value, filename)) {
                    hasPattern = true;
                }
            }
        }

        return hasPattern;
    }

    private boolean extractFromJsonArrayWithFilename(Map<String, Set<String>> patternMap, Set<String> patternProperties, JSONArray arr, String filename) {
        boolean hasPattern = false;

        for (Object obj : arr) {
            if (obj instanceof JSONObject) {
                if (extractFromJsonObjectWithFilename(patternMap, patternProperties, (JSONObject) obj, filename)) {
                    hasPattern = true;
                }
            }
        }

        return hasPattern;
    }

}
