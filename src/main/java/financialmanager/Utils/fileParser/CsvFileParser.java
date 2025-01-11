package financialmanager.Utils.fileParser;

import financialmanager.objectFolder.bankAccountFolder.BankAccountService;
import financialmanager.objectFolder.usersFolder.UsersService;
import lombok.experimental.SuperBuilder;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;

public class CsvFileParser extends FileParser {

    public CsvFileParser(BankAccountService bankAccountService, UsersService usersService, BufferedReader bufferedReader) {
        super(bankAccountService, usersService, bufferedReader);
    }

    public String[] getNextLineOfData(MultipartFile file) {
        try {
            String line = bufferedReader.readLine();
            if (line != null) {
                // Split by commas and trim spaces
                return line.split(";");
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading CSV file", e);
        }
        return null;
    }
}