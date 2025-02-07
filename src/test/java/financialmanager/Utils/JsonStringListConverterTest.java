package financialmanager.Utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
public class JsonStringListConverterTest {

    @Autowired
    private JsonStringListConverter jsonStringListConverter;

    @Test
    void convertToDatabaseColumn_withValidList() {
        List<String> input = Arrays.asList("apple", "banana", "cherry");
        String result = jsonStringListConverter.convertToDatabaseColumn(input);
        assertThat(result).isEqualTo("[\"apple\",\"banana\",\"cherry\"]");
    }

    @Test
    void convertToDatabaseColumn_withEmptyList() {
        List<String> input = Collections.emptyList();
        String result = jsonStringListConverter.convertToDatabaseColumn(input);
        assertThat(result).isEqualTo("[]");
    }

    @Test
    void convertToDatabaseColumn_withNull() {
        String result = jsonStringListConverter.convertToDatabaseColumn(null);
        assertThat(result).isEqualTo("[]");
    }

    @Test
    void convertToEntityAttribute_withValidJson() {
        String json = "[\"dog\",\"cat\",\"mouse\"]";
        List<String> result = jsonStringListConverter.convertToEntityAttribute(json);
        assertThat(result).isEqualTo(Arrays.asList("dog", "cat", "mouse"));
    }

    @Test
    void convertToDatabaseColumn_ShouldThrowRuntimeException_WhenObjectMapperFails() throws IOException {
        // Arrange
        ObjectMapper mockObjectMapper = mock(ObjectMapper.class);
        JsonStringListConverter converter = new JsonStringListConverter(mockObjectMapper);

        List<String> testList = List.of("test");

        // Simulate ObjectMapper throwing an IOException
        when(mockObjectMapper.writeValueAsString(testList)).thenThrow(new JsonProcessingException("Mock Exception") {});

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            converter.convertToDatabaseColumn(testList);
        });

        assertTrue(exception.getMessage().contains("Failed to convert List<String> to JSON string"));
        assertNotNull(exception.getCause());
        assertInstanceOf(JsonProcessingException.class, exception.getCause());
    }

    @Test
    void convertToEntityAttribute_withEmptyJson() {
        String json = "[]";
        List<String> result = jsonStringListConverter.convertToEntityAttribute(json);
        assertThat(result).isEqualTo(Collections.emptyList());
    }

    @Test
    void convertToEntityAttribute_withNull() {
        List<String> result = jsonStringListConverter.convertToEntityAttribute(null);
        assertThat(result).isEqualTo(Collections.emptyList());
    }

    @Test
    void convertToEntityAttribute_withInvalidJson() {
        String invalidJson = "not a json";
        assertThatThrownBy(() -> jsonStringListConverter.convertToEntityAttribute(invalidJson))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to convert JSON string to List<String>");
    }
}
