package com.diamssword.characters.client;

import com.diamssword.characters.Characters;
import com.diamssword.characters.storage.PlayerAppearance;
import com.diamssword.characters.api.appearence.LayerDef;
import com.diamssword.characters.api.ComponentManager;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import java.awt.*;

public class ClothingLayer  extends FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {
    private final FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> ctx;
    private final LayerDef layer;
    public final boolean altTexture;
    private final ClothingModel<AbstractClientPlayerEntity> model;
    public ClothingLayer(FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> context, LayerDef layer, boolean altTexture, boolean thinArm) {
        super(context);
        this.ctx=context;
        this.altTexture=altTexture;
        this.layer=layer;
        model= new ClothingModel<>(thinArm, altTexture?layer.getLayer2():layer.getLayer1(),altTexture);
    }
    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        var data= ComponentManager.getPlayerDatas(entity);
        if(data.getAppearence() instanceof PlayerAppearance ap)
        {
                var c1=ap.getClothDatas(layer);
                if(c1.isPresent())
                {
                    var c=c1.get();
                    ctx.getModel().copyBipedStateTo(model);
                    model.animateModel(entity,limbAngle,limbDistance,tickDelta);
                    var col=new Color(255,255,255);
                    if(c.needColor())
                    {
                        col=new Color(c.color()).brighter();//dirty trick to make color fit more with the website viewer
                    }
                        model.render(matrices,vertexConsumers.getBuffer(model.getLayer(new Identifier(c.texture().getNamespace(),"textures/cloth/"+c.texture().getPath()+".png"))),light, LivingEntityRenderer.getOverlay(entity,0),col.getRed()/255f, col.getGreen() /255f,  col.getBlue() /255f,1);
                }
        }
    }

}
