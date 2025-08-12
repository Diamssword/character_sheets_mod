package com.diamssword.characters.client;

import com.diamssword.characters.Characters;
import com.diamssword.characters.mixins.EntityRenderDispatcherAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.EntityRenderers;

@Environment(EnvType.CLIENT)
public class CharactersClient {

	public static void reloadPlayerRender() {
		Characters.LOGGER.info("Reloading player models");
		MinecraftClient client=MinecraftClient.getInstance();
		EntityRendererFactory.Context context = new EntityRendererFactory.Context(client.getEntityRenderDispatcher(), client.getItemRenderer(), client.getBlockRenderManager(), client.getEntityRenderDispatcher().getHeldItemRenderer(), client.getResourceManager(), client.getEntityModelLoader(), client.textRenderer);
		((EntityRenderDispatcherAccessor) client.getEntityRenderDispatcher()).setModelRenderers(EntityRenderers.reloadPlayerRenderers(context));
	}
}
