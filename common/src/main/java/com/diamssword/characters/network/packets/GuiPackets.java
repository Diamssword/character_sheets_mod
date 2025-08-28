package com.diamssword.characters.network.packets;

import com.diamssword.characters.commands.SkinCommand;
import com.diamssword.characters.network.Channels;
import net.minecraft.server.command.CommandManager;

public class GuiPackets {
	public record WardRobePacket(String type) {}
	public record ImportGuiPacket(String status) {}
	public record GuiPacketCode(String code) {}
	public static void init() {
		Channels.MAIN.registerClientboundDeferred(WardRobePacket.class);
		Channels.MAIN.registerClientboundDeferred(ImportGuiPacket.class);
		Channels.MAIN.registerServerbound(GuiPacketCode.class,(msg,ctx)->{
			var b=SkinCommand.waitingPlayers.get(ctx.player());
			if(b !=null)
			{
				if(b)
				{
					SkinCommand.addCharacter(ctx.player(),msg.code).thenAccept(ok->{
						if(ok) {
							Channels.MAIN.serverHandle(ctx.player()).send(new ImportGuiPacket("complete"));
							SkinCommand.waitingPlayers.remove(ctx.player());
						}
						else
							Channels.MAIN.serverHandle(ctx.player()).send(new ImportGuiPacket("error"));

					});
				}
				else
				{
					SkinCommand.replaceCharacter(ctx.player(),msg.code).thenAccept(ok->{
						if(ok) {
							Channels.MAIN.serverHandle(ctx.player()).send(new ImportGuiPacket("complete"));
							SkinCommand.waitingPlayers.remove(ctx.player());
						}
						else
							Channels.MAIN.serverHandle(ctx.player()).send(new ImportGuiPacket("error"));

					});
				}
			}
		});
	}
}
