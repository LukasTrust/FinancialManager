package financialmanager.Utils.fileParser;

import java.util.List;

public interface IFileParser {
    String getFileName();
    String[] getNextLineOfData();
    List<String[]> readAllLines();
}
