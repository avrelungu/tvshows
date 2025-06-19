package com.example.user_service.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.IOException;

@Converter(autoApply = true)
public class JsonNodeConverter implements AttributeConverter<JsonNode,String> {
    public static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(JsonNode jsonNode) {
        try {
            return mapper.writeValueAsString(jsonNode);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not convert JSON to String", e);
        }
    }

    @Override
    public JsonNode convertToEntityAttribute(String s) {
        try {
            return mapper.readTree(s);
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not deserialize JsonNode", e);
        }
    }
}
