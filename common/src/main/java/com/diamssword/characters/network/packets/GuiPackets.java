package com.diamssword.characters.network.packets;

import com.diamssword.characters.Characters;
import com.diamssword.characters.commands.SkinCommand;
import com.diamssword.characters.network.Channels;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GuiPackets {
	public static Map<ServerPlayerEntity,String> openedEdit=new ConcurrentHashMap<>();
	public record WardRobePacket(String type) {}
	public record BodyGuiPacket(String saveId,String... allowedLayers) {}
	public record ImportGuiPacket(String status) {}
	public record GuiPacketCode(String code) {}
	public static void init() {
		Channels.MAIN.registerClientboundDeferred(WardRobePacket.class);
		Channels.MAIN.registerClientboundDeferred(BodyGuiPacket.class);
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
	public static boolean openEditGui(Collection<ServerPlayerEntity> players, @Nullable String guiId)
	{
		if(guiId ==null)
		{
			players.forEach(p->{
				openedEdit.put(p,"default");
			});
			Channels.MAIN.serverHandle(players).send(new GuiPackets.BodyGuiPacket("default"));
			return true;
		}
		else {
			var c = Characters.config.serverOptions.findGuiConfig(guiId);
			if(c != null) {
				players.forEach(p->{
					openedEdit.put(p,c.id());
				});
				Channels.MAIN.serverHandle(players).send(new GuiPackets.BodyGuiPacket(c.id(), c.allowedLayers()));
				return true;
			}
		}
		return false;
	}
}
