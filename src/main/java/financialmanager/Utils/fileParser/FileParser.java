package financialmanager.Utils.fileParser;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.BufferedReader;

@AllArgsConstructor
public abstract class FileParser implements IFileParser {
    protected final BufferedReader bufferedReader;
    @Getter
    protected final String fileName;
}