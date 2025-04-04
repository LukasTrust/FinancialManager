package financialmanager.objectFolder.transactionFolder;

import financialmanager.Utils.fileParser.FileParserFactory;
import financialmanager.objectFolder.contractFolder.BaseContractService;
import financialmanager.objectFolder.contractFolder.ContractAssociationService;
import financialmanager.objectFolder.contractFolder.contractHistoryFolder.BaseContractHistoryService;
import financialmanager.objectFolder.counterPartyFolder.CounterPartyService;
import financialmanager.objectFolder.localeFolder.LocaleService;
import financialmanager.objectFolder.responseFolder.ResponseService;
import financialmanager.objectFolder.resultFolder.ResultService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class TransactionUploadServiceTest {

    private TransactionUploadService transactionUploadService;

    private BaseTransactionService baseTransactionService;
    private BaseContractService baseContractService;
    private BaseContractHistoryService baseContractHistoryService;

    private ContractAssociationService contractAssociationService;
    private CounterPartyService counterPartyService;
    private FileParserFactory fileParserFactory;
    private ResponseService responseService;
    private ResultService resultService;
    private LocaleService localeService;

    @BeforeEach
    void setUp() {
        baseTransactionService = mock(BaseTransactionService.class);
        baseContractService = mock(BaseContractService.class);
        baseContractHistoryService = mock(BaseContractHistoryService.class);

        contractAssociationService = mock(ContractAssociationService.class);
        counterPartyService = mock(CounterPartyService.class);
        fileParserFactory = mock(FileParserFactory.class);
        responseService = mock(ResponseService.class);
        resultService = mock(ResultService.class);
        localeService = mock(LocaleService.class);

        transactionUploadService = new TransactionUploadService(baseTransactionService, baseContractService, baseContractHistoryService,
                contractAssociationService, counterPartyService, fileParserFactory, responseService, resultService, localeService);
    }

    @Test
    void uploadDataForTransactions_noId_noFiles() {
        Long id = null;
        MultipartFile[] files = null;

        ResponseEntity<?> response = transactionUploadService.uploadDataForTransactions(id, files);

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertEquals(new ArrayList<>(), response.getBody());
    }

    @Test
    void uploadDataForTransactions_noId_emptyFiles() {
        Long id = null;
        MultipartFile[] files = new MultipartFile[0];

        ResponseEntity<?> response = transactionUploadService.uploadDataForTransactions(id, files);

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertEquals(new ArrayList<>(), response.getBody());
    }
}