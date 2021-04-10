package org.imanity.framework.bukkit.test;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.imanity.framework.Autowired;
import org.imanity.framework.BeanContext;
import org.imanity.framework.bukkit.test.condition.TestCondition;
import org.imanity.framework.details.constructor.BeanParameterDetailsMethod;
import org.imanity.framework.util.CC;
import org.imanity.framework.util.Stacktrace;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Getter
@RequiredArgsConstructor
public class TestInfo implements Comparable<TestInfo> {

    private static final Logger LOGGER = LogManager.getLogger();

    @Autowired
    private static BeanContext BEAN_CONTEXT;

    private final String name;
    private final Material type;
    private final BeanParameterDetailsMethod method;
    private final Object target;
    private final TestCondition condition;
    private final boolean requireExecutor;
    private final Class<? extends Throwable> expected;
    private final int priority;
    private final TestList testList;

    private TestStatus status = TestStatus.NOT_CALLED;
    private Throwable throwable;

    public boolean checkCondition(@Nullable CommandSender sender) {
        if (this.requireExecutor && sender == null) {
            return false;
        }
        return this.condition.check(sender, this);
    }

    public void run(@Nullable CommandSender sender, boolean mustNoCall, boolean mustPassCondition) {
        if (!this.testList.canRun(this)) {
            return;
        }
        if ((!mustNoCall || status == TestStatus.NOT_CALLED) && (!mustPassCondition || this.checkCondition(sender))) {
            this.status = TestStatus.RUNNING;
            Throwable error = null;
            Object result = null;

            try {
                result = this.method.invoke(this.target, BEAN_CONTEXT);
            } catch (Throwable throwable) {
                error = throwable;
                if (error instanceof InvocationTargetException && error.getCause() != null) {
                    error = error.getCause();
                }
            }

            if (result instanceof CompletableFuture) {
                CompletableFuture<?> future = (CompletableFuture) result;
                future.thenAccept(res -> this.postRun(sender, res)).exceptionally(throwable -> {
                    this.throwable = throwable;
                    return null;
                });
                return;
            }

            this.throwable = error;
            this.postRun(sender, result);
        } else if (sender != null) {
            this.testList.stopRunning(TestGroupStatus.FAILURE);
            sender.sendMessage(CC.RED + "The Test didn't passed run condition!");
        }
    }

    public void reset() {
        this.throwable = null;
        this.status = TestStatus.NOT_CALLED;
    }

    public void postRun(@Nullable CommandSender sender, Object result) {
        if (this.expected != null) {
            if (this.expected.isInstance(this.throwable)) {
                this.result(sender, TestStatus.PASSED, "&7Expected Error: &a" + this.throwable.getClass().getSimpleName());
                return;
            } else if (this.throwable != null) {
                this.result(sender, TestStatus.ERROR, "&7Unexpected Error: &c" + throwable.getClass().getSimpleName());
                return;
            }
            this.result(sender, TestStatus.ERROR, "&7Not Receiving Expected Error: &c" + this.expected.getSimpleName());
            return;
        }

        if (this.throwable != null) {
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

        if (this.isPassed()) {
            this.testList.next();
        } else {
            this.testList.stopRunning(TestGroupStatus.FAILURE);
        }
    }

    private void log(@Nullable CommandSender executor, String message) {
        message = CC.translate(message);
        LOGGER.info(message);
        if (executor != null) {
            executor.sendMessage(message);
        }
    }

    public boolean isPassed() {
        return this.status == TestStatus.CALLED || this.status == TestStatus.PASSED;
    }

    public boolean isCalled() {
        return this.status != TestStatus.CALLED;
    }

    public boolean hasCondition() {
        return this.condition != null;
    }

    public void remove() {
        this.testList.removeTest(this);
    }

    @Override
    public int compareTo(@NotNull TestInfo testInfo) {
        return Integer.compare(this.priority, testInfo.priority);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestInfo testInfo = (TestInfo) o;
        return name.equals(testInfo.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
