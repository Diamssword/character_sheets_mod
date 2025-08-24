package com.diamssword.characters.fabric;

import com.diamssword.characters.api.ComponentManager;
import net.fabricmc.api.ModInitializer;

import com.diamssword.characters.Characters;

public final class CharactersFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        Characters.init();
        ComponentManager.INSTANCE=new ComponentsImpl();
    }
}
