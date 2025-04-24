package cn.wavelet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TextProcessor {
    public static List<String> processText(String filePath) throws IOException {
        String content = Files.readString(Paths.get(filePath));
        content = content.replaceAll("[^a-zA-Z\\s]", " ")
                .toLowerCase()
                .replaceAll("\\s+", " ")
                .trim();
        if (content.isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.asList(content.split(" "));
    }
}