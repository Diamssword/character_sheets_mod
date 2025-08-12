package com.diamssword.characters.fabric.client.gui;

import com.diamssword.characters.Characters;
import com.diamssword.characters.fabric.client.gui.components.RButtonComponent;
import com.diamssword.characters.network.Channels;
import com.diamssword.characters.network.packets.CosmeticsPackets;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.TextAreaComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class OutfitPopupGui extends BaseUIModelScreen<FlowLayout> {

    private final WardrobeGui parent;
    private final int index;
    private final RButtonComponent bt;
    public OutfitPopupGui(WardrobeGui parent, int index, RButtonComponent bt) {
        super(FlowLayout.class, DataSource.asset(Characters.asRessource("wardrobe_popup")));
        this.parent=parent;
        this.index=index;
        this.bt=bt;
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent.childById(RButtonComponent.class,"button").onPress(v->{
            var text=rootComponent.childById(TextAreaComponent.class,"text");
            if(!text.getText().isEmpty())
            {
                Channels.MAIN.clientHandle().send(new CosmeticsPackets.SaveOutfit(text.getText(),index));
                bt.tooltip(Text.literal(text.getText()));
                MinecraftClient.getInstance().setScreen(parent);
            }
        });
        rootComponent.childById(RButtonComponent.class,"cancel").onPress(v->{
            MinecraftClient.getInstance().setScreen(parent);
        });
    }

    public boolean shouldPause() {
        return false;
    }
}