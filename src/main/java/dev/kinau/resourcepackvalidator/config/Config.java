package dev.kinau.resourcepackvalidator.config;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;

@Accessors(fluent = true)
@Getter
public class Config {
    private Map<String, JsonObject> validators = new HashMap<>();
}
