package dev.kinau.resourcepackvalidator.validator;

import dev.kinau.resourcepackvalidator.ValidationJob;
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

public class ValidatorRegistry {

    private final Validator<ValidationJob, EmptyValidationContext, ValidationJob> rootValidator = new Validator<>() {
        @Override
        protected ValidationResult<ValidationJob> isValid(ValidationJob job, EmptyValidationContext context, ValidationJob data) {
            return success(job);
        }
    };

    public ValidatorRegistry() {
        rootValidator
                .then(new NamespaceCollectionValidator()
                        .then(new AnyNamespacePresentValidator()))
                .then(new JsonElementMapper()
                        .thenForEachElement(new ModelIsJsonObjectValidator()
                                .then(new ModelHasAnyTextureValidator()
                                        .then(new ModelTexturesExistsValidator()))
                                .then(new ModelHasAnyOverrideValidator()
                                        .then(new ModelOverridesExistsValidator()))))
                .then(new UnusedFileValidator());
    }

    public void validate(ValidationJob job) {
        rootValidator.validate(job, ValidationContext.EMPTY, job);
    }

}
