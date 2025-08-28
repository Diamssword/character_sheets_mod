package com.diamssword.characters.client.gui;

import com.diamssword.characters.Characters;
import com.diamssword.characters.api.ComponentManager;
import com.diamssword.characters.api.appearence.Cloth;
import com.diamssword.characters.api.appearence.LayerDef;
import com.diamssword.characters.client.gui.components.*;
import com.diamssword.characters.network.Channels;
import com.diamssword.characters.network.packets.CosmeticsPackets;
import com.diamssword.characters.network.packets.GuiPackets;
import com.diamssword.characters.storage.ClothingLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class AddCharacterGui extends Screen {
	private boolean replace;
	private int errorTimer=0;
	public AddCharacterGui(boolean replace) {
		super(Text.literal(""));
		this.replace=replace;
	}

	@Override
	public void tick() {
		super.tick();
		if(errorTimer>0)
			errorTimer--;
	}

	public void onError() {
		errorTimer=220;
	}
	@Override
	protected void init() {
		super.init();
		var manager=ComponentManager.getPlayerCharacter(client.player);
		if(replace)
			replace = manager.getCurrentCharacterID() !=null;
		int midX=this.width/2;
		int midY=this.height/2;

		var search=new TextFieldWidget(client.textRenderer,midX-55,midY-10 ,60,18,Text.translatable(Characters.MOD_ID+".wardrobe.search"));
		addDrawableChild(ButtonWidget.builder(Text.translatable(Characters.MOD_ID+".add_gui.paste"),(e)->{
					String s=client.keyboard.getClipboard();
					if(s.length()<=6)
						search.setText(s);
				})
				.size(50,20).position(midX+8,midY-11).build());
		search.setPlaceholder(Text.translatable(Characters.MOD_ID+".add_gui.code"));
		search.setTextPredicate(s->s.length()<=6);
		addDrawableChild(search);
		this.setFocused(search);

		var bt=addDrawableChild(ButtonWidget.builder(Text.translatable(Characters.MOD_ID+".add_gui.accept"),(e)->{
			Channels.MAIN.clientHandle().send(new GuiPackets.GuiPacketCode(search.getText()));
				})
				.size(100,20).position(midX-50,midY+20).build());
		search.setChangedListener(l->bt.active=l.length()>5);
		bt.active=false;
	}
	public boolean shouldPause() {
		return false;
	}
	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context,mouseX,mouseY,delta);
		drawMultiLineText(context,Text.translatable(Characters.MOD_ID+".add_gui.title."+(replace?"replace":"add")), this.width / 2 - 60, this.height/2 -70, 10526880);
		drawMultiLineText(context, Text.translatable(Characters.MOD_ID+".add_gui.desc"), this.width / 2 - 60, this.height/2 -55, 10526880);
		if(replace)
			context.drawTextWithShadow(this.textRenderer, Text.translatable(Characters.MOD_ID+".add_gui.desc.replace"), this.width / 2 - 60, this.height/2 -30, 10526880);
		if(errorTimer>0)
		{
			int i=0;
			if(errorTimer>210)
				i=errorTimer%2;
			drawMultiLineText(context, Text.translatable(Characters.MOD_ID+".add_gui.error"), this.width / 2 - 60 -i, this.height/2 +50+i, 0xFFd60606);
		}

	}
	public void drawMultiLineText(DrawContext context,  Text text, int x, int y, int color) {
		String[] lines = text.getString().split("\n");
		int lineHeight = textRenderer.fontHeight + 2; // Add some spacing between lines

		for (int i = 0; i < lines.length; i++) {
			context.drawTextWithShadow(textRenderer, lines[i], x, y + (i * lineHeight), color);
		}
	}

}
