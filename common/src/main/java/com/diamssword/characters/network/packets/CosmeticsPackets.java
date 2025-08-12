package com.diamssword.characters.network.packets;

import com.diamssword.characters.ClothingLoader;
import com.diamssword.characters.network.Channels;
import com.diamssword.characters.storage.ComponentManager;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class CosmeticsPackets {
    public record EquipCloth( String clothID, @Nullable String layerID){};
    public record EquipOutfit(int index){};
    public record RefreshSkin(UUID player){};
    public record SaveOutfit(String name,int index){};
    public static void init()
    {
        Channels.MAIN.registerClientboundDeferred(RefreshSkin.class);
        Channels.MAIN.registerServerbound(EquipCloth.class,(msg, ctx)->{
            if(!msg.clothID.equals("null"))
            {
                var c= ClothingLoader.instance.getCloth(msg.clothID);
                if(ctx.player().isCreative())
                    c.ifPresent(cloth -> ComponentManager.getPlayerDatas(ctx.player()).getAppearence().setCloth(cloth));
                else
                    c.ifPresent(cloth ->ComponentManager.getPlayerDatas(ctx.player()).getAppearence().equipCloth(cloth));
            }
            else if(msg.layerID !=null)
            {
                    ComponentManager.getPlayerDatas(ctx.player()).getAppearence().removeCloth(msg.layerID);
            }

        });
        Channels.MAIN.registerServerbound(EquipOutfit.class,(msg, ctx)->{
            ComponentManager.getPlayerDatas(ctx.player()).getAppearence().equipOutfit(msg.index);
        });
        Channels.MAIN.registerServerbound(SaveOutfit.class,(msg, ctx)->{
            ComponentManager.getPlayerDatas(ctx.player()).getAppearence().saveOutfit(msg.name,msg.index);
        });
    }
}
