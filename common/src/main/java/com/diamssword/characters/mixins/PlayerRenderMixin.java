package com.diamssword.characters.mixins;

import com.diamssword.characters.storage.ClothingLoader;
import com.diamssword.characters.api.appearence.LayerDef;
import com.diamssword.characters.client.renders.ClothingLayer;
import com.diamssword.characters.client.renders.CustomPlayerModel;
import com.diamssword.characters.client.Entities;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerRenderMixin extends LivingEntityRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {

	public PlayerRenderMixin(EntityRendererFactory.Context ctx, PlayerEntityModel<AbstractClientPlayerEntity> model, float shadowRadius) {
		super(ctx, model, shadowRadius);
	}

	@Inject(at = @At("TAIL"), method = "<init>")
	private void init(EntityRendererFactory.Context ctx, boolean slim, CallbackInfo info) {

		var th = ((PlayerEntityRenderer) (Object) this);
		((LivingRendererAccessor) th).setModel(new PlayerEntityModel<>(ctx.getPart(slim ? Entities.PLAYER_MODEL_S : Entities.PLAYER_MODEL), slim));
		if(ClothingLoader.instance !=null) {
			for (LayerDef value : ClothingLoader.instance.getLayers().values()) {
				this.addFeature(new ClothingLayer(th, value, false, slim));
				if (value.getLayer2() > -1)
					this.addFeature(new ClothingLayer(th, value, true, slim));
			}
		}
	}

	@Inject(at = @At("TAIL"), method = "scale(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/util/math/MatrixStack;F)V")
	protected void scale(LivingEntity abstractClientPlayerEntity, MatrixStack matrixStack, float f, CallbackInfo info) {
		CustomPlayerModel.scale(abstractClientPlayerEntity, matrixStack, f, info);
	}


}
