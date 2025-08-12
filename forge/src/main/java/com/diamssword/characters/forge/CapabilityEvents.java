package com.diamssword.characters.forge;

import com.diamssword.characters.Characters;
import com.diamssword.characters.storage.ComponentManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CapabilityEvents {
    public static final Map<Identifier, Capability<?>> CAPABILITY_MAP = new HashMap<>();
    public static final Identifier APPEARANCE = Characters.asRessource("appearance");
    public static final Identifier CHARACTERS = Characters.asRessource("characters");
    public static final Identifier SKIN_SERVER = Characters.asRessource("skin_server_cache");
    public static final Capability<PlayerDatasImpl> PLAYER_APPEARANCE = CapabilityManager.get(new CapabilityToken<>(){});
    public static final Capability<SkinServerCacheImpl> SKIN_SERVER_CACHE = CapabilityManager.get(new CapabilityToken<>(){});
    public static final Capability<PlayerCharactersImpl> PLAYER_CHARACTERS = CapabilityManager.get(new CapabilityToken<>(){});
    public static void register(RegisterCapabilitiesEvent event) {
        CAPABILITY_MAP.put(APPEARANCE,PLAYER_APPEARANCE);
        CAPABILITY_MAP.put(CHARACTERS,PLAYER_CHARACTERS);
        CAPABILITY_MAP.put(SKIN_SERVER,SKIN_SERVER_CACHE);
        event.register(PlayerDatasImpl.class);
        event.register(SkinServerCacheImpl.class);
        event.register(PlayerCharactersImpl.class);
    }
    // Attach capabilities to players
    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof PlayerEntity pl) {
            PlayerDataProvider provider = new PlayerDataProvider(pl);
            event.addCapability(APPEARANCE, provider);
            event.addListener(provider::invalidate);
            PlayerCharactersProvider provider1 = new PlayerCharactersProvider(pl);
            event.addCapability(CHARACTERS, provider1);
            event.addListener(provider1::invalidate);
        }
    }
    @SubscribeEvent
    public static void onAttachCapabilitiesWorld(AttachCapabilitiesEvent<World> event) {
        if (event.getObject() instanceof ServerWorld serverLevel) {
            if (serverLevel.getRegistryKey() == World.OVERWORLD) {
                SkinServerCacheProvider provider = new SkinServerCacheProvider();
                event.addCapability(SKIN_SERVER, provider);
                event.addListener(provider::invalidate);
            }
        }
    }
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player instanceof ServerPlayerEntity) {
            event.player.getCapability(PLAYER_APPEARANCE).ifPresent(PlayerDatasImpl::serverTick);
        }
    }
    // Handle player respawning/dimension change
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        PlayerEntity oldPlayer = event.getOriginal();
        PlayerEntity newPlayer = event.getEntity();
        
        oldPlayer.getCapability(PLAYER_APPEARANCE).ifPresent(oldStats -> {
            newPlayer.getCapability(PLAYER_APPEARANCE).ifPresent(newStats -> {
                newStats.fromNBT(oldStats.toNBT());
            });
        });
        oldPlayer.getCapability(PLAYER_CHARACTERS).ifPresent(oldStats -> {
            newPlayer.getCapability(PLAYER_CHARACTERS).ifPresent(newStats -> {
                newStats.readFromNbt(oldStats.toNBT());
            });
        });
    }
}