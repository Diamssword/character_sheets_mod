package com.diamssword.characters.mixins;

import com.diamssword.characters.api.ComponentManager;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {

	protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}

	@Inject(at = @At("HEAD"), method = "getDimensions", cancellable = true)
	public void getDimensions(EntityPose pose, CallbackInfoReturnable<EntityDimensions> cir) {
		if (pose == EntityPose.STANDING || pose == EntityPose.CROUCHING || pose == EntityPose.SITTING) {
			var comp = ComponentManager.getPlayerDatas((PlayerEntity)(Object) this);
			var h = comp.getAppearence().getHeightScale();
			var baseH = pose == EntityPose.CROUCHING ? 1.5f : 1.8f;
			var dim = EntityDimensions.changing(0.6f, baseH * h);
			cir.setReturnValue(dim);
		}

	}
	@Inject(at = @At("HEAD"), method = "getActiveEyeHeight", cancellable = true)
	public void getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions, CallbackInfoReturnable<Float> cir) {
		if (pose == EntityPose.STANDING)
			cir.setReturnValue(dimensions.height * 0.9f);
		else if (pose == EntityPose.CROUCHING)
			cir.setReturnValue(dimensions.height * 0.85f);

	}
}