package com.diamssword.characters.mixins;

import com.diamssword.characters.client.SkinsLoader;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerListHud.class)
public class PlayerListHudMixin {

    @Inject(method = "getPlayerName", at = @At("RETURN"), cancellable = true)
    private void changeTabName(PlayerListEntry entry, CallbackInfoReturnable<Text> cir) {
		var value=SkinsLoader.clientSkinCache.getSkin(entry.getProfile().getId());
		if(value.isPresent() && value.get().characterName()!=null) {

			cir.setReturnValue(Text.literal(value.get().characterName()).append(Text.literal(" [").append(cir.getReturnValue()).append(Text.literal("]")).formatted(Formatting.ITALIC,Formatting.GRAY)));
		}
    }

}