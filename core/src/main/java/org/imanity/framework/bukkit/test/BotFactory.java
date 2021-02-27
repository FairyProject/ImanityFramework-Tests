package org.imanity.framework.bukkit.test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public interface BotFactory {

    static final AtomicInteger ID_COUNTER = new AtomicInteger(0);

    boolean canActive();

    default Bot generateRandomBot() {
        return this.generateBot(TestService.INSTANCE.getRandomUuid(), "Bot-" + ID_COUNTER.getAndIncrement());
    }

    Bot generateBot(UUID uuid, String name);

}
