package com.diamssword.characters.client;

import com.diamssword.characters.api.ComponentManager;
import net.minecraft.client.model.*;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class CustomPlayerModel {

    private static final String EAR = "ear";
    /**
     * The key of the cloak model part, whose value is {@value}.
     */
    private static final String CLOAK = "cloak";
    /**
     * The key of the left sleeve model part, whose value is {@value}.
     */
    private static final String LEFT_SLEEVE = "left_sleeve";
    /**
     * The key of the right sleeve model part, whose value is {@value}.
     */
    private static final String RIGHT_SLEEVE = "right_sleeve";
    /**
     * The key of the left pants model part, whose value is {@value}.
     */
    private static final String LEFT_PANTS = "left_pants";
    /**
     * The key of the right pants model part, whose value is {@value}.
     */
    private static final String RIGHT_PANTS = "right_pants";

    public static ModelData getTexturedModelData(Dilation dilation, boolean slim) {
        ModelData modelData = BipedEntityModel.getModelData(dilation, 0.0f);
        ModelPartData modelPartData = modelData.getRoot();
        modelPartData.addChild(EAR, ModelPartBuilder.create().uv(24, 0).cuboid(-3.0f, -6.0f, -1.0f, 6.0f, 6.0f, 1.0f, dilation), ModelTransform.NONE);
        modelPartData.addChild(CLOAK, ModelPartBuilder.create().uv(0, 0).cuboid(-5.0f, 0.0f, -1.0f, 10.0f, 16.0f, 1.0f, dilation, 1.0f, 0.5f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        var dil = dilation;
        dil = dil.add(slim ? -0.25f : 0.25f, 0, 0);
        modelPartData.addChild(
                EntityModelPartNames.LEFT_ARM,
                ModelPartBuilder.create().uv(32, 48).cuboid(-1.0f + (slim ? -0.2f : 0), -2.0F, -2.0F, 3.5F, 12.0F, 4.0F, dil),
                ModelTransform.pivot(5.0F, 2f+(slim?0.5f:0), 0.0F)
        );
        modelPartData.addChild(
                EntityModelPartNames.RIGHT_ARM,
                ModelPartBuilder.create().uv(40, 16).cuboid(-2.5f - (slim ? -0.1f : 0.15f), -2.0F, -2.0F, 3.5F, 12.0F, 4.0F, dil),
                ModelTransform.pivot(-5.0F, 2f+(slim?0.5f:0), 0.0F)
        );
        //modelPartData.addChild(EntityModelPartNames.LEFT_ARM, ModelPartBuilder.create().uv(32, 48).cuboid(-1.0f + (slim ? -0.2f : 0), -2.0f, -2.0f, 3.5f, 12.0f, 4.0f, dil), ModelTransform.pivot(5.0f, 2f, 0.0f));
        //modelPartData.addChild(EntityModelPartNames.RIGHT_ARM, ModelPartBuilder.create().uv(40, 16).cuboid(-2.5f - (slim ? -0.2f : 0.15f), -2.0f, -2.0f, 3.5f, 12.0f, 4.0f, dil), ModelTransform.pivot(-5.0f, 2f, 0.0f));
        modelPartData.addChild(LEFT_SLEEVE, ModelPartBuilder.create().uv(48, 48).cuboid(-1.0f + (slim ? 0.1f : 0), -2.0f, -2.0f, 3.0f, 12.0f, 4.0f, dil.add(0.25f)), ModelTransform.pivot(5.0f, 2f, 0.0f));
        modelPartData.addChild(RIGHT_SLEEVE, ModelPartBuilder.create().uv(40, 32).cuboid(-2.5f - (slim ? -0.1f : 0.15f), -2.0f, -2.0f, 3.0f, 12.0f, 4.0f, dil.add(0.25f)), ModelTransform.pivot(-5.0f, 2f, 0.0f));

        modelPartData.addChild(EntityModelPartNames.LEFT_LEG, ModelPartBuilder.create().uv(16, 48).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, dilation), ModelTransform.pivot(1.9f, 12.0f, 0.0f));
        modelPartData.addChild(LEFT_PANTS, ModelPartBuilder.create().uv(0, 48).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, dilation.add(0.25f)), ModelTransform.pivot(1.9f, 12.0f, 0.0f));
        modelPartData.addChild(RIGHT_PANTS, ModelPartBuilder.create().uv(0, 32).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, dilation.add(0.25f)), ModelTransform.pivot(-1.9f, 12.0f, 0.0f));
        modelPartData.addChild(EntityModelPartNames.JACKET, ModelPartBuilder.create().uv(16, 32).cuboid(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f, dilation.add(0.25f)), ModelTransform.NONE);
        return modelData;
    }

    public static void scale(LivingEntity abstractClientPlayerEntity, MatrixStack matrixStack, float f, CallbackInfo info) {
        var sx = 1f;
        var sy = 1f;
        if (abstractClientPlayerEntity instanceof AbstractClientPlayerEntity pl) {
            var prof = pl.getGameProfile();
            if (prof != null) {
                var st = ComponentManager.getPlayerDatas(pl).getAppearence();
                //Disabled for now sx = st.getRestrainedWidth();
                sy = st.getHeightScale();
            }

        }
        matrixStack.scale(sy, sy, sy);
    }
}
