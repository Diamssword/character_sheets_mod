package com.diamssword.characters;

import com.diamssword.characters.api.CharactersApi;
import com.diamssword.characters.api.ComponentManager;
import com.diamssword.characters.client.ClientComesticsPacket;
import com.diamssword.characters.client.Entities;
import com.diamssword.characters.commands.ClothCommand;
import com.diamssword.characters.commands.PStatsCommand;
import com.diamssword.characters.commands.SkinCommand;
import com.diamssword.characters.commands.WardrobeCommand;
import com.diamssword.characters.config.Config;
import com.diamssword.characters.config.ConfigManager;
import com.diamssword.characters.implementations.CharactersApiImpl;
import com.diamssword.characters.network.Channels;
import com.diamssword.characters.storage.*;
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
        ReloadListenerRegistry.register(ResourceType.SERVER_DATA,ClothingLoader.instance, ClothingLoader.instance.getId());
        ReloadListenerRegistry.register(ResourceType.SERVER_DATA,ClassesLoader.instance, ClassesLoader.instance.getId());
        ClassesLoader.initEvents();
        CharactersApi.instance=new CharactersApiImpl();

        PlayerCharacters.attachComponentToCharacters(CharactersApi.CHARACTER_ATTACHED_COMPONENT_APPEARANCE, (p)-> ComponentManager.getPlayerDatas(p).getAppearence(), PlayerAppearance::serializer,PlayerAppearance::unserializer);
        PlayerCharacters.attachComponentToCharacters(CharactersApi.CHARACTER_ATTACHED_COMPONENT_INVENTORY, InventorySaver::new,InventorySaver::serializer,InventorySaver::unserializer);
        PlayerCharacters.attachComponentToCharacters(CharactersApi.CHARACTER_ATTACHED_COMPONENT_STATS, (p)-> ComponentManager.getPlayerDatas(p).getStats(), PlayerStats::serializer,PlayerStats::unserializer);

        config= ConfigManager.loadConfig();
        Channels.init();
        Events.init();
        if(Platform.getEnvironment()== Env.CLIENT)
            initClient();
        registerCommand("character", SkinCommand::register);
        registerCommand("cloths", ClothCommand::register);
        registerCommand("playerskills", PStatsCommand::register);
        registerCommand("wardrobe", WardrobeCommand::register);
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
