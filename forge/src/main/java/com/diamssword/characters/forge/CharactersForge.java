package com.diamssword.characters.forge;

import com.diamssword.characters.storage.ComponentManager;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import com.diamssword.characters.Characters;

@Mod(Characters.MOD_ID)
public final class CharactersForge {
    public CharactersForge() {
        // Submit our event bus to let Architectury API register our content on the right time.
        EventBuses.registerModEventBus(Characters.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        // Run our common setup.
        Characters.init();

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        // Register capability
        modEventBus.addListener(CapabilityEvents::register);
        ModNetworking.register();
        ComponentManager.INSTANCE=new ComponentsImpl();

    }
}
