package org.imanity.framework.bukkit.test.condition;

import org.bukkit.command.CommandSender;
import org.imanity.framework.bukkit.test.TestInfo;
import org.jetbrains.annotations.Nullable;

public interface TestCondition {

    boolean check(@Nullable CommandSender executor, TestInfo testInfo);

    String toInfo();
}
