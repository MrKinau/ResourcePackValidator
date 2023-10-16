package dev.kinau.resourcepackvalidator.validator.font;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.kinau.resourcepackvalidator.ValidationJob;
import dev.kinau.resourcepackvalidator.report.TestSuite;
import dev.kinau.resourcepackvalidator.utils.FileUtils;
import dev.kinau.resourcepackvalidator.validator.ValidationResult;
import dev.kinau.resourcepackvalidator.validator.context.FileContext;
import dev.kinau.resourcepackvalidator.validator.generic.FileContextValidator;

import java.util.HashMap;
import java.util.Map;

public class FontTextureExistsValidator extends FileContextValidator<JsonElement, Map<JsonObject, String>> {

    public FontTextureExistsValidator(Map<String, JsonObject> config, TestSuite testSuite) {
        super(config, testSuite);
    }

    @Override
    protected ValidationResult<Map<JsonObject, String>> isValid(ValidationJob job, FileContext context, JsonElement langFile) {
        Map<JsonObject, String> textureData = new HashMap<>();
        if (!langFile.isJsonObject()) return success(textureData);
        if (!langFile.getAsJsonObject().has("providers")) return success(textureData);
        JsonElement providersElement = langFile.getAsJsonObject().get("providers");
        if (!providersElement.isJsonArray()) return success(textureData);
        JsonArray providers = providersElement.getAsJsonArray();

        boolean failed = false;
        for (JsonElement provider : providers) {
            if (!provider.isJsonObject()) continue;
            JsonObject providerObj = provider.getAsJsonObject();
            if (!providerObj.has("type")) continue;
            if (!providerObj.get("type").getAsString().equals("bitmap")) continue;
            if (!providerObj.has("file")) continue;
            JsonElement file = providerObj.get("file");
            if (!file.isJsonPrimitive() || !file.getAsJsonPrimitive().isString()) {
                failed = true;
                failedError("Language has invalid texture registered (file value is not string) for provider {} at {}", provider.toString(), context.value().getPath());
                continue;
            }
            textureData.put(providerObj, file.getAsString());
        }
        if (failed)
            return failedError();

        for (Map.Entry<JsonObject, String> kv : textureData.entrySet()) {
            if (!FileUtils.textureExists(context.namespace(), job.rootDir(), kv.getValue())) {
                failed = true;
                failedError("Language has linked texture that is not present ({}) at {}", kv.getKey() + " » " + kv.getValue(), context.value().getPath());
            }
        }
        if (failed)
            return failedError();

        return success(textureData);
    }


}
