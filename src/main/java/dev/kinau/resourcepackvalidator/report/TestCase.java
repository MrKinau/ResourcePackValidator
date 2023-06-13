package dev.kinau.resourcepackvalidator.report;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.Level;

import java.util.Arrays;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Accessors(fluent = true)
@Getter
@Setter
@Slf4j
public class TestCase {
    private final String name;
    private long time;
    private Failure failure;
    private int fails;

    @Getter(AccessLevel.PRIVATE)
    private long startedAt = -1;

    public TestCase start() {
        if (startedAt != -1) return this;
        this.startedAt = System.currentTimeMillis();
        return this;
    }

    public void stop() {
        addTime(System.currentTimeMillis() - startedAt);
        this.startedAt = -1;
    }

    public void addTime(long ms) {
        this.time += ms;
    }

    public void addErrorNoMessage(Level level, String error) {
        if (level == Level.ERROR)
            addError(error, (String) null);
        else
            addWarning(error, null);
    }

    public void addError(String error) {
        log.error(error);
        addError(error, (String) null);
    }

    public void addError(String error, Throwable ex) {
        log.error("Could not adjust the log level", ex);
        addError(error, Arrays.stream(ex.getStackTrace()).map(StackTraceElement::toString).collect(Collectors.joining("\n")));
    }

    public void addWarning(String error, String data) {
        fails++;
        if (failure == null)
            this.failure = new Failure(error + (data != null ? "\n" + data : ""), false);
        else {
            failure().data(failure.data() + "\n\n" + error + (data != null ? "\n" + data : ""));
        }
    }

    public void addError(String error, String data) {
        fails++;
        if (failure == null)
            this.failure = new Failure(error + (data != null ? "\n" + data : ""), true);
        else {
            failure().data(failure.data() + "\n\n" + error + (data != null ? "\n" + data : ""));
            if (!failure().error()) failure().error(true);
        }
    }

}
