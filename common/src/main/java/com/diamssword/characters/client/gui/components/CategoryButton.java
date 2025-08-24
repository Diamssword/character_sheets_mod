package com.diamssword.characters.client.gui.components;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.IconButtonWidget;


import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class CategoryButton extends ButtonWidget {
	protected final Identifier iconTexture;
	protected boolean activated=false;
	public final static int SIZE=20;
	public final static int TSIZE=32;

	public CategoryButton(Text message, Identifier iconTexture, ButtonWidget.PressAction onPress) {
		super(0, 0, SIZE, SIZE, message, onPress, DEFAULT_NARRATION_SUPPLIER);

		this.iconTexture = iconTexture;
	}

	public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
		super.renderButton(context, mouseX, mouseY, delta);
		context.drawTexture( this.iconTexture,this.getIconX()+2, this.getIconY()+2,SIZE-4,SIZE-4,0,0,TSIZE,TSIZE,TSIZE,TSIZE);
	}

	public void drawMessage(DrawContext context, TextRenderer textRenderer, int color) {
		int i = this.getX() + 2;
		int j = this.getX() + this.getWidth() - TSIZE - 6;
		drawScrollableText(context, textRenderer, this.getMessage(), i, this.getY(), j, this.getY() + this.getHeight(), color);
	}

	private int getIconX() {
		return this.getX() + (this.width / 2 - SIZE / 2);
	}

	private int getIconY() {
		return this.getY();
	}

	public static net.minecraft.client.gui.widget.IconButtonWidget.Builder builder(Text message, Identifier texture, ButtonWidget.PressAction pressAction) {
		return new net.minecraft.client.gui.widget.IconButtonWidget.Builder(message, texture, pressAction);
	}
	@Override
	public boolean isSelected() {
		return this.activated || this.isFocused();
	}

	public void setActivated(boolean b) {
		activated=b;
	}
}
