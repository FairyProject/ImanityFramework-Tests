package org.imanity.framework.bukkit.test.condition;

import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;
import org.imanity.framework.bukkit.test.TestInfo;
import org.imanity.framework.bukkit.test.TestService;
import org.jetbrains.annotations.Nullable;

@RequiredArgsConstructor
public class TestConditionSpEL implements TestCondition {

    private final String condition;

    @Override
    public boolean check(@Nullable CommandSender executor, TestInfo testInfo) {
        try {
            return this.condition == null || this.condition.isEmpty() || TestService.INSTANCE.getScriptParser().getElValue(this.condition, testInfo.getTarget(), null, Boolean.class);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    @Override
    public String toInfo() {
        return "SpEL: " + condition;
    }
}
