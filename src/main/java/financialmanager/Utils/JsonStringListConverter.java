package financialmanager.Utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import financialmanager.Utils.fileParser.FileParserFactory;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@Converter
public class JsonStringListConverter implements AttributeConverter<List<String>, String> {

    private final ObjectMapper objectMapper;
    private static final Logger log = LoggerFactory.getLogger(JsonStringListConverter.class);

    // Constructor injection for better testability
    public JsonStringListConverter() {
        this(new ObjectMapper());
    }

    // Allow dependency injection for testing
    public JsonStringListConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        try {
            // Return empty array if null
            return attribute == null ? "[]" : objectMapper.writeValueAsString(attribute);
        } catch (IOException e) {
            log.error("Failed to convert List<String> to JSON string", e);
            throw new RuntimeException("Failed to convert List<String> to JSON string", e);
        }
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        try {
            return dbData == null || dbData.isEmpty() ? new ArrayList<>() : objectMapper.readValue(dbData, new TypeReference<List<String>>() {});
        } catch (IOException e) {
            log.error("Failed to convert JSON string to List<String>", e);
            throw new RuntimeException("Failed to convert JSON string to List<String>", e);
        }
    }
}