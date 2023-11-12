package com.example.iso8583.iso8583;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.NotNull;
import org.springframework.core.io.ClassPathResource;


public final class ISO8583ActionCode {
  private ISO8583ActionCode() {}

  public static Map<String, String> get(@NotNull String key) {
    List<Map<String, String>> actionCodes = getAll();
    for (Map<String, String> item : actionCodes) {
      if (Objects.equals(item.get("code"), key)) {
        return item;
      }
    }
    return Collections.emptyMap();
  }

  public static List<Map<String, String>> getAll() {
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      ClassPathResource resource = new ClassPathResource("iso8583ActionCodes.json");
      return objectMapper.readValue(resource.getInputStream(), List.class);
    } catch (IOException e) {
      return Collections.emptyList();
    }
  }
}
