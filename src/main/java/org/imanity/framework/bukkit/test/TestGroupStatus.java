package org.imanity.framework.bukkit.test;

import lombok.Getter;
import org.imanity.framework.util.CC;

public enum TestGroupStatus {

    NOT_RUNNING("&7Not Running"),
    RUNNING("&fRunning"),
    SUCCESS("&aSuccessful"),
    FAILURE("&cFailure");

    @Getter
    private final String displayName;

    TestGroupStatus(String displayName) {
        this.displayName = CC.translate(displayName);
    }

}
