package org.imanity.framework.bukkit.test.v1_8;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_8_R3.*;
import org.imanity.framework.Autowired;
import org.imanity.framework.bukkit.test.Bot;
import org.imanity.framework.bukkit.test.TestService;
import org.imanity.framework.bukkit.test.v1_8.util.NMSUtil;
import org.imanity.framework.bukkit.util.TaskUtil;

import java.util.UUID;

public class EntityBot extends EntityPlayer implements Bot {

    @Autowired
    private static TestService TEST_SERVICE;

    private static final float EPSILON = 0.005F;

    public EntityBot(MinecraftServer minecraftserver, WorldServer worldserver, GameProfile gameprofile, PlayerInteractManager playerinteractmanager) {
        super(minecraftserver, worldserver, gameprofile, playerinteractmanager);
    }

    @Override
    public void die(DamageSource damagesource) {
        if (!this.dead) {
            TaskUtil.runAsyncScheduled(() -> {
                PacketPlayInClientCommand in = new PacketPlayInClientCommand(PacketPlayInClientCommand.EnumClientCommand.PERFORM_RESPAWN);
                this.playerConnection.a(in);
            }, 20L);
        }
        super.die(damagesource);
    }

    public void t_() {
        super.t_();

        if (motX != 0 || motY != 0 || motZ != 0) {
            g(0, 0);
        }

        if (Math.abs(motX) < EPSILON && Math.abs(motY) < EPSILON && Math.abs(motZ) < EPSILON) {
            motX = motY = motZ = 0;
        }
    }

    @Override
    public void g(float f, float f1) {
        NMSUtil.flyingMoveLogic(this, f, f1);
    }

    @Override
    public void disconnect(String message) {
        this.playerConnection.disconnect(message);

        TEST_SERVICE.removeBot(this.getUniqueID());
    }
}
