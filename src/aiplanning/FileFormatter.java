package aiplanning;

import java.io.*;
import java.nio.file.Files;
import java.util.List;

public class FileFormatter {
    public static File formatFile(File inputFile) {
        int maxLength = getMaxLineLength(inputFile);
        try {
            List<String> lines = Files.readAllLines(inputFile.toPath());
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).length() < maxLength) {
                    lines.set(i, lines.get(i) + "#".repeat(maxLength - lines.get(i).length()));
                }
            }
            Files.write(inputFile.toPath(), lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return inputFile;
    }

    private static int getMaxLineLength(File inputFile) {

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFile))) {
            int maxLength = 0;
            String currentLine;
            while ((currentLine = bufferedReader.readLine()) != null) {
                if (currentLine.length() > maxLength) {
                    maxLength = currentLine.length();
                }
            }
            return maxLength;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
