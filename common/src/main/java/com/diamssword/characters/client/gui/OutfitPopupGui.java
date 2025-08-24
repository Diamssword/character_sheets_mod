package com.diamssword.characters.client.gui;

import com.diamssword.characters.Characters;
import com.diamssword.characters.api.ComponentManager;
import com.diamssword.characters.network.Channels;
import com.diamssword.characters.network.packets.CosmeticsPackets;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class OutfitPopupGui extends Screen {

    private final WardrobeGui parent;
    private final int index;
    private final ButtonWidget bt;
    public OutfitPopupGui(WardrobeGui parent, int index, ButtonWidget bt) {
        super(Text.literal(""));
        this.parent=parent;
        this.index=index;
        this.bt=bt;
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
                System.out.println(text.getText());
                Channels.MAIN.clientHandle().send(new CosmeticsPackets.SaveOutfit(text.getText(),index));
                ComponentManager.getPlayerDatas(client.player).getAppearence().saveOutfit(text.getText(),index);
                MinecraftClient.getInstance().setScreen(parent);
            }
        }).size(100,20).position(midX-110,midY+30).build());
        addDrawableChild(new ButtonWidget.Builder(Text.translatable(Characters.MOD_ID+".wardrobe.outfit_cancel"),(b)-> MinecraftClient.getInstance().setScreen(parent)).size(100,20).position(midX+10,midY+30).build());
    }

    public boolean shouldPause() {
        return false;
    }
}