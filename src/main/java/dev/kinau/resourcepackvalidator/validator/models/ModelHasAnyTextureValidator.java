package dev.kinau.resourcepackvalidator.validator.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.kinau.resourcepackvalidator.ValidationJob;
import dev.kinau.resourcepackvalidator.report.TestSuite;
import dev.kinau.resourcepackvalidator.utils.FileUtils;
import dev.kinau.resourcepackvalidator.validator.ValidationResult;
import dev.kinau.resourcepackvalidator.validator.context.FileContext;
import dev.kinau.resourcepackvalidator.validator.generic.FileContextValidator;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.Level;

import java.util.List;
import java.util.Map;

@Slf4j
public class ModelHasAnyTextureValidator extends FileContextValidator<JsonObject, JsonObject> {

    private static final List<String> ARMOR_MODEL_CATEGORIES = List.of("wolf_body", "horse_body", "llama_body", "humanoid", "humanoid_leggings", "wings");

    public ModelHasAnyTextureValidator(Map<String, JsonObject> config, TestSuite testSuite) {
        super(config, testSuite);
    }

    @Override
    protected ValidationResult<JsonObject> isValid(ValidationJob job, FileContext context, JsonObject modelObj) {
        if (FileUtils.isArmorModel(context.value(), context.namespace())) {
            if (!modelObj.has("layers"))
                return failOrSkip(context, job, modelObj, "Layers is not specified in equipment model (layers attribute not present) at {}", context.value().getPath());
            if (!modelObj.get("layers").isJsonObject())
                return failOrSkip(context, job, modelObj, "Layers is not specified in equipment model (layers attribute not an object) at {}", context.value().getPath());
            JsonObject layers = modelObj.get("layers").getAsJsonObject();

            JsonObject textures = new JsonObject();
            for (String armorModelCategory : ARMOR_MODEL_CATEGORIES) {
                ValidationResult<JsonObject> result = testArmorLayer(layers, armorModelCategory, context, textures);
                if (result != null) return result;
            }

            if (textures.isEmpty())
                return failedError("No layers defined in equipment model at {}", context.value().getPath());
            JsonObject mappedTextures = new JsonObject();
            mappedTextures.add("textures", textures);
            return success(mappedTextures);
        } else {
            if (!modelObj.has("textures"))
                return failOrSkip(context, job, modelObj, "No textures specified in model (textures attribute not present) at {}", context.value().getPath());
            if (!modelObj.get("textures").isJsonObject())
                return failOrSkip(context, job, modelObj, "No textures specified in model (textures attribute not an object) at {}", context.value().getPath());
            if (modelObj.getAsJsonObject("textures").isEmpty())
                return failOrSkip(context, job, modelObj, "No textures specified in model (textures object is empty) at {}", context.value().getPath());
            JsonObject textures = modelObj.getAsJsonObject("textures");
            if (textures.keySet().stream().allMatch(s -> s.equals("particle"))) {
                ValidationResult<JsonObject> result = failOrIgnore(context, job, modelObj, "No textures specified in model (textures object only contains particles, although this maybe works, it's kinda illegal) at {}", context.value().getPath());
                if (result != null) return result;
            }
        }
        return success(modelObj);
    }

    private ValidationResult<JsonObject> failOrSkip(FileContext context, ValidationJob job, JsonObject data, String warning, Object... args) {
        if (hasParent(context, job, data))
            return skip();
        return failedError(warning, args);
    }

    private ValidationResult<JsonObject> failOrIgnore(FileContext context, ValidationJob job, JsonObject data, String warning, Object... args) {
        if (!hasParent(context, job, data))
            return failedError(warning, args);
        return null;
    }

    private boolean hasParent(FileContext context, ValidationJob job, JsonObject obj) {
        if (obj.has("parent") && obj.get("parent").isJsonPrimitive() && obj.getAsJsonPrimitive("parent").isString()) {
            String parent = obj.getAsJsonPrimitive("parent").getAsString();
            return FileUtils.modelExists(context.namespace(), parent, job.assetDictionary());
        }
        return false;
    }

    private ValidationResult<JsonObject> testArmorLayer(JsonObject layers, String layer, FileContext context, JsonObject returnTextures) {
        if (!layers.has(layer)) return null;
        if (!layers.get(layer).isJsonArray())
            return failedError("Layer {} is not specified in equipment model ({} attribute not an array) at {}", layer, layer, context.value().getPath());
        JsonArray textures = layers.get(layer).getAsJsonArray();
        if (textures.isEmpty())
            return failedError("No textures specified in equipment layer {} at {}", layer, context.value().getPath());
        for (int i = 0; i < textures.size(); i++) {
            if (!textures.get(i).isJsonObject())
                return failedError("Specified texture in equipment layer {} is not an object (array element {}, starting at 0) at {}", layer, i, context.value().getPath());
            JsonObject texture = textures.get(i).getAsJsonObject();
            if (!texture.has("texture"))
                return failedError("No texture specified in equipment layer {} (texture in array element {} is missing, starting at 0) at {}", layer, i, context.value().getPath());
            if (!texture.get("texture").isJsonPrimitive() || !texture.get("texture").getAsJsonPrimitive().isString())
                return failedError("No texture specified in equipment layer {} (texture in array element {} is not a string, starting at 0) at {}", layer, i, context.value().getPath());
            returnTextures.addProperty(layer + "[" + i + "]", texture.get("texture").getAsString());
        }
        return null;
    }

    @Override
    protected Level defaultFailedLogLevel() {
        return Level.WARN;
    }
}
