package financialmanager.Utils.fileParser;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
class FileParserFactoryTest {

    @Autowired
    private FileParserFactory fileParserFactory;

    @Test
    void getFileParser_withNull() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> fileParserFactory.getFileParser(null));

        assertEquals("File is null", thrown.getMessage());
    }

    @Test
    void getFileParser_withNullContentType() {
        MultipartFile multipartFile = mock(MultipartFile.class);

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> fileParserFactory.getFileParser(multipartFile));

        assertEquals("File content type is null", thrown.getMessage());
    }

    @Test
    void getFileParser_withNoInputStream() throws IOException {
        MultipartFile multipartFile = mock(MultipartFile.class);

        when(multipartFile.getContentType()).thenReturn("image/jpeg");

        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> fileParserFactory.getFileParser(multipartFile));

        assertEquals("Error initializing file parser", thrown.getMessage());
    }

    @Test
    void getFileParser_withContentTypeNotSupported() throws IOException {
        MultipartFile multipartFile = mock(MultipartFile.class);
        InputStream inputStream = mock(InputStream.class);

        when(multipartFile.getContentType()).thenReturn("image/jpeg");
        when(multipartFile.getInputStream()).thenReturn(inputStream);

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> fileParserFactory.getFileParser(multipartFile));

        assertEquals("Unsupported file type: image/jpeg", thrown.getMessage());
    }

    @Test
    void getFileParser_withContentTypeTextCsv() throws IOException {
        MultipartFile multipartFile = mock(MultipartFile.class);
        InputStream inputStream = mock(InputStream.class);

        when(multipartFile.getContentType()).thenReturn("text/csv");
        when(multipartFile.getInputStream()).thenReturn(inputStream);

        IFileParser fileParser = fileParserFactory.getFileParser(multipartFile);
        assertNotNull(fileParser);
        assertInstanceOf(CsvFileParser.class, fileParser);
    }

    @Test
    void getFileParser_withContentTypeMsExcel() throws IOException {
        MultipartFile multipartFile = mock(MultipartFile.class);
        InputStream inputStream = mock(InputStream.class);

        when(multipartFile.getContentType()).thenReturn("application/vnd.ms-excel");
        when(multipartFile.getInputStream()).thenReturn(inputStream);

        IFileParser fileParser = fileParserFactory.getFileParser(multipartFile);
        assertNotNull(fileParser);
        assertInstanceOf(CsvFileParser.class, fileParser);
    }
}