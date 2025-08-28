package com.diamssword.characters.client;

import com.diamssword.characters.Characters;
import com.diamssword.characters.api.PlayerPresence;
import com.diamssword.characters.client.gui.AddCharacterGui;
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
import java.util.function.Consumer;
import java.util.function.Function;

public class ClientComesticsPacket {
	public static Consumer<Map<UUID, PlayerPresence>> PlayerProfilesRequestCallback = null;
	public static Function<String, Screen> wardrobeGui=WardrobeGui::new;
	public static void init() {

		Channels.MAIN.registerClientbound(CosmeticsPackets.RefreshSkin.class, (message, access) -> {
			SkinsLoader.clientSkinCache.removeFromCache(message.player());
			SkinsLoader.instance.markReload(message.player(), true);
		});
		Channels.MAIN.registerClientbound(SkinServerCache.SendPlayerInfos.class, (msg, ctx) -> {
			SkinsLoader.clientSkinCache.addToCache(msg.player(), msg.skin(), msg.skinHead(), msg.slim());
			SkinsLoader.instance.markReload(msg.player(), true);
		});
		Channels.MAIN.registerClientbound(SkinServerCache.SendPlayerPresences.class, (msg, ctx) -> {
			msg.presences().forEach((k, v) -> SkinsLoader.instance.loadSkinHeadOffline(k, v.head()));
			if (PlayerProfilesRequestCallback != null)
				PlayerProfilesRequestCallback.accept(msg.presences());
			PlayerProfilesRequestCallback = null;
		});
		Channels.MAIN.registerClientbound(GuiPackets.WardRobePacket.class, (msg, ctx) -> {
			MinecraftClient.getInstance().setScreen(wardrobeGui.apply(msg.type()));
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
