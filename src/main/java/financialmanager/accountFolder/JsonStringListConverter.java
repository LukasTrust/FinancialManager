package financialmanager.accountFolder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.io.IOException;
import java.util.List;

@Converter
public class JsonStringListConverter implements AttributeConverter<List<String>, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        try {
            return attribute == null ? null : objectMapper.writeValueAsString(attribute);
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert List<String> to JSON string", e);
        }
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        try {
            return dbData == null ? null : objectMapper.readValue(dbData, new TypeReference<List<String>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert JSON string to List<String>", e);
        }
    }
}