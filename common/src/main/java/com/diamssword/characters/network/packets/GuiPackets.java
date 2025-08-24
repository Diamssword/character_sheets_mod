package com.diamssword.characters.network.packets;

import com.diamssword.characters.network.Channels;

public class GuiPackets {
	public record WardRobePacket(String type) {}
	public static void init() {
		Channels.MAIN.registerClientboundDeferred(WardRobePacket.class);
	}
}
