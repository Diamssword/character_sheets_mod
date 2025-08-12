package com.diamssword.characters.client;

import com.diamssword.characters.Characters;
import dev.architectury.registry.client.level.entity.EntityModelLayerRegistry;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModelLayer;

public class Entities {

	public static final EntityModelLayer PLAYER_MODEL = new EntityModelLayer(Characters.asRessource("player"), "main");
	public static final EntityModelLayer PLAYER_MODEL_S = new EntityModelLayer(Characters.asRessource("player_slim"), "main");

	public static void init() {
		EntityModelLayerRegistry.register(PLAYER_MODEL, () -> TexturedModelData.of(CustomPlayerModel.getTexturedModelData(Dilation.NONE, false), 64, 64));
		EntityModelLayerRegistry.register(PLAYER_MODEL_S, () -> TexturedModelData.of(CustomPlayerModel.getTexturedModelData(Dilation.NONE, true), 64, 64));
	}
}