package com.diamssword.characters.fabric.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Surface;
import io.wispforest.owo.ui.util.NinePatchTexture;
import net.minecraft.util.Identifier;

public class Panels {
	public static final Identifier PANEL_NINE_PATCH_TEXTURE = new Identifier("owo", "panel/resurgence");
	public static final Identifier OVERLAY_NINE_PATCH_TEXTURE = new Identifier("owo", "panel/r_overlay");
	public static Surface PANEL = (context, component) -> {
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		NinePatchTexture.draw(PANEL_NINE_PATCH_TEXTURE, context, component.x(), component.y(), component.width(), component.height());
		RenderSystem.disableBlend();
	};
	public static Surface PANEL_WHITE = (context, component) -> {
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		NinePatchTexture.draw(OVERLAY_NINE_PATCH_TEXTURE, context, component.x(), component.y(), component.width(), component.height());
		RenderSystem.disableBlend();
	};

	public static void drawOverlay(OwoUIDrawContext ctx, int x, int y, int width, int height) {
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		NinePatchTexture.draw(OVERLAY_NINE_PATCH_TEXTURE, ctx, x, y, width, height);
		RenderSystem.disableBlend();
	}

}
