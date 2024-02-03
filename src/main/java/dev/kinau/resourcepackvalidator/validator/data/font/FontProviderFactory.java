package dev.kinau.resourcepackvalidator.validator.data.font;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FontProviderFactory {

    public FontProvider create(JsonObject object) {
        if (object == null) return null;
        if (!object.has("type") || !object.get("type").isJsonPrimitive()) return null;
        String type = object.get("type").getAsString();
        switch (type) {
            case "bitmap": {
                String file = Optional.ofNullable(object.get("file")).map(JsonElement::getAsString).orElse(null);
                Integer height = Optional.ofNullable(object.get("height")).map(JsonElement::getAsInt).orElse(null);
                Integer ascent = Optional.ofNullable(object.get("ascent")).map(JsonElement::getAsInt).orElse(null);
                List<String> chars = Optional.ofNullable(object.get("chars"))
                        .map(JsonElement::getAsJsonArray)
                        .map(jsonElements -> jsonElements.asList().stream().map(JsonElement::getAsString).toList())
                        .orElse(null);
                return new BitmapFontProvider(file, height, ascent, chars);
            }
            case "space": {
                Map<String, Float> advancesMap = null;
                JsonObject advances = object.getAsJsonObject("advances");
                if (advances != null) {
                    advancesMap = new HashMap<>();
                    for (String chars : advances.keySet()) {
                        advancesMap.put(chars, advances.get(chars).getAsFloat());
                    }
                }
                return new SpaceFontProvider(advancesMap);
            }
            default: return null;
        }
    }
}
