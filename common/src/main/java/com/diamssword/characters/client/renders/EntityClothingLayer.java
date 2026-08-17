package com.diamssword.characters.client.renders;

import com.diamssword.characters.api.ComponentManager;
import com.diamssword.characters.api.IPlayerAppearanceProvider;
import com.diamssword.characters.api.appearence.LayerDef;
import com.diamssword.characters.storage.PlayerAppearance;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

import java.awt.*;

public class EntityClothingLayer<T extends LivingEntity & IPlayerAppearanceProvider> extends FeatureRenderer<T, PlayerEntityModel<T>> {
	private final FeatureRendererContext<T, PlayerEntityModel<T>> ctx;
	private final LayerDef layer;
	public final boolean altTexture;
	private ClothingModel<T> model;
	private boolean slim=false;
	public EntityClothingLayer(FeatureRendererContext<T, PlayerEntityModel<T>> context, LayerDef layer, boolean altTexture) {
		super(context);
		this.ctx=context;
		this.altTexture=altTexture;
		this.layer=layer;
		model= new ClothingModel<>(slim, altTexture?layer.getLayer2():layer.getLayer1(),altTexture);
	}
	@Override
	public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
			if(entity.getSkinDatas().slim!=slim)
			{
				slim=!slim;
				model= new ClothingModel<>(slim, altTexture?layer.getLayer2():layer.getLayer1(),altTexture);
			}
			var c1=entity.getClothDatas(layer);

			if(c1.isPresent())
			{

				var c=c1.get();
				ctx.getModel().copyBipedStateTo(model);
				model.animateModel(entity,limbAngle,limbDistance,tickDelta);
				var col=new Color(255,255,255);
				if(c.needColor())
				{
					col=new Color(c.color());
				}
				model.render(matrices,vertexConsumers.getBuffer(model.getLayer(new Identifier(c.texture().getNamespace(),"textures/cloth/"+c.texture().getPath()+".png"))),light, LivingEntityRenderer.getOverlay(entity,0),col.getRed()/255f, col.getGreen() /255f,  col.getBlue() /255f,1);
			}
	}

}
