package financialmanager.Utils.fileParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CsvFileParser extends FileParser {

    private static final Logger log = LoggerFactory.getLogger(CsvFileParser.class);

    public CsvFileParser(BufferedReader bufferedReader, String fileName) {
        super(bufferedReader, fileName);
    }

    public List<String[]> readAllLines() {
        List<String[]> lines = new ArrayList<>();
        String[] line;
        int lineNumber = 0;
        int errorCount = 0;

        // Read lines and handle potential parsing errors
        while ((line = getNextLineOfData()) != null) {
            if (line.length == 0) {
                errorCount++;
                log.error("Error in reading line {}", lineNumber);
                continue;
            }

            lineNumber++;
            lines.add(line);
        }

        log.info("Total {} lines read", lineNumber);
        log.info("Successfully read {} lines without errors", lineNumber - errorCount);
        log.info("Encountered errors in {} lines", errorCount);

        return lines;
    }

    public String[] getNextLineOfData() {
        try {
            String line = bufferedReader.readLine();
            if (line == null) {
                return null;
            }

            String[] split = line.split(",");
            if (split.length == 1) {
                split = line.split(";");
            }

            return split;
        } catch (IOException e) {
            log.error("Error in reading data", e);
            return new String[0];
        }
    }
}