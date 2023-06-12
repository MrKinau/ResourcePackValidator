package dev.kinau.resourcepackvalidator.validator.general;

import dev.kinau.resourcepackvalidator.ValidationJob;
import dev.kinau.resourcepackvalidator.utils.Namespace;
import dev.kinau.resourcepackvalidator.validator.ValidationResult;
import dev.kinau.resourcepackvalidator.validator.Validator;
import dev.kinau.resourcepackvalidator.validator.context.EmptyValidationContext;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
public class AnyNamespacePresentValidator extends Validator<Collection<Namespace>, EmptyValidationContext, Object> {

    @Override
    protected ValidationResult<Object> isValid(ValidationJob job, EmptyValidationContext context, Collection<Namespace> data) {
        if (data == null || data.isEmpty()) {
            return failedWarning("Could not find any namespace in the resource pack!\n" +
                    "Although it is valid, it does not change anything without a namespace (e.g. assets/minecraft/)");
        } else {
            log.debug("Found namespace(s): {}", data.stream().map(Namespace::getName).collect(Collectors.joining(", ")));
            return success();
        }
    }
}
