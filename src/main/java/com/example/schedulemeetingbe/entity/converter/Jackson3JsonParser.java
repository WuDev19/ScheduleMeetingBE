package com.example.schedulemeetingbe.entity.converter;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

public class Jackson3JsonParser {
    private static final JsonMapper mapper = JsonMapper.builder().build();

    public static JsonNode parse(String rawJson) {
        if (rawJson == null || rawJson.isEmpty()) return null;
        try {
            return mapper.readTree(rawJson);
        } catch (Exception e) {
            return null;
        }
    }

}
