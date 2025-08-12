package com.diamssword.characters.config;

import com.diamssword.characters.Characters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.architectury.platform.Platform;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = Platform.getConfigFolder().resolve(Characters.MOD_ID+".json");
    
    public static void saveConfig(Config config) {
        try {
            Files.writeString(CONFIG_PATH, GSON.toJson(config));
        } catch (IOException e) {
            Characters.LOGGER.error(e);
        }
    }
    
    public static Config loadConfig() {
        if (!Files.exists(CONFIG_PATH)) {
            Config defaultConfig = new Config();
            saveConfig(defaultConfig);
            return defaultConfig;
        }
        
        try {
            String json = Files.readString(CONFIG_PATH);
            return GSON.fromJson(json, Config.class);
        } catch (IOException e) {
            return new Config(); // Return default on error
        }
    }
}