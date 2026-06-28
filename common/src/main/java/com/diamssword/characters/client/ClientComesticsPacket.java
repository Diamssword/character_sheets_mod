package com.diamssword.characters.client;

import com.diamssword.characters.Characters;
import com.diamssword.characters.Utils;
import com.diamssword.characters.api.ComponentManager;
import com.diamssword.characters.api.PlayerSkinInfos;
import com.diamssword.characters.client.gui.AddCharacterGui;
import com.diamssword.characters.client.gui.BodyPartEditorGui;
import com.diamssword.characters.client.gui.WardrobeGui;
import com.diamssword.characters.network.Channels;
import com.diamssword.characters.network.SkinServerCache;
import com.diamssword.characters.network.packets.CosmeticsPackets;
import com.diamssword.characters.network.packets.GuiPackets;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class ClientComesticsPacket {
	public static Consumer<Map<UUID, PlayerSkinInfos>> PlayerProfilesRequestCallback = null;
	public static Function<String, Screen> wardrobeGui=WardrobeGui::new;
	public static BiFunction<String,String[], Screen> bodyGui= BodyPartEditorGui::new;
	public static void init() {
		Utils.skinServerCacheSupplier=(s)->SkinsLoader.clientSkinCache;
		Channels.MAIN.registerClientbound(CosmeticsPackets.RefreshSkin.class, (message, access) -> {
			SkinsLoader.clientSkinCache.removeFromCache(message.player());
			SkinsLoader.instance.markReload(message.player(), true,false);
		});
		Channels.MAIN.registerClientbound(SkinServerCache.SendPlayerInfos.class, (msg, ctx) -> {
			SkinsLoader.clientSkinCache.setActiveCharacter(msg.player(),msg.infos().username(), msg.infos().characterName(),msg.infos().layers(),msg.infos().slim());
			SkinsLoader.instance.markReload(msg.player(), true,false);
		});
		Channels.MAIN.registerClientbound(SkinServerCache.SendPlayerMatchInfos.class, (msg, ctx) -> {
			if (PlayerProfilesRequestCallback != null)
				PlayerProfilesRequestCallback.accept(msg.players());
			PlayerProfilesRequestCallback = null;
		});
		Channels.MAIN.registerClientbound(GuiPackets.WardRobePacket.class, (msg, ctx) -> {
			MinecraftClient.getInstance().setScreen(wardrobeGui.apply(msg.type()));
		});
		Channels.MAIN.registerClientbound(GuiPackets.BodyGuiPacket.class, (msg, ctx) -> {
			MinecraftClient.getInstance().setScreen(bodyGui.apply(msg.saveId(),msg.allowedLayers()));
		});

		Channels.MAIN.registerClientbound(CosmeticsPackets.SaveLayersClient.class,(msg, ctx)->{
			ComponentManager.getPlayerDatas(ctx.player()).getAppearence().saveLayers(msg.guid(), msg.name(), msg.index(),msg.layers());
		});
		Channels.MAIN.registerClientbound(GuiPackets.ImportGuiPacket.class, (msg, ctx) -> {
			if(msg.status().equals("add")||msg.status().equals("replace"))
				MinecraftClient.getInstance().setScreen(new AddCharacterGui(msg.status().equals("replace")));
			else if(MinecraftClient.getInstance().currentScreen instanceof AddCharacterGui gui)
			{
				if(msg.status().equals("complete")) {
					gui.close();
					ctx.player().sendMessage(Text.translatable(Characters.MOD_ID+".add_gui.success"));
				}
				else
				{
					gui.onError();
				}
			}



		});
	}

}
