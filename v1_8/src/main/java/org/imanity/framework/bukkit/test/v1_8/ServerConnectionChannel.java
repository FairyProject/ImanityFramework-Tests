package org.imanity.framework.bukkit.test.v1_8;

import io.netty.channel.*;
import io.netty.handler.timeout.ReadTimeoutHandler;
import net.minecraft.server.v1_8_R3.*;

@SuppressWarnings({ "rawtypes"})
public class ServerConnectionChannel extends ChannelInitializer {
    final ServerConnection serverConnection;

    public ServerConnectionChannel(ServerConnection serverconnection) {
        this.serverConnection = serverconnection;
    }

    protected void initChannel(Channel channel) {
        try {
            channel.config().setOption(ChannelOption.IP_TOS, 24);
        } catch (ChannelException ignored) {
        }

        try {
            channel.config().setOption(ChannelOption.TCP_NODELAY, Boolean.FALSE);
        } catch (ChannelException ignored) {
        }

        channel.pipeline().addLast("timeout", new ReadTimeoutHandler(30))
                .addLast("legacy_query", new LegacyPingHandler(this.serverConnection))
                .addLast("splitter", new PacketSplitter())
                .addLast("decoder", new PacketDecoder(EnumProtocolDirection.SERVERBOUND))
                .addLast("prepender", new PacketPrepender())
                .addLast("encoder", new PacketEncoder(EnumProtocolDirection.CLIENTBOUND));
        NetworkManager networkmanager = new NetworkManager(EnumProtocolDirection.SERVERBOUND);

        channel.pipeline().addLast("packet_handler", networkmanager);
        networkmanager.a(new HandshakeListener(serverConnection.d(), networkmanager));
    }
}