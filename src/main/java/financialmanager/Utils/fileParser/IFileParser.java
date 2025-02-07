package financialmanager.Utils.fileParser;

import java.util.List;
import java.util.logging.Logger;

public interface IFileParser {
    String getFileName();
    String[] getNextLineOfData();
    List<String[]> readAllLines();
}
