package com.diamssword.characters.mixins;

import com.diamssword.characters.client.SkinsLoader;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.network.ClientConnection;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(ClientConnection.class)
public abstract class ClientConnectionMixin {

	@Inject(at = @At("HEAD"), method = "isEncrypted", cancellable = true)
	protected void loadTextures(CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(true);
	}
}
