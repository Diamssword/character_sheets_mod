package com.diamssword.characters.client;

import com.diamssword.characters.api.PlayerPresence;
import com.diamssword.characters.client.gui.WardrobeGui;
import com.diamssword.characters.network.Channels;
import com.diamssword.characters.network.SkinServerCache;
import com.diamssword.characters.network.packets.CosmeticsPackets;
import com.diamssword.characters.network.packets.GuiPackets;
import net.minecraft.client.MinecraftClient;

import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class ClientComesticsPacket {
	public static Consumer<Map<UUID, PlayerPresence>> PlayerProfilesRequestCallback = null;

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
			MinecraftClient.getInstance().setScreen(new WardrobeGui(msg.type()));
		});
	}

}
