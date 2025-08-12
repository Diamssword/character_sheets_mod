package com.diamssword.characters.commands;

import com.diamssword.characters.http.APIService;
import com.diamssword.characters.network.Channels;
import com.diamssword.characters.network.packets.CosmeticsPackets;
import com.diamssword.characters.storage.ComponentManager;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;

public class SkinCommand {
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
		root.then(add);
		root.then(replace);
		root.then(switc);
		root.then(remov);
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
				chs.switchCharacter(sub);
				ComponentManager.getPlayerDatas(entity).getAppearence().refreshSkinData();
				Channels.MAIN.serverHandle(ctx.getSource().getServer()).send(new CosmeticsPackets.RefreshSkin(entity.getGameProfile().getId()));
				ctx.getSource().sendFeedback(() -> Text.literal("Personnage appliqué: " + chs.getCurrentCharacterID()), true);
				return 1;
			} else
				ctx.getSource().sendFeedback(() -> Text.literal("Aucun personnage trouvé: " + sub), true);
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
				ctx.getSource().sendFeedback(() -> Text.literal("Personnage supprimé: " + sub), true);
				return 1;
			} else
				ctx.getSource().sendFeedback(() -> Text.literal("Aucun personnage trouvé: " + sub), true);
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
			ServerPlayerEntity finalEntity = entity;

			APIService.importCharacter(entity, sub).handle((b, t) -> {
				if (t != null)
					t.printStackTrace();
				if (b.isPresent()) {
					var chs = ComponentManager.getPlayerCharacter(finalEntity);
					chs.switchCharacter(chs.addNewCharacter(b.get()));
					ComponentManager.getPlayerDatas(finalEntity).getAppearence().refreshSkinData();
					Channels.MAIN.serverHandle(ctx.getSource().getServer()).send(new CosmeticsPackets.RefreshSkin(finalEntity.getGameProfile().getId()));
					ctx.getSource().sendFeedback(() -> Text.literal("Nouveau personnage appliqué avec succés: " + chs.getCurrentCharacterID()), true);
					return 1;
				} else {
					ctx.getSource().sendFeedback(() -> Text.literal("Erreur, veuillez réessayer"), true);
					return -1;
				}
			});
			return 1;
		}
		return 0;
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
						ComponentManager.getPlayerDatas(finalEntity).getAppearence().refreshSkinData();
						Channels.MAIN.serverHandle(ctx.getSource().getServer()).send(new CosmeticsPackets.RefreshSkin(finalEntity.getGameProfile().getId()));
						ctx.getSource().sendFeedback(() -> Text.literal("Nouveau personnage appliqué avec succés: " + chs.getCurrentCharacterID()), true);
						return 1;
					} else {
						ctx.getSource().sendFeedback(() -> Text.literal("Erreur, veuillez réessayer"), true);
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

}
