package com.diamssword.characters.forge;

import com.diamssword.characters.Characters;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetworking {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(Characters.asRessource("capabilities_syncer"),() -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals,PROTOCOL_VERSION::equals);
    
    private static int packetId = 0;
    
    public static void register() {
        INSTANCE.messageBuilder(SyncPlayerStatsPacket.class, packetId++)
            .decoder(SyncPlayerStatsPacket::decode)
            .encoder(SyncPlayerStatsPacket::encode)
            .consumerMainThread(SyncPlayerStatsPacket::handle)
            .add();
    }
    
    // Send sync packet to specific player
    public static <MSG> void syncToClient(ServerPlayerEntity player,MSG msg) {
            INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), msg);
    }
    
    // Send sync packet to all players tracking this player
    public static <MSG> void syncToTracking(ServerPlayerEntity player,MSG msg) {

            INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> player), msg);
            syncToClient(player,msg);
    }
    
    // Send sync packet to all players
    public static <MSG> void syncToAll(ServerPlayerEntity player,MSG msg) {
            INSTANCE.send(PacketDistributor.ALL.noArg(), msg);
    }
}