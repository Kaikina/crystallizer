package com.tgirou.crystallizer.networking;

import com.tgirou.crystallizer.api.util.Constants;
import com.tgirou.crystallizer.networking.packet.ItemStackSyncPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class Messages {
    private static SimpleChannel instance;
    private static int packetId = 0;

    private Messages() {}

    private static int id() {
        return packetId++;
    }

    public static void register() {
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(Constants.MOD_ID, "messages"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();
        instance = net;
        net.messageBuilder(ItemStackSyncPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(ItemStackSyncPacket::new)
                .encoder(ItemStackSyncPacket::toBytes)
                .consumer(ItemStackSyncPacket::handle)
                .add();
    }

    public static <T> void sendToServer(T message) {
        instance.sendToServer(message);
    }

    public static <T> void sendToPlayer(T message, ServerPlayer player) {
        instance.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static <T> void sendToClients(T message) {
        instance.send(PacketDistributor.ALL.noArg(), message);
    }
}
