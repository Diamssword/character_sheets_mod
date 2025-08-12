package com.diamssword.characters.client;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.math.random.Random;

@Environment(EnvType.CLIENT)
public class ClothingModel<T extends LivingEntity> extends BipedEntityModel<T> {
    private final boolean thinArms;

    public ClothingModel( boolean thinArms,int layer,boolean altTexture) {
        super(getTexturedModelData(new Dilation(0.04f+(layer*0.04f)),thinArms,altTexture).createModel(), RenderLayer::getEntityTranslucent);
        this.thinArms = thinArms;

        this.hat.visible=false;
    }

    public static TexturedModelData getTexturedModelData(Dilation dilation, boolean slim,boolean altTexture) {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        modelPartData.addChild("head", ModelPartBuilder.create().uv(altTexture?32:0, 0).cuboid(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, dilation), ModelTransform.pivot(0.0F, 0.0F , 0.0F));
        modelPartData.addChild("hat", ModelPartBuilder.create().uv(32, 0).cuboid(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, dilation.add(0.5F)), ModelTransform.pivot(0.0F, 0.0F, 0.0F));
        modelPartData.addChild("body", ModelPartBuilder.create().uv(16, altTexture?32:16).cuboid(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, dilation), ModelTransform.pivot(0.0F, 0.0F, 0.0F));
        modelPartData.addChild("right_leg", ModelPartBuilder.create().uv(0, altTexture?32:16).cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, dilation), ModelTransform.pivot(-1.9F, 12.0F, 0.0F));
        modelPartData.addChild("left_leg", ModelPartBuilder.create().uv(altTexture?0:16, 48).cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, dilation), ModelTransform.pivot(1.9F, 12.0F, 0.0F));
        var dil=dilation;
            dil=dil.add(slim?-0.25f:0.15f,0,0);
            modelPartData.addChild("left_arm", ModelPartBuilder.create().uv(altTexture?48:32, 48).cuboid(-1.0f+(slim?-0.2f:0), -2.0F, -2.0F, 3.5F, 12.0F, 4.0F, dil), ModelTransform.pivot(5.0F, 2.5F, 0.0F));
            modelPartData.addChild("right_arm", ModelPartBuilder.create().uv(40, altTexture?32:16).cuboid(-2.5f-(slim?-0.2f:0.15f), -2.0F, -2.0F, 3.5F, 12.0F, 4.0F, dil), ModelTransform.pivot(-5.0F, 2.5F, 0.0F));

        return TexturedModelData.of(modelData,64,64);
    }

    protected Iterable<ModelPart> getBodyParts() {
        return Iterables.concat(super.getBodyParts());
    }
    public void setAngles(T livingEntity, float f, float g, float h, float i, float j) {
        super.setAngles(livingEntity, f, g, h, i, j);
    }

    public void setVisible(boolean visible) {
        super.setVisible(visible);
        this.hat.visible=false;
    }

    public void setArmAngle(Arm arm, MatrixStack matrices) {
        ModelPart modelPart = this.getArm(arm);
        if (this.thinArms) {
            float f = 0.5F * (float)(arm == Arm.RIGHT ? 1 : -1);
            modelPart.pivotX += f;
            modelPart.rotate(matrices);
            modelPart.pivotX -= f;
        } else {
            modelPart.rotate(matrices);
        }

    }

}
