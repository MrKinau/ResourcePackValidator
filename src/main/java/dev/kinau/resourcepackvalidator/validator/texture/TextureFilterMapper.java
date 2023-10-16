package dev.kinau.resourcepackvalidator.validator.texture;

import com.google.gson.JsonObject;
import dev.kinau.resourcepackvalidator.ValidationJob;
import dev.kinau.resourcepackvalidator.report.TestSuite;
import dev.kinau.resourcepackvalidator.validator.ValidationResult;
import dev.kinau.resourcepackvalidator.validator.context.FileContext;
import dev.kinau.resourcepackvalidator.validator.context.FileContextWithData;
import dev.kinau.resourcepackvalidator.validator.generic.FileContextValidator;

import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.function.BiPredicate;

public class TextureFilterMapper extends FileContextValidator<BufferedImage, BufferedImage> {

    private final BiPredicate<ValidationJob, FileContextWithData<BufferedImage>> predicate;

    public TextureFilterMapper(Map<String, JsonObject> config, TestSuite testSuite, BiPredicate<ValidationJob, FileContextWithData<BufferedImage>> predicate) {
        super(config, testSuite);
        this.predicate = predicate;
    }

    @Override
    protected ValidationResult<BufferedImage> isValid(ValidationJob job, FileContext context, BufferedImage data) {
        if (predicate.test(job, new FileContextWithData<>(context.namespace(), context.value(), data)))
            return success(data);
        return skip();
    }
}
