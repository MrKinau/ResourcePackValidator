package dev.kinau.resourcepackvalidator.report;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@AllArgsConstructor
@Accessors(fluent = true)
@Getter
@Setter
public class Failure {
    private String data;
    private boolean error;
}
