package org.imanity.framework.bukkit.test;

import lombok.Getter;
import org.imanity.framework.util.CC;

@Getter
public enum TestStatus {

    NOT_CALLED(CC.RED + "Not Called"),
    RUNNING(CC.WHITE + "Running..."),
    CALLED(CC.GREEN + "Called"),
    PASSED(CC.GREEN + "Passed"),
    ERROR(CC.DARK_RED + "Error");

    private final String displayName;

    TestStatus(String displayName) {
        this.displayName = displayName;
    }

}
