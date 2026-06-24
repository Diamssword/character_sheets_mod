package com.diamssword.characters.client.gui;

import com.diamssword.characters.Characters;
import com.diamssword.characters.api.ComponentManager;
import com.diamssword.characters.api.http.SkinLayerValue;
import com.diamssword.characters.network.Channels;
import com.diamssword.characters.network.packets.CosmeticsPackets;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class LayerPopupGui extends Screen {

    private final int index;
	private final String guiId;
	private final SkinLayerValue[] layers;
	private final String[] allowedLayers;
    public LayerPopupGui(String[] allowedLayers,String guiId, int index, SkinLayerValue[] layers) {
        super(Text.literal(""));
        this.allowedLayers=allowedLayers;
        this.index=index;
		this.layers=layers;
		this.guiId=guiId;
    }

    @Override
    protected void init() {
        int midX=width/2;
        int midY=height/2;
        var text=new TextFieldWidget(client.textRenderer,midX-(width/4), midY,width/2,20,Text.translatable(Characters.MOD_ID+".wardrobe.outfit_name"));
        text.setPlaceholder(Text.translatable(Characters.MOD_ID+".wardrobe.outfit_name"));
        addDrawableChild(text);
        addDrawableChild(new ButtonWidget.Builder(Text.translatable(Characters.MOD_ID+".wardrobe.outfit_create"),(b)->{
            if(!text.getText().isEmpty())
            {
				ComponentManager.getPlayerDatas(client.player).getAppearence().saveLayers(guiId,text.getText(),index,new SkinLayerValue[0]);
				Channels.MAIN.clientHandle().send(new CosmeticsPackets.SaveLayers(text.getText(),index,layers));
				MinecraftClient.getInstance().setScreen(new BodyPartEditorGui(layers,this.guiId,allowedLayers));
            }
        }).size(100,20).position(midX-110,midY+30).build());
        addDrawableChild(new ButtonWidget.Builder(Text.translatable(Characters.MOD_ID+".wardrobe.outfit_cancel"),(b)-> MinecraftClient.getInstance().setScreen(new BodyPartEditorGui(this.guiId,allowedLayers))).size(100,20).position(midX+10,midY+30).build());
    }

    public boolean shouldPause() {
        return false;
    }
}