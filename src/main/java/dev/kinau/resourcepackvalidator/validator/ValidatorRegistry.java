package dev.kinau.resourcepackvalidator.validator;

import com.google.gson.JsonObject;
import dev.kinau.resourcepackvalidator.ValidationJob;
import dev.kinau.resourcepackvalidator.config.Config;
import dev.kinau.resourcepackvalidator.report.TestSuite;
import dev.kinau.resourcepackvalidator.utils.FileUtils;
import dev.kinau.resourcepackvalidator.validator.context.EmptyValidationContext;
import dev.kinau.resourcepackvalidator.validator.context.FileContext;
import dev.kinau.resourcepackvalidator.validator.context.FileContextWithData;
import dev.kinau.resourcepackvalidator.validator.context.ValidationContext;
import dev.kinau.resourcepackvalidator.validator.data.font.BitmapFontProvider;
import dev.kinau.resourcepackvalidator.validator.data.font.FontProvider;
import dev.kinau.resourcepackvalidator.validator.font.FontCharacterUsageValidator;
import dev.kinau.resourcepackvalidator.validator.font.FontProviderMapper;
import dev.kinau.resourcepackvalidator.validator.font.FontTextureExistsValidator;
import dev.kinau.resourcepackvalidator.validator.general.AnyNamespacePresentValidator;
import dev.kinau.resourcepackvalidator.validator.general.UnusedFileValidator;
import dev.kinau.resourcepackvalidator.validator.generic.FilterValidator;
import dev.kinau.resourcepackvalidator.validator.generic.JsonElementMapper;
import dev.kinau.resourcepackvalidator.validator.generic.NamespaceCollectionValidator;
import dev.kinau.resourcepackvalidator.validator.models.*;
import dev.kinau.resourcepackvalidator.validator.models.override.ModelHasAnyOverrideValidator;
import dev.kinau.resourcepackvalidator.validator.models.override.ModelOverridesExistsValidator;
import dev.kinau.resourcepackvalidator.validator.texture.TextureFilterMapper;
import dev.kinau.resourcepackvalidator.validator.texture.TextureIsNotCorruptedValidator;
import dev.kinau.resourcepackvalidator.validator.texture.TextureLimitMipLevelValidator;
import dev.kinau.resourcepackvalidator.validator.texture.TextureMapper;

import java.awt.image.BufferedImage;
import java.util.Map;

public class ValidatorRegistry {

    private final Validator<ValidationJob, EmptyValidationContext, ValidationJob> rootValidator;

    public ValidatorRegistry(Config config, TestSuite testSuite) {
        Map<String, JsonObject> configData = config.validators();

        this.rootValidator = new Validator<>(configData, testSuite) {
            @Override
            protected ValidationResult<ValidationJob> isValid(ValidationJob job, EmptyValidationContext context, ValidationJob data) {
                return success(job);
            }

            @Override
            protected boolean skipTestCase(EmptyValidationContext context) {
                return true;
            }
        };

        rootValidator
                .then(new NamespaceCollectionValidator(configData, testSuite)
                        .then(new AnyNamespacePresentValidator(configData, testSuite)))
                .then(new JsonElementMapper(configData, testSuite, FileUtils.Directory.MODELS)
                        .thenForEachElement(new ModelIsJsonObjectValidator(configData, testSuite)
                                .then(new ModelHasAnyTextureValidator(configData, testSuite)
                                        .then(new ModelTexturesExistsValidator(configData, testSuite)))
                                .then(new ModelHasAnyOverrideValidator(configData, testSuite)
                                        .then(new ModelOverridesExistsValidator(configData, testSuite)))
                                .then(new ModelRequiresOverlayOverrideValidator(configData, testSuite))
                                .then(new ModelParentValidator(configData, testSuite))
                                .then(new ModelTextureReferencesResolvableValidator(configData, testSuite))))
                .then(new JsonElementMapper(configData, testSuite, FileUtils.Directory.FONT)
                        .thenForEachElement(new FontProviderMapper(configData, testSuite)
                                .thenForEachElement(new FilterValidator<FontProvider, FileContext, BitmapFontProvider>(configData, testSuite, this::isBitmapFontProvider)
                                        .then(new FontTextureExistsValidator(configData, testSuite)))
                                .thenForEachElement(new FontCharacterUsageValidator(configData, testSuite))))
                .then(new TextureMapper(configData, testSuite)
                        .thenForEachElement(new TextureIsNotCorruptedValidator(configData, testSuite)
                                .then(new TextureFilterMapper(configData, testSuite, this::isPartOfAtlas)
                                        .then(new TextureLimitMipLevelValidator(configData, testSuite)))))
                .then(new UnusedFileValidator(configData, testSuite));
    }

    public boolean validate(ValidationJob job) {
        ValidationResult.Status status = rootValidator.validate(job, ValidationContext.EMPTY, job);
        return status != ValidationResult.Status.FAILED;
    }

    private BitmapFontProvider isBitmapFontProvider(ValidationJob job, FontProvider fontProvider) {
        if (fontProvider instanceof BitmapFontProvider bitmapFontProvider)
            return bitmapFontProvider;
        return null;
    }

    private boolean isPartOfAtlas(ValidationJob job, FileContextWithData<BufferedImage> context) {
        return job.textureAtlas().isPartOfAtlas(context.namespace(), context.value());
    }

}
