package dev.kinau.resourcepackvalidator.validator;

import com.google.gson.JsonObject;
import dev.kinau.resourcepackvalidator.ValidationJob;
import dev.kinau.resourcepackvalidator.config.Config;
import dev.kinau.resourcepackvalidator.validator.context.EmptyValidationContext;
import dev.kinau.resourcepackvalidator.validator.context.ValidationContext;
import dev.kinau.resourcepackvalidator.validator.general.AnyNamespacePresentValidator;
import dev.kinau.resourcepackvalidator.validator.general.UnusedFileValidator;
import dev.kinau.resourcepackvalidator.validator.generic.JsonElementMapper;
import dev.kinau.resourcepackvalidator.validator.generic.NamespaceCollectionValidator;
import dev.kinau.resourcepackvalidator.validator.models.ModelHasAnyTextureValidator;
import dev.kinau.resourcepackvalidator.validator.models.ModelIsJsonObjectValidator;
import dev.kinau.resourcepackvalidator.validator.models.ModelTexturesExistsValidator;
import dev.kinau.resourcepackvalidator.validator.models.override.ModelHasAnyOverrideValidator;
import dev.kinau.resourcepackvalidator.validator.models.override.ModelOverridesExistsValidator;

import java.util.Map;

public class ValidatorRegistry {

    private final Validator<ValidationJob, EmptyValidationContext, ValidationJob> rootValidator;

    public ValidatorRegistry(Config config) {
        Map<String, JsonObject> configData = config.validators();

        this.rootValidator = new Validator<>(configData) {
            @Override
            protected ValidationResult<ValidationJob> isValid(ValidationJob job, EmptyValidationContext context, ValidationJob data) {
                return success(job);
            }
        };

        rootValidator
                .then(new NamespaceCollectionValidator(configData)
                        .then(new AnyNamespacePresentValidator(configData)))
                .then(new JsonElementMapper(configData)
                        .thenForEachElement(new ModelIsJsonObjectValidator(configData)
                                .then(new ModelHasAnyTextureValidator(configData)
                                        .then(new ModelTexturesExistsValidator(configData)))
                                .then(new ModelHasAnyOverrideValidator(configData)
                                        .then(new ModelOverridesExistsValidator(configData)))))
                .then(new UnusedFileValidator(configData));
    }

    public void validate(ValidationJob job) {
        rootValidator.validate(job, ValidationContext.EMPTY, job);
    }

}
