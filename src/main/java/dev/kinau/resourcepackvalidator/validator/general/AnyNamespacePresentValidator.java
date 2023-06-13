package dev.kinau.resourcepackvalidator.validator.general;

import com.google.gson.JsonObject;
import dev.kinau.resourcepackvalidator.ValidationJob;
import dev.kinau.resourcepackvalidator.report.TestSuite;
import dev.kinau.resourcepackvalidator.utils.Namespace;
import dev.kinau.resourcepackvalidator.validator.ValidationResult;
import dev.kinau.resourcepackvalidator.validator.Validator;
import dev.kinau.resourcepackvalidator.validator.context.EmptyValidationContext;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.Level;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class AnyNamespacePresentValidator extends Validator<Collection<Namespace>, EmptyValidationContext, Object> {

    public AnyNamespacePresentValidator(Map<String, JsonObject> config, TestSuite testSuite) {
        super(config, testSuite);
    }

    @Override
    protected ValidationResult<Object> isValid(ValidationJob job, EmptyValidationContext context, Collection<Namespace> data) {
        if (data == null || data.isEmpty()) {
            return failedError("Could not find any namespace in the resource pack!\n" +
                    "Although it is valid, it does not change anything without a namespace (e.g. assets/minecraft/)");
        } else {
            log.debug(logPrefix() + "Found namespace(s): {}", data.stream().map(Namespace::getName).collect(Collectors.joining(", ")));
            return success();
        }
    }

    @Override
    protected Level defaultFailedLogLevel() {
        return Level.WARN;
    }
}
