package org.imanity.framework.bukkit.test;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.imanity.framework.Autowired;
import org.imanity.framework.util.CC;
import org.imanity.framework.util.Stacktrace;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Getter
@RequiredArgsConstructor
public class TestInfo {

    private static final Logger LOGGER = LogManager.getLogger();

    @Autowired
    private static TestService TEST_SERVICE;

    private final String name;

    private final Material type;

    private final Method method;

    private final Object target;

    private final String condition;

    private final Class<? extends Throwable> expected;

    private final boolean hasParameter;

    private TestStatus status = TestStatus.NOT_CALLED;
    private Throwable throwable;

    public boolean checkCondition() {
        try {
            return this.condition == null || this.condition.isEmpty() || TestService.INSTANCE.getScriptParser().getElValue(this.condition, this.target, null, Boolean.class);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public void run(@Nullable CommandSender sender, boolean mustNoCall, boolean mustPassCondition) {
        if ((!mustNoCall || status == TestStatus.NOT_CALLED) && (!mustPassCondition || this.checkCondition())) {
            Throwable error = null;
            Object result = null;

            try {
                if (this.hasParameter) {
                    result = this.method.invoke(this.target, TEST_SERVICE.getBotFactory());
                } else {
                    result = this.method.invoke(this.target);
                }
            } catch (Throwable throwable) {
                error = throwable;
                if (error instanceof InvocationTargetException && error.getCause() != null) {
                    error = error.getCause();
                }
            }

            this.throwable = error;

            if (this.expected != null) {
                if (this.expected.isInstance(error)) {
                    this.result(sender, TestStatus.PASSED, "&7Expected Error: &a" + error.getClass().getSimpleName());
                    return;
                }
                this.result(sender, TestStatus.ERROR, "&7Expected Error: &c" + this.expected.getSimpleName());
                return;
            }

            if (error != null) {
                this.result(sender, TestStatus.ERROR, "&7Unexpected Error: &c" + throwable.getClass().getSimpleName());
                return;
            }

            if (result != null) {
                if (result instanceof Boolean) {
                    boolean relBol = (boolean) result;
                    if (relBol) {
                        this.result(sender, TestStatus.PASSED, "&7Returning &aTrue");
                        return;
                    }

                    this.result(sender, TestStatus.ERROR, "&7Returning &cFalse");
                    return;
                }

                this.result(sender, TestStatus.CALLED, "&7Returning Value: &w" + result.toString());
                return;
            }

            this.result(sender, TestStatus.CALLED, "&7Runs Correctly");
        } else if (sender != null) {
            sender.sendMessage(CC.RED + "The Test didn't passed run condition!");
        }
    }

    public void result(@Nullable CommandSender sender, TestStatus status, String message) {
        this.status = status;

        this.log(sender, CC.CHAT_BAR);
        this.log(sender, "&7Test: &f" + this.name);
        String resultMessage = "&7Result: " + status.getDisplayName();

        this.log(sender, resultMessage);
        if (message != null) {
            this.log(sender, message);
        }
        if (this.throwable != null && status != TestStatus.PASSED) {
            Stacktrace.print(throwable);
        }
        this.log(sender, CC.CHAT_BAR);
    }

    private void log(@Nullable CommandSender executor, String message) {
        message = CC.translate(message);
        LOGGER.info(message);
        if (executor != null) {
            executor.sendMessage(message);
        }
    }

    public boolean isCalled() {
        return this.status != TestStatus.CALLED;
    }


}
