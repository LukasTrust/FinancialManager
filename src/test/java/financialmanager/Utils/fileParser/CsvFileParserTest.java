package financialmanager.Utils.fileParser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
class CsvFileParserTest {

    private CsvFileParser csvFileParser;
    private BufferedReader bufferedReader;

    @BeforeEach
    void setup() {
        bufferedReader = mock(BufferedReader.class);
        csvFileParser = new CsvFileParser(bufferedReader, "testFile");
    }

    @Test
    void readAllLines_zeroLines() throws IOException {
        when(bufferedReader.readLine()).thenReturn(null);

        List<String[]> lines = csvFileParser.readAllLines();

        csvFileParser.readAllLines();

        assertThat(lines.size()).isEqualTo(0);
    }

    @Test
    void readAllLines_oneLine() throws IOException {
        when(bufferedReader.readLine()).thenReturn("first;second;third;fourth").thenReturn(null);

        List<String[]> lines = csvFileParser.readAllLines();
        assertThat(lines.size()).isEqualTo(1);
    }

    @Test
    void readAllLines_multipleLines() throws IOException {
        when(bufferedReader.readLine()).thenReturn("first;second;third;fourth")
                .thenReturn("a;b;c;d")
                .thenReturn("one;two;three;four")
                .thenReturn(null);

        List<String[]> lines = csvFileParser.readAllLines();

        assertThat(lines.size()).isEqualTo(3);
    }

    @Test
    void readAllLines_malformedLine() throws IOException {
        when(bufferedReader.readLine()).thenReturn("first;second;third")
                .thenReturn("malformed line")
                .thenReturn("valid;line")
                .thenReturn(null);

        List<String[]> lines = csvFileParser.readAllLines();

        assertThat(lines.size()).isEqualTo(3);
        assertThat(lines.get(0)[0]).isEqualTo("first");
        assertThat(lines.get(1)[0]).isEqualTo("malformed line");
        assertThat(lines.get(2)[0]).isEqualTo("valid");
    }

    @Test
    void readAllLines_fileWithErrors() throws IOException {
        when(bufferedReader.readLine()).thenReturn("first;second;third")
                .thenThrow(new IOException("Error reading file"))
                .thenReturn("valid;line")
                .thenReturn(null);

        List<String[]> lines = csvFileParser.readAllLines();

        assertThat(lines.size()).isEqualTo(2);
        assertThat(lines.get(0)[0]).isEqualTo("first");
        assertThat(lines.get(1)[0]).isEqualTo("valid");
    }

    @Test
    void readAllLines_emptyLine() throws IOException {
        when(bufferedReader.readLine()).thenReturn("").thenReturn(null);

        List<String[]> lines = csvFileParser.readAllLines();

        assertThat(lines.size()).isEqualTo(1);
        assertThat(lines.getFirst().length).isEqualTo(1);
    }

    @Test
    void readAllLines_specialCharacters() throws IOException {
        when(bufferedReader.readLine()).thenReturn("first;seco!@#$;third&*()")
                .thenReturn("line;with;special$%characters")
                .thenReturn(null);

        List<String[]> lines = csvFileParser.readAllLines();

        assertThat(lines.size()).isEqualTo(2);
        assertThat(lines.get(0)[1]).isEqualTo("seco!@#$");
        assertThat(lines.get(1)[2]).isEqualTo("special$%characters");
    }


    @Test
    void getNextLineOfData_emptyLine() throws IOException {
        when(bufferedReader.readLine()).thenReturn(null);

        String[] line = csvFileParser.getNextLineOfData();

        assertThat(line).isEqualTo(null);
    }

    @Test
    void getNextLineOfData_notEmptyLine() throws IOException {
        when(bufferedReader.readLine()).thenReturn("first;second;third;fourth").thenReturn(null);

        String[] line = csvFileParser.getNextLineOfData();

        assertThat(line[0]).isEqualTo("first");
        assertThat(line[1]).isEqualTo("second");
        assertThat(line[2]).isEqualTo("third");
        assertThat(line[3]).isEqualTo("fourth");
    }

    @Test
    void getNextLineOfData_errorReadingFile() throws IOException {
        when(bufferedReader.readLine()).thenThrow(new IOException("Error reading file"));

        String[] line = csvFileParser.getNextLineOfData();

        assertThat(line.length).isEqualTo(0);
    }
}