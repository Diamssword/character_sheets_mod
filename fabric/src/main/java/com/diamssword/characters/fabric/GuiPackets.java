package com.diamssword.characters.fabric;

import com.diamssword.characters.network.Channels;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class GuiPackets {
	public record WardRobePacket() {}
	public static void init() {
		Channels.MAIN.registerClientboundDeferred(WardRobePacket.class);
	}
}
