package financialmanager.Utils.fileParser;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DataColumnsTest {

    DataColumns dataColumns;

    @Test
    void testCheckIfAllAreFound_FirstColumnZero() {
        dataColumns = new DataColumns(0, 2, 3, 4);
        assertFalse(dataColumns.checkIfAllAreFound(), "checkIfAllAreFound should return false when one column is zero.");
    }

    @Test
    void testCheckIfAllAreFound_SecondColumnZero() {
        dataColumns = new DataColumns(2, 0, 3, 4);
        assertFalse(dataColumns.checkIfAllAreFound(), "checkIfAllAreFound should return false when one column is zero.");
    }

    @Test
    void testCheckIfAllAreFound_ThirdColumnZero() {
        dataColumns = new DataColumns(2, 3, 0, 4);
        assertFalse(dataColumns.checkIfAllAreFound(), "checkIfAllAreFound should return false when one column is zero.");
    }

    @Test
    void testCheckIfAllAreFound_FourthColumnZero() {
        dataColumns = new DataColumns(2, 3, 4, 0);
        assertFalse(dataColumns.checkIfAllAreFound(), "checkIfAllAreFound should return false when one column is zero.");
    }

    @Test
    void testCheckIfAllAreFound_AllColumnsZero() {
        dataColumns = new DataColumns(0, 0, 0, 0);
        assertFalse(dataColumns.checkIfAllAreFound(), "checkIfAllAreFound should return false when all columns are zero.");
    }

    @Test
    void testCheckIfAllAreFound_AllColumnsNonZero() {
        dataColumns = new DataColumns(5, 10, 15, 20);
        assertTrue(dataColumns.checkIfAllAreFound(), "checkIfAllAreFound should return true when all columns are non-zero.");
    }
}