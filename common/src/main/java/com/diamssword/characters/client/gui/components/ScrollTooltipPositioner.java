package com.diamssword.characters.client.gui.components;

import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector2i;
import org.joml.Vector2ic;

public class ScrollTooltipPositioner implements TooltipPositioner {

	public ScrollTooltipPositioner() {
	}

	@Override
	public Vector2ic getPosition(int screenWidth, int screenHeight, int x, int y, int width, int height) {

		Vector2i vector2i = new Vector2i(x + 12, y);
		if (vector2i.x + width > screenWidth - 5) {
			vector2i.x = Math.max(x - 12 - width, 9);
		}


		return vector2i;
	}
}
