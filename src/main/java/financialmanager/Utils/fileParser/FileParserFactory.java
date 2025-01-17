package financialmanager.Utils.fileParser;

import financialmanager.objectFolder.bankAccountFolder.BankAccountService;
import financialmanager.objectFolder.contractFolder.ContractService;
import financialmanager.objectFolder.counterPartyFolder.CounterPartyService;
import financialmanager.objectFolder.responseFolder.ResponseService;
import financialmanager.objectFolder.transactionFolder.TransactionService;
import financialmanager.objectFolder.usersFolder.UsersService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Component
@AllArgsConstructor
public class FileParserFactory {

    private final BankAccountService bankAccountService;
    private final UsersService usersService;
    private final CounterPartyService counterPartyService;
    private final TransactionService transactionService;
    private final ResponseService responseService;
    private final ContractService contractService;

    public IFileParser getFileParser(MultipartFile file) {
        String contentType = file.getContentType();

        if (contentType == null) {
            throw new IllegalArgumentException("File type is not supported");
        }

        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(file.getInputStream()));

            switch (contentType) {
                case "application/vnd.ms-excel":
                case "text/csv":
                    return new CsvFileParser(bankAccountService, usersService, counterPartyService,
                            transactionService, responseService, contractService, bufferedReader);
                default:
                    throw new IllegalArgumentException("Unsupported file type: " + contentType);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error initializing file parser", e);
        }
    }
}
