package com.diamssword.characters.forge;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class SyncPlayerStatsPacket {
    private final UUID player;
    private final Identifier capability;
    private final NbtCompound data;
    
    protected SyncPlayerStatsPacket(UUID player,Identifier capability, NbtCompound data) {
        this.player=player;
        this.capability = capability;
        this.data = data;
    }
    public SyncPlayerStatsPacket(PlayerEntity player,Identifier capability) {
        var cap=CapabilityEvents.CAPABILITY_MAP.get(capability);
        var d=player.getCapability(cap).resolve().get();
        this.player=player.getUuid();
        this.capability = capability;
        if(d instanceof SyncedCapability ca)
            this.data = ca.writeSyncData();
        else
            this.data = new NbtCompound();
    }
    
    // Encode packet data
    public static void encode(SyncPlayerStatsPacket packet, PacketByteBuf buf) {
        buf.writeUuid(packet.player);
        buf.writeIdentifier(packet.capability);
        buf.writeNbt(packet.data);
    }
    
    // Decode packet data
    public static SyncPlayerStatsPacket decode(PacketByteBuf buf) {
        UUID id=buf.readUuid();
        Identifier cap = buf.readIdentifier();
        var data=buf.readNbt();
        return new SyncPlayerStatsPacket(id,cap,data);
    }

    // Handle packet on client side
    public static void handle(SyncPlayerStatsPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            // This runs on the client side
            handleClientSide(packet);
        });
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClientSide(SyncPlayerStatsPacket packet) {
        // Get the client player
        MinecraftClient minecraft = MinecraftClient.getInstance();
        World level = minecraft.world;
        if (level == null) return;

        // Find the player by UUID
        PlayerEntity player = level.getPlayerByUuid(packet.player);
        if (player != null) {
            // Update the client-side capability data
            var cap=CapabilityEvents.CAPABILITY_MAP.get(packet.capability);
            if(cap !=null) {
                player.getCapability(cap).ifPresent(stats -> {
                    if (stats instanceof SyncedCapability clientStats) {
                        clientStats.applySyncData(packet.data);
                    }
                });
            }
        }
    }
}