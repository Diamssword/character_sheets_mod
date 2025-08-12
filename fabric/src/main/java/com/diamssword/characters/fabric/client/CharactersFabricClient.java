package com.diamssword.characters.fabric.client;

import com.diamssword.characters.fabric.GuiPackets;
import com.diamssword.characters.fabric.client.gui.WardrobeGui;
import com.diamssword.characters.fabric.client.gui.components.ComponentsRegister;
import com.diamssword.characters.network.Channels;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;

public final class CharactersFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.
        Channels.MAIN.registerClientbound(GuiPackets.WardRobePacket.class,(p,c)->{
            MinecraftClient.getInstance().setScreen(new WardrobeGui());
        });
        ComponentsRegister.init();
    }
}
