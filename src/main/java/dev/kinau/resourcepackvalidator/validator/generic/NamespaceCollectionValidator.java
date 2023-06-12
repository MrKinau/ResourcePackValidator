package dev.kinau.resourcepackvalidator.validator.generic;

import com.google.gson.JsonObject;
import dev.kinau.resourcepackvalidator.ValidationJob;
import dev.kinau.resourcepackvalidator.utils.Namespace;
import dev.kinau.resourcepackvalidator.validator.ValidationResult;
import dev.kinau.resourcepackvalidator.validator.Validator;
import dev.kinau.resourcepackvalidator.validator.context.EmptyValidationContext;

import java.util.Collection;
import java.util.Map;

public class NamespaceCollectionValidator extends Validator<ValidationJob, EmptyValidationContext, Collection<Namespace>> {

    public NamespaceCollectionValidator(Map<String, JsonObject> config) {
        super(config);
    }

    @Override
    protected ValidationResult<Collection<Namespace>> isValid(ValidationJob job, EmptyValidationContext context, ValidationJob data) {
        return success(job.namespaces());
    }
}
