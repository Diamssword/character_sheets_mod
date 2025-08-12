package com.diamssword.characters;

import com.diamssword.characters.client.ClientComesticsPacket;
import com.diamssword.characters.client.CharactersClient;
import com.diamssword.characters.client.Entities;
import com.diamssword.characters.commands.ClothCommand;
import com.diamssword.characters.commands.SkinCommand;
import com.diamssword.characters.config.Config;
import com.diamssword.characters.config.ConfigManager;
import com.diamssword.characters.network.Channels;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.platform.Platform;
import dev.architectury.registry.ReloadListenerRegistry;
import dev.architectury.utils.Env;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Consumer;

public final class Characters {
    public static final String MOD_ID = "character_sheet";

    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    public static Config config;
    public static void init() {
        ReloadListenerRegistry.register(ResourceType.SERVER_DATA,ClothingLoader.instance,ClothingLoader.instance.getId());
        config= ConfigManager.loadConfig();
        Channels.init();
        Events.init();
        if(Platform.getEnvironment()== Env.CLIENT)
            initClient();
        registerCommand("character", SkinCommand::register);
        registerCommand("cloth", ClothCommand::register);
    }
    @Environment(EnvType.CLIENT)
    public static void initClient()
    {
        ClientComesticsPacket.init();
        Entities.init();
    }
    public static Identifier asRessource(String name) {
        return new Identifier(MOD_ID, name);
    }
    public static void registerCommand(String name, Consumer<LiteralArgumentBuilder<ServerCommandSource>> builder) {
        LiteralArgumentBuilder<ServerCommandSource> l = CommandManager.literal(name);
        builder.accept(l);
        CommandRegistrationEvent.EVENT.register(((dispatcher, registryAccess, environment) -> dispatcher.register(l)));
    }
}
