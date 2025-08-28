package com.diamssword.characters.commands;

import com.diamssword.characters.Characters;
import com.diamssword.characters.api.http.ApiCharacterValues;
import com.diamssword.characters.network.packets.GuiPackets;
import com.diamssword.characters.storage.ClothingLoader;
import com.diamssword.characters.storage.PlayerAppearance;
import com.diamssword.characters.http.APIService;
import com.diamssword.characters.network.Channels;
import com.diamssword.characters.network.packets.CosmeticsPackets;
import com.diamssword.characters.api.ComponentManager;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class SkinCommand {

	public static Map<PlayerEntity,Boolean> waitingPlayers=new HashMap<>();
	private static final SuggestionProvider<ServerCommandSource> SUGGESTION_PROVIDER = (context, builder) -> {
		if (context.getSource().isExecutedByPlayer()) {
			var set = ComponentManager.getPlayerCharacter(context.getSource().getPlayer()).getCharactersNames();
			return CommandSource.suggestMatching(set, builder);
		}
		return CommandSource.suggestMatching(new String[0], builder);

	};

	public static void register(LiteralArgumentBuilder<ServerCommandSource> builder) {
		var root = builder.requires(ctx -> ctx.hasPermissionLevel(2));
		var add = CommandManager.literal("add").then(CommandManager.argument("code", StringArgumentType.string()).executes(SkinCommand::createExec1).then(CommandManager.argument("player", EntityArgumentType.player()).executes(SkinCommand::createExec1)));
		var replace = CommandManager.literal("replace").then(CommandManager.argument("character", StringArgumentType.string()).suggests(SUGGESTION_PROVIDER).then(CommandManager.argument("code", StringArgumentType.string()).executes(SkinCommand::replaceExec).then(CommandManager.argument("player", EntityArgumentType.player()).executes(SkinCommand::replaceExec))));
		var switc = CommandManager.literal("switch").then(CommandManager.argument("character", StringArgumentType.string()).suggests(SUGGESTION_PROVIDER).executes(SkinCommand::switchExec).then(CommandManager.argument("player", EntityArgumentType.player()).executes(SkinCommand::switchExec)));
		var remov = CommandManager.literal("delete").then(CommandManager.argument("character", StringArgumentType.string()).suggests(SUGGESTION_PROVIDER).executes(SkinCommand::removeExec).then(CommandManager.argument("player", EntityArgumentType.player()).executes(SkinCommand::removeExec)));
		var gui = CommandManager.literal("gui").then(CommandManager.literal("replace").then(CommandManager.argument("player", EntityArgumentType.players()).executes(e -> guiExec(e, false)))).then(CommandManager.literal("add").then(CommandManager.argument("player", EntityArgumentType.players()).executes(e -> guiExec(e, true))));
		root.then(add);
		root.then(replace);
		root.then(switc);
		root.then(remov);
		root.then(gui);
	}

	private static int guiExec(CommandContext<ServerCommandSource> ctx,boolean add) throws CommandSyntaxException {
		var entities = EntityArgumentType.getPlayers(ctx, "player");
		entities.forEach(e->waitingPlayers.put(e,add));
		Channels.MAIN.serverHandle(entities).send(new GuiPackets.ImportGuiPacket(add?"add":"replace"));
		return 1;
	}
	private static int switchExec(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		var entity = ctx.getSource().getPlayer();
		try {
			entity = EntityArgumentType.getPlayer(ctx, "player");
		} catch (IllegalArgumentException ignored) {
		}
		String sub = StringArgumentType.getString(ctx, "character");
		if (entity != null) {
			var chs =ComponentManager.getPlayerCharacter(entity);
			if (chs.getCharactersNames().contains(sub)) {
				if(sub.equals(chs.getCurrentCharacterID()))
				{
					ctx.getSource().sendFeedback(() -> Text.literal("This character is already  the current character: " + chs.getCurrentCharacterID()), true);
					return 1;
				}
				chs.switchCharacter(sub);
				if(ComponentManager.getPlayerDatas(entity).getAppearence() instanceof PlayerAppearance ap)
					ap.refreshSkinData();
				Channels.MAIN.serverHandle(ctx.getSource().getServer()).send(new CosmeticsPackets.RefreshSkin(entity.getGameProfile().getId()));
				ctx.getSource().sendFeedback(() -> Text.literal("Character applied: " + chs.getCurrentCharacterID()), true);
				return 1;
			} else
				ctx.getSource().sendFeedback(() -> Text.literal("No character found: " + sub), true);
		}
		return -1;
	}

	private static int removeExec(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		var entity = ctx.getSource().getPlayer();
		try {
			entity = EntityArgumentType.getPlayer(ctx, "player");
		} catch (IllegalArgumentException ignored) {
		}
		String sub = StringArgumentType.getString(ctx, "character");
		if (entity != null) {
			var chs = ComponentManager.getPlayerCharacter(entity);
			if (chs.getCharactersNames().contains(sub)) {
				chs.deleteCharacter(sub);
				ctx.getSource().sendFeedback(() -> Text.literal("Character ereased: " + sub), true);
				return 1;
			} else
				ctx.getSource().sendFeedback(() -> Text.literal("No character found: " + sub), true);
		}
		return -1;
	}

	private static int createExec1(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		var entity = ctx.getSource().getPlayer();
		try {
			entity = EntityArgumentType.getPlayer(ctx, "player");
		} catch (IllegalArgumentException ignored) {
		}
		String sub = StringArgumentType.getString(ctx, "code");
		if (entity != null) {

			ServerPlayerEntity finalEntity1 = entity;
			addCharacter(entity,sub).thenAccept(a->{
					if(a) {
						var chs = ComponentManager.getPlayerCharacter(finalEntity1);
						ctx.getSource().sendFeedback(() -> Text.literal("New character successfully applied: " + chs.getCurrentCharacterID()), true);
					}
					else
						ctx.getSource().sendFeedback(() -> Text.literal("Error, please try again"), true);
					});
			return 1;
		}
		return 0;
	}
	private static void setNewProfileDatas(ApiCharacterValues character, PlayerEntity player)
	{
		var dts=ComponentManager.getPlayerDatas(player);
		character.stats.points.forEach((k,v)-> dts.getStats().setLevel(k,v));
		character.appearance.additional.forEach((k, v)->{
			var ind=v.indexOf(':');
			var path=v;
			String space=Characters.MOD_ID;
			if(ind>-1)
			{
				space=v.substring(0,ind);
				path=v.substring(ind+1);
			}
			var cloth=ClothingLoader.instance.getCloth(new Identifier(space,k+"/"+path));
			cloth.ifPresent(c->{
				if(c.layer().isBaseLayer())
					dts.getAppearence().setCloth(c);
			});

		});
	}
	private static int replaceExec(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		var entity = ctx.getSource().getPlayer();
		try {
			entity = EntityArgumentType.getPlayer(ctx, "player");
		} catch (IllegalArgumentException ignored) {
		}
		String sub = StringArgumentType.getString(ctx, "code");
		String chara = StringArgumentType.getString(ctx, "character");
		if (entity != null) {
			ServerPlayerEntity finalEntity = entity;
			var chs = ComponentManager.getPlayerCharacter(entity);
			if (chs.getCharactersNames().contains(chara)) {
				APIService.importCharacter(entity, sub).handle((b, t) -> {
					if (t != null)
						t.printStackTrace();
					if (b.isPresent()) {
						chs.replaceCharacter(chara, b.get());
						chs.switchCharacter(chara);
						setNewProfileDatas(b.get(),finalEntity);
						if(ComponentManager.getPlayerDatas(finalEntity).getAppearence() instanceof PlayerAppearance ap)
							ap.refreshSkinData();
						Channels.MAIN.serverHandle(ctx.getSource().getServer()).send(new CosmeticsPackets.RefreshSkin(finalEntity.getGameProfile().getId()));
						ctx.getSource().sendFeedback(() -> Text.literal("Character replaced successfully: " + chs.getCurrentCharacterID()), true);
						return 1;
					} else {
						ctx.getSource().sendFeedback(() -> Text.literal("Error, please try again"), true);
						return -1;
					}
				});
				return 1;
			}
			ctx.getSource().sendFeedback(() -> Text.literal("Aucun personnage trouvé: " + chara), true);
			return -1;
		}
		return 0;
	}
	public static CompletableFuture<Boolean> addCharacter(ServerPlayerEntity player, String code)
	{
		return APIService.importCharacter(player, code).handle((b, t) -> {
			if (t != null)
				t.printStackTrace();
			if (b.isPresent()) {
				var chs = ComponentManager.getPlayerCharacter(player);
				chs.switchCharacter(chs.addNewCharacter(b.get()));
				setNewProfileDatas(b.get(),player);
				if(ComponentManager.getPlayerDatas(player).getAppearence() instanceof PlayerAppearance ap)
					ap.refreshSkinData();
				Channels.MAIN.serverHandle(player.getServer()).send(new CosmeticsPackets.RefreshSkin(player.getGameProfile().getId()));
				return true;
			} else {
				return false;
			}
		});
	}
	public static CompletableFuture<Boolean> replaceCharacter(ServerPlayerEntity player, String code)
	{
		return APIService.importCharacter(player, code).handle((b, t) -> {
			if (t != null)
				t.printStackTrace();
			if (b.isPresent()) {
				var chs = ComponentManager.getPlayerCharacter(player);
				var id=chs.getCurrentCharacterID();
				if(id !=null)
					chs.replaceCharacter(id, b.get());
				else
					id=chs.addNewCharacter(b.get());
				chs.switchCharacter(id);
				setNewProfileDatas(b.get(),player);
				if(ComponentManager.getPlayerDatas(player).getAppearence() instanceof PlayerAppearance ap)
					ap.refreshSkinData();
				Channels.MAIN.serverHandle(player.getServer()).send(new CosmeticsPackets.RefreshSkin(player.getGameProfile().getId()));
				return true;
			} else {
				return false;
			}
		});
	}

}
