package financialmanager.Utils.fileParser;

import financialmanager.objectFolder.bankAccountFolder.BankAccountService;
import financialmanager.objectFolder.contractFolder.ContractService;
import financialmanager.objectFolder.counterPartyFolder.CounterPartyService;
import financialmanager.objectFolder.responseFolder.ResponseService;
import financialmanager.objectFolder.transactionFolder.Transaction;
import financialmanager.objectFolder.transactionFolder.TransactionService;
import financialmanager.objectFolder.usersFolder.UsersService;
import lombok.experimental.SuperBuilder;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CsvFileParser extends FileParser {

    public CsvFileParser(BankAccountService bankAccountService, UsersService usersService, CounterPartyService counterPartyService, TransactionService transactionService, ResponseService responseService, ContractService contractService, BufferedReader bufferedReader) {
        super(bankAccountService, usersService, counterPartyService, transactionService, responseService, contractService, bufferedReader);
    }

    public List<String[]> readAllLines() {
        List<String[]> lines = new ArrayList<>();
        String[] line;

        // Read lines and handle potential parsing errors
        while ((line = getNextLineOfData()) != null) {
            try {
                lines.add(line);
            } catch (Exception e) {
            }
        }

        return lines;
    }

    public String[] getNextLineOfData() {
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