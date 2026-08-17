package com.diamssword.characters.network.packets;

import com.diamssword.characters.Characters;
import com.diamssword.characters.Utils;
import com.diamssword.characters.api.CharactersApi;
import com.diamssword.characters.api.PlayerSkinInfos;
import com.diamssword.characters.api.http.SkinLayerValue;
import com.diamssword.characters.api.skin.BodyLayerInfo;
import com.diamssword.characters.network.SkinServerCache;
import com.diamssword.characters.storage.ClothingLoader;
import com.diamssword.characters.network.Channels;
import com.diamssword.characters.api.ComponentManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CosmeticsPackets {
    public record EquipCloth(Identifier clothID, @Nullable String layerID){};
    public record EquipOutfit(int index){};
    public record RefreshSkin(UUID player){};
    public record SaveOutfit(String name,int index){};
	public record AskForPlayerRefresh(){}

	public record ApplySkinChange(SkinLayerValue[] layers){};
	public record SaveLayers(String name,int index,SkinLayerValue[] layers){};
	public record SaveLayersClient(String guid,String name,int index,SkinLayerValue[] layers){};

    public static void init()
    {
        Channels.MAIN.registerClientboundDeferred(RefreshSkin.class);
		Channels.MAIN.registerClientboundDeferred(SaveLayersClient.class);
        Channels.MAIN.registerServerbound(EquipCloth.class,(msg, ctx)->{
            if(!msg.clothID.getNamespace().equals("null"))
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
		Channels.MAIN.registerServerbound(AskForPlayerRefresh.class,(msg, ctx)->{
			ctx.player().getWorld().getPlayers().forEach(ComponentManager::syncPlayerDatas);


		});
        Channels.MAIN.registerServerbound(EquipOutfit.class,(msg, ctx)->{
            ComponentManager.getPlayerDatas(ctx.player()).getAppearence().equipOutfit(msg.index);
        });
        Channels.MAIN.registerServerbound(SaveOutfit.class,(msg, ctx)->{
            ComponentManager.getPlayerDatas(ctx.player()).getAppearence().saveOutfit(msg.name,msg.index);
        });
		Channels.MAIN.registerServerbound(SaveLayers.class,(msg, ctx)->{
			var gId=GuiPackets.openedEdit.get(ctx.player());
			if(gId!=null) {
				var allowed = Characters.config.serverOptions.findGuiConfig(gId);
				List<String> ls = List.of();
				if(allowed != null)
					ls = List.of(allowed.allowedLayers());
				var lays=flattenMap(filterAllowedLayers(msg.layers, ls));
				ComponentManager.getPlayerDatas(ctx.player()).getAppearence().saveLayers(gId, msg.name, msg.index, lays);
				Channels.MAIN.serverHandle(ctx.player()).send(new SaveLayersClient(gId,msg.name,msg.index,lays));
			}
		});
		Channels.MAIN.registerServerbound(ApplySkinChange.class,(msg, ctx)->{
			var gId=GuiPackets.openedEdit.get(ctx.player());
			if(gId !=null) {
				var allowedLayers=new ArrayList<String>();
				var allowed=Characters.config.serverOptions.findGuiConfig(gId);
				if(allowed!=null)
					allowedLayers.addAll(List.of(allowed.allowedLayers()));
				var cha = ComponentManager.getPlayerCharacter(ctx.player());
				var result = mixandMatchLayers(ctx.player(), msg.layers,allowedLayers);
				cha.getCurrentCharacter().appearance.layers = result;
				ComponentManager.getPlayerDatas(ctx.player()).getAppearence().getSkinDatas().layers = result;
				SkinServerCache.get(ctx.player().getServer()).setActiveCharacter(ctx.player(), cha.getCurrentCharacter().stats.firstname + " " + cha.getCurrentCharacter().stats.lastname, cha.getCurrentCharacter().appearance.layers, cha.getCurrentCharacter().appearance.slim);
				Channels.MAIN.serverHandle(ctx.player().getServer()).send(new CosmeticsPackets.RefreshSkin(ctx.player().getGameProfile().getId()));
				GuiPackets.openedEdit.remove(ctx.player());
			}
		});
    }
	public static Map<BodyLayerInfo, List<SkinLayerValue>> filterAllowedLayers(SkinLayerValue[] inputs, List<String> allowedLayers)
	{
		var map = new HashMap<BodyLayerInfo, List<SkinLayerValue>>();
		for(SkinLayerValue layer :inputs) {
			if(allowedLayers.isEmpty() || allowedLayers.contains(layer.layer)) {
				CharactersApi.bodyParts().getBodyLayer(layer.layer).ifPresent(b -> {
					if(!b.external()) {
						var ls = map.computeIfAbsent(b, c -> new ArrayList<>());
						if((b.multi() || ls.isEmpty() || (ls.size() == 1 && b.splited())) && ls.size() < 30) {
							ls.add(layer);
						}
					}
				});
			}
		}
		return map;
	}
	public static SkinLayerValue[] flattenMap(Map<BodyLayerInfo, List<SkinLayerValue>> map)
	{
		return map.entrySet()
				.stream()
				.sorted((a, b) -> (int) ((a.getKey().size() * 100000) - (int) (b.getKey().size() * 100000)))
				.flatMap(entry -> entry.getValue().stream())
				.toArray(SkinLayerValue[]::new);
	}
	public static SkinLayerValue[] mixandMatchLayers(PlayerEntity player, SkinLayerValue[] inputs, List<String> allowedLayers)
	{
		var map=filterAllowedLayers(inputs,allowedLayers);
		if(!allowedLayers.isEmpty()) {
			var playerLayers = Utils.getSkinServerCacheSideSafe(player).map(PlayerSkinInfos::layers).orElse(new SkinLayerValue[0]);

			for(SkinLayerValue layer :playerLayers) {
				if(!allowedLayers.contains(layer.layer)) {
					CharactersApi.bodyParts().getBodyLayer(layer.layer).ifPresent(b -> {
						if(!b.external()) {
							var ls = map.computeIfAbsent(b, c -> new ArrayList<>());
							if((b.multi() || ls.isEmpty() || (ls.size() == 1 && b.splited())) && ls.size() < 30) {
								ls.add(layer);
							}
						}
					});
				}
			}
		}
		return flattenMap(map);
	}
}
