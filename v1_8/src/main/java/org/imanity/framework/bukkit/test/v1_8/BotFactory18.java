package org.imanity.framework.bukkit.test.v1_8;

import com.mojang.authlib.GameProfile;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import net.minecraft.server.v1_8_R3.*;
import org.apache.logging.log4j.LogManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Player;
import org.imanity.framework.Autowired;
import org.imanity.framework.Component;
import org.imanity.framework.bukkit.test.Bot;
import org.imanity.framework.bukkit.test.BotFactory;
import org.imanity.framework.bukkit.test.TestService;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.UUID;

@Component
public class BotFactory18 implements BotFactory {

    @Autowired
    private TestService testService;

    @Override
    public boolean canActive() {
        try {
            Class.forName("net.minecraft.server.v1_8_R3.EntityPlayer");
            return true;
        } catch (ClassNotFoundException throwable) {
            return false;
        }
    }

    @Override
    public Bot generateBot(UUID uuid, String name) {
        DedicatedPlayerList playerList = ((CraftServer) Bukkit.getServer()).getHandle();

        GameProfile gameProfile = (new ProfileLoader(uuid.toString(), name)).loadProfile();

        NetworkManager network = new NetworkManager(EnumProtocolDirection.SERVERBOUND);
        InetSocketAddress address = new InetSocketAddress(Bukkit.getServer().getIp(), Bukkit.getServer().getPort());

        try {
            Bootstrap b = new Bootstrap();
            b.option(ChannelOption.TCP_NODELAY, true);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.group(new NioEventLoopGroup());
            b.channel(NioSocketChannel.class);
            ServerConnectionChannel scc = new ServerConnectionChannel(new ServerConnection(((CraftServer) Bukkit.getServer()).getHandle().getServer()));
            b.handler(scc);

            ChannelFuture f = b.connect(address.getAddress(), address.getPort()).sync(); // (5)
            //network.channel = f.channel();
            network.channelActive(f.channel().pipeline().lastContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        WorldServer world = (WorldServer) MinecraftServer.getServer().getWorld();
        EntityBot entityBot = new EntityBot(((CraftServer) Bukkit.getServer()).getServer(), world, gameProfile, new PlayerInteractManager(world));
        this.testService.addBot(entityBot);

        this.a(playerList, network, entityBot);

        return entityBot;
    }

    public void a(DedicatedPlayerList playerList, NetworkManager networkmanager, EntityPlayer entityplayer) {
        MinecraftServer server = MinecraftServer.getServer();

        GameProfile gameprofile = entityplayer.getProfile();
        UserCache usercache = server.getUserCache();
        GameProfile gameprofile1 = usercache.a(gameprofile.getId());
        String s = gameprofile1 == null ? gameprofile.getName() : gameprofile1.getName();
        usercache.a(gameprofile);
        NBTTagCompound nbttagcompound = playerList.a(entityplayer);
        if (nbttagcompound != null && nbttagcompound.hasKey("bukkit")) {
            NBTTagCompound bukkit = nbttagcompound.getCompound("bukkit");
            s = bukkit.hasKeyOfType("lastKnownName", 8) ? bukkit.getString("lastKnownName") : s;
        }

        entityplayer.spawnIn(server.getWorldServer(entityplayer.dimension));
        entityplayer.playerInteractManager.a((WorldServer)entityplayer.world);
        String s1 = "local";
        if (networkmanager.getSocketAddress() != null) {
            s1 = networkmanager.getSocketAddress().toString();
        }

        Player bukkitPlayer = entityplayer.getBukkitEntity();
        PlayerSpawnLocationEvent ev = new PlayerSpawnLocationEvent(bukkitPlayer, bukkitPlayer.getLocation());
        Bukkit.getPluginManager().callEvent(ev);
        Location loc = ev.getSpawnLocation();
        WorldServer world = ((CraftWorld)loc.getWorld()).getHandle();
        entityplayer.spawnIn(world);
        entityplayer.setPosition(loc.getX(), loc.getY(), loc.getZ());

        try {
            final Method setYawPitch = Entity.class.getDeclaredMethod("setYawPitch", float.class, float.class);
            setYawPitch.setAccessible(true);
            setYawPitch.invoke(entityplayer, loc.getYaw(), loc.getPitch());
        } catch (Throwable throwable) {
            throw new IllegalArgumentException(throwable);
        }

        WorldServer worldserver = server.getWorldServer(entityplayer.dimension);
        WorldData worlddata = worldserver.getWorldData();
        BlockPosition blockposition = worldserver.getSpawn();

        try {
            final Method a = PlayerList.class.getDeclaredMethod("a", EntityPlayer.class, EntityPlayer.class, World.class);
            a.setAccessible(true);
            a.invoke(playerList, entityplayer, null, worldserver);
        } catch (Throwable throwable) {
            throw new IllegalArgumentException(throwable);
        }

        PlayerConnection playerconnection = new DummyPlayerConnection(server, networkmanager, entityplayer);
        playerconnection.sendPacket(new PacketPlayOutLogin(entityplayer.getId(), entityplayer.playerInteractManager.getGameMode(), worlddata.isHardcore(), worldserver.worldProvider.getDimension(), worldserver.getDifficulty(), Math.min(playerList.getMaxPlayers(), 60), worlddata.getType(), worldserver.getGameRules().getBoolean("reducedDebugInfo")));
        entityplayer.getBukkitEntity().sendSupportedChannels();
        playerconnection.sendPacket(new PacketPlayOutCustomPayload("MC|Brand", (new PacketDataSerializer(Unpooled.buffer())).a(server.getServerModName())));
        playerconnection.sendPacket(new PacketPlayOutServerDifficulty(worlddata.getDifficulty(), worlddata.isDifficultyLocked()));
        playerconnection.sendPacket(new PacketPlayOutSpawnPosition(blockposition));
        playerconnection.sendPacket(new PacketPlayOutAbilities(entityplayer.abilities));
        playerconnection.sendPacket(new PacketPlayOutHeldItemSlot(entityplayer.inventory.itemInHandIndex));
        entityplayer.getStatisticManager().d();
        entityplayer.getStatisticManager().updateStatistics(entityplayer);
        playerList.sendScoreboard((ScoreboardServer)worldserver.getScoreboard(), entityplayer);
        server.aH();
        String joinMessage;
        if (!entityplayer.getName().equalsIgnoreCase(s)) {
            joinMessage = "§e" + LocaleI18n.a("multiplayer.player.joined.renamed", new Object[]{entityplayer.getName(), s});
        } else {
            joinMessage = "§e" + LocaleI18n.a("multiplayer.player.joined", new Object[]{entityplayer.getName()});
        }

        playerList.onPlayerJoin(entityplayer, joinMessage);
        worldserver = server.getWorldServer(entityplayer.dimension);
        playerconnection.a(entityplayer.locX, entityplayer.locY, entityplayer.locZ, entityplayer.yaw, entityplayer.pitch);
        playerList.b(entityplayer, worldserver);
        if (server.getResourcePack().length() > 0) {
            entityplayer.setResourcePack(server.getResourcePack(), server.getResourcePackHash());
        }

        Iterator iterator = entityplayer.getEffects().iterator();

        while(iterator.hasNext()) {
            MobEffect mobeffect = (MobEffect)iterator.next();
            playerconnection.sendPacket(new PacketPlayOutEntityEffect(entityplayer.getId(), mobeffect));
        }

        entityplayer.syncInventory();
        if (nbttagcompound != null && nbttagcompound.hasKeyOfType("Riding", 10)) {
            Entity entity = EntityTypes.a(nbttagcompound.getCompound("Riding"), worldserver);
            if (entity != null) {
                entity.attachedToPlayer = true;
                worldserver.addEntity(entity);
                entityplayer.mount(entity);
                entity.attachedToPlayer = false;
            }
        }

        LogManager.getLogger(DedicatedPlayerList.class).info(entityplayer.getName() + "[" + s1 + "] logged in with entity id " + entityplayer.getId() + " at ([" + entityplayer.world.worldData.getName() + "]" + entityplayer.locX + ", " + entityplayer.locY + ", " + entityplayer.locZ + ")");
    }
}
