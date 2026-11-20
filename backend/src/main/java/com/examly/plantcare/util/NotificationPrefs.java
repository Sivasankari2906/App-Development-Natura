package com.examly.plantcare.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

/**
 * Reads the notification preferences stored on a user. Some databases return a JSON column
 * as a quoted JSON string (for example H2), so a textual result is parsed a second time.
 */
public final class NotificationPrefs {

    private NotificationPrefs() {
    }

    public static JsonNode parse(ObjectMapper mapper, String stored) {
        if (stored == null || stored.isBlank()) {
            return JsonNodeFactory.instance.objectNode();
        }
        try {
            JsonNode node = mapper.readTree(stored);
            if (node.isTextual()) {
                node = mapper.readTree(node.asText());
            }
            return node.isObject() ? node : JsonNodeFactory.instance.objectNode();
        } catch (JsonProcessingException e) {
            return JsonNodeFactory.instance.objectNode();
        }
    }
}
