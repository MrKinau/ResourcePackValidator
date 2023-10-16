package dev.kinau.resourcepackvalidator.validator.texture;

import com.google.gson.JsonObject;
import dev.kinau.resourcepackvalidator.ValidationJob;
import dev.kinau.resourcepackvalidator.report.TestSuite;
import dev.kinau.resourcepackvalidator.utils.FileUtils;
import dev.kinau.resourcepackvalidator.validator.ValidationResult;
import dev.kinau.resourcepackvalidator.validator.context.EmptyValidationContext;
import dev.kinau.resourcepackvalidator.validator.context.FileContextWithData;
import dev.kinau.resourcepackvalidator.validator.generic.MappingValidator;
import lombok.extern.slf4j.Slf4j;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Slf4j
public class TextureMapper extends MappingValidator<ValidationJob, EmptyValidationContext, BufferedImage> {

    public TextureMapper(Map<String, JsonObject> config, TestSuite testSuite) {
        super(config, testSuite);
    }

    @Override
    protected ValidationResult<Collection<FileContextWithData<BufferedImage>>> isValid(ValidationJob job, EmptyValidationContext context, ValidationJob data) {
        List<FileContextWithData<BufferedImage>> images = new ArrayList<>();
        job.textureCache().values().forEach(namespaceTexture -> {
            namespaceTexture.cache().get(FileUtils.Directory.TEXTURES).forEach(element -> {
                images.add(new FileContextWithData<>(namespaceTexture.namespace(), element.file(), element.element()));
            });
        });
        return success(images);
    }
}
