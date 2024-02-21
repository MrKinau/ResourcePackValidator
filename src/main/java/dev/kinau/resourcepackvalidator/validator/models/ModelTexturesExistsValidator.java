package dev.kinau.resourcepackvalidator.validator.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.kinau.resourcepackvalidator.ValidationJob;
import dev.kinau.resourcepackvalidator.report.TestSuite;
import dev.kinau.resourcepackvalidator.utils.FileUtils;
import dev.kinau.resourcepackvalidator.validator.ValidationResult;
import dev.kinau.resourcepackvalidator.validator.context.FileContext;
import dev.kinau.resourcepackvalidator.validator.generic.FileContextValidator;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ModelTexturesExistsValidator extends FileContextValidator<JsonObject, Map<String, String>> {

    public ModelTexturesExistsValidator(Map<String, JsonObject> config, TestSuite testSuite) {
        super(config, testSuite);
    }

    @Override
    protected ValidationResult<Map<String, String>> isValid(ValidationJob job, FileContext context, JsonObject modelObj) {
        Map<String, String> textureData = new HashMap<>();

        JsonObject textures = modelObj.getAsJsonObject("textures"); // validated previously

        // Check if "textures" field has correct entries String<->String Map
        boolean failed = false;
        for (String key : textures.keySet()) {
            JsonElement value = textures.get(key);
            if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
                failed = true;
                failedError("Model has invalid texture registered (texture value is not string) for variable {} at {}", key, context.value().getPath());
                continue;
            }
            textureData.put(key, value.getAsString());
        }
        if (failed)
            return failedError();

        // Check if set texture (from "textures") exists in resourcepack. Also checking if it is set to a
        // reference textures (prefixed with #).
        // Fallback 1: Check if it is present in another overlay
        // Fallback 2: check if it is a minecraft vanilla texture
        for (Map.Entry<String, String> kv : textureData.entrySet()) {
            if (!validateTexture(kv.getKey(), kv.getValue(), textureData, context, job))
                failed = true;
        }
        if (failed)
            return failedError();

        // Check if a texture assigned onto an element is defined in the "textures" field
        if (modelObj.has("elements") && modelObj.get("elements").isJsonArray()) {
            JsonArray elements = modelObj.getAsJsonArray("elements");
            for (JsonElement element : elements) {
                if (!element.isJsonObject()) continue;
                JsonObject elementObj = element.getAsJsonObject();
                if (!elementObj.has("faces") || !elementObj.get("faces").isJsonObject()) continue;
                JsonObject facesObj = elementObj.getAsJsonObject("faces");
                for (String key : facesObj.keySet()) {
                    if (!facesObj.get(key).isJsonObject()) continue;
                    JsonObject faceObj = facesObj.getAsJsonObject(key);
                    if (!faceObj.has("texture") || !faceObj.get("texture").isJsonPrimitive()
                            || !faceObj.getAsJsonPrimitive("texture").isString()) continue;
                    String texture = faceObj.getAsJsonPrimitive("texture").getAsString();
                    if (!validateTexture(key, texture, textureData, context, job))
                        failed = true;
                }
            }
        }
        if (failed)
            return failedError();

        return success(textureData);
    }

    private boolean validateTexture(String key, String texture, Map<String, String> textureData, FileContext context, ValidationJob job) {
        if (texture.startsWith("#")) {
            String referenceKey = texture.substring(1);
            if (textureData.containsKey(referenceKey)) return true;
        }
        if (!FileUtils.textureExists(context.namespace(), texture, job.assetDictionary())) {
            failedError("Model has linked texture that is not present ({}) at {}", key + " » " + texture, context.value().getPath());
            return false;
        }
        return true;
    }
}
