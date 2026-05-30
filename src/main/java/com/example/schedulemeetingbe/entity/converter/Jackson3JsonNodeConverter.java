package com.example.schedulemeetingbe.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Converter
public class Jackson3JsonNodeConverter implements AttributeConverter<JsonNode, String> {

    private static final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Override
    public String convertToDatabaseColumn(JsonNode jsonNode) {
        if (jsonNode == null) return null;
        return jsonNode.toString();
    }

    @Override
    public JsonNode convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) return null;
        try {
            return jsonMapper.readTree(dbData);
        } catch (Exception e) {
            return null;
        }
    }
}
