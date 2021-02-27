package org.imanity.framework.bukkit.test;

import org.bukkit.entity.Player;

import java.util.UUID;

public interface Bot {

    void disconnect(String message);

    Player getBukkitEntity();

    UUID getUniqueID();

}
