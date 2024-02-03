package dev.kinau.resourcepackvalidator.validator.font;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.kinau.resourcepackvalidator.ValidationJob;
import dev.kinau.resourcepackvalidator.report.TestSuite;
import dev.kinau.resourcepackvalidator.validator.ValidationResult;
import dev.kinau.resourcepackvalidator.validator.context.FileContext;
import dev.kinau.resourcepackvalidator.validator.context.FileContextWithData;
import dev.kinau.resourcepackvalidator.validator.data.font.FontProvider;
import dev.kinau.resourcepackvalidator.validator.generic.MappingValidator;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Slf4j
public class FontProviderMapper extends MappingValidator<JsonElement, FileContext, FontProvider> {

    public FontProviderMapper(Map<String, JsonObject> config, TestSuite testSuite) {
        super(config, testSuite);
    }

    @Override
    protected ValidationResult<Collection<FileContextWithData<FontProvider>>> isValid(ValidationJob job, FileContext context, JsonElement fontFile) {
        if (fontFile == null || !fontFile.isJsonObject()) return failedError("Font file does not contain valid JSON at {}", context.value().getPath());
        if (!fontFile.getAsJsonObject().has("providers")) return failedError("Font file does not contain providers array at {}", context.value().getPath());
        JsonElement providersElement = fontFile.getAsJsonObject().get("providers");
        if (!providersElement.isJsonArray()) return failedError("Font file does not contain providers array at {}", context.value().getPath());
        JsonArray providers = providersElement.getAsJsonArray();

        List<FontProvider> providerList = new ArrayList<>();
        for (JsonElement provider : providers) {
            if (!provider.isJsonObject()) continue;
            FontProvider fontProvider = job.fontProviderFactory().create(provider.getAsJsonObject());
            if (fontProvider == null) continue;
            providerList.add(fontProvider);
        }
        return success(providerList.stream()
                .map(fontProvider -> new FileContextWithData<FontProvider>(context.namespace(), context.value(), fontProvider))
                .toList());
    }
}
