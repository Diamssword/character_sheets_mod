package com.diamssword.characters.client.gui.components;

import com.diamssword.characters.Characters;
import com.diamssword.characters.api.appearence.Cloth;
import com.diamssword.characters.client.ClothingModel;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.*;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.RotationAxis;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ClothButtonComponent extends ButtonWidget {
	public static final Identifier TEXTURE=Characters.asRessource("textures/gui/cloth_bg.png");
	private Cloth cloth;
	private final ClothingModel<AbstractClientPlayerEntity> model = new ClothingModel<>(false, 0, false);
	private final ClothingModel<AbstractClientPlayerEntity> model1 = new ClothingModel<>(false, 1, true);
	protected final EntityRenderDispatcher dispatcher;
	protected final VertexConsumerProvider.Immediate entityBuffers;
	private boolean hoveredSent = false;
	private boolean selected = false;
	private final List<Consumer<Cloth>> onHovered = new ArrayList<>();
	private boolean wasHovered;
	private long lastHoveredTime;
	private long tooltipDelay;
	public final ScrollableCloths parent;
	public ClothButtonComponent(Cloth cloth,ButtonWidget.PressAction action,ScrollableCloths parent) {
		super(0,0,0,0,Text.literal(""),action,ButtonWidget.DEFAULT_NARRATION_SUPPLIER);
		this.parent=parent;
		setCloth(cloth);
		final var client = MinecraftClient.getInstance();
		this.dispatcher = client.getEntityRenderDispatcher();
		this.entityBuffers = client.getBufferBuilders().getEntityVertexConsumers();
	}
	public void setHeight(int height) {
		this.height = height;
	}
	public void setCloth(Cloth cloth) {
		this.cloth = cloth;
		var tool=Text.literal((cloth.name().replaceAll("/"," ").replaceAll("_"," ")));
		if(!cloth.collection().equals("default"))
			tool.append(Text.literal("\n"+cloth.collection()).formatted(Formatting.BLUE,Formatting.ITALIC));
		this.setTooltip(Tooltip.of(tool));
	}
	public Cloth getCloth() {
		return cloth;
	}

	public boolean selected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public void onClothHovered(Consumer<Cloth> callback) {

		onHovered.add(callback);
	}
	public void setHover(boolean hovered)
	{
		this.hovered=hovered;
		if (this.hovered) {
			if (!this.hoveredSent)
				this.onHovered.forEach(c->c.accept(cloth));
			this.hoveredSent = true;
		} else if (this.hoveredSent) {
			this.onHovered.forEach(c->c.accept(null));
			this.hoveredSent = false;
		}
	}
	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		if (this.visible) {
			this.renderButton(context, mouseX, mouseY, delta);
			this.applyTooltip();
		}
	}

	private void applyTooltip() {
		if (this.getTooltip() != null) {
			boolean bl = this.hovered || this.isFocused() && MinecraftClient.getInstance().getNavigationType().isKeyboard();
			if (bl != this.wasHovered) {
				if (bl) {
					this.lastHoveredTime = Util.getMeasuringTimeMs();
				}

				this.wasHovered = bl;
			}

			if (bl && Util.getMeasuringTimeMs() - this.lastHoveredTime > this.tooltipDelay) {
				Screen screen = MinecraftClient.getInstance().currentScreen;
				if (screen != null) {
					screen.setTooltip(this.getTooltip(), this.getTooltipPositioner(), this.isFocused());
				}
			}
		}
	}
	 @Override
	 protected TooltipPositioner getTooltipPositioner() {
		 return new ScrollTooltipPositioner(this);
	 }

	@Override
	protected void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
		var x=this.getX()+1;
		var y=this.getY()+1;
		var a=this.getX()+this.getWidth()-1;
		var b=this.getY()+this.getHeight()-1;
		context.fill(x,y,a,b,this.selected?0xffb1d2b0: 0xffc6c6c6);
		context.drawHorizontalLine(x,a-1,this.getY(),0xffffffff);
		context.drawVerticalLine(this.getX(),y-1,b,0xffffffff);
		context.drawHorizontalLine(x,a-1,this.getY()+this.getHeight()-1,0xff555555);
		context.drawVerticalLine(this.getX()+getWidth()-1,y-1,b,0xff555555);
		drawClothing(context, mouseX);

	}

	public void drawClothing(DrawContext context, int mouseX) {
		var matrices = context.getMatrices();
		matrices.push();
		float scale = cloth.layer().getDisplayMode()==1 ?30: (cloth.layer().getDisplayMode()==2 ? 40 : 20);
		matrices.translate(this.getX() + (this.width / 2f), this.getY() + (this.height / 2f) - 8, 100);
		if (cloth.layer().getDisplayMode()!=0) {
			if (cloth.layer().getDisplayMode()==2) {
				matrices.translate(0, -(this.height * 0.8f), 0);
			} else {
				matrices.translate(0, this.height / 3f, 0);
			}
			matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-15f));
		}
		matrices.scale(scale, scale, scale);
		float yRotation = (float) Math.toDegrees(Math.atan((mouseX - this.getX() - this.width / 2f) / 40f));
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180 + (yRotation * .6f)));
		RenderSystem.setShaderLights(new Vector3f(.15f, 1, 0), new Vector3f(.15f, -1, 0));
		this.dispatcher.setRenderShadows(false);
		renderLayer(matrices, this.entityBuffers, LightmapTextureManager.MAX_LIGHT_COORDINATE);
		this.dispatcher.setRenderShadows(true);
		this.entityBuffers.draw();
		DiffuseLighting.enableGuiDepthLighting();

		matrices.pop();
	}

	public void renderLayer(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
		model.child = false;
		model1.child = false;
		var pack = OverlayTexture.packUv(OverlayTexture.getU(0), OverlayTexture.getV(false));
		model.render(matrices, vertexConsumers.getBuffer(model.getLayer(new Identifier(cloth.id().getNamespace(),"textures/cloth/"+ cloth.id().getPath() + ".png"))), light, pack, 1, 1, 1, 1);
		model1.render(matrices, vertexConsumers.getBuffer(model1.getLayer(new Identifier(cloth.id().getNamespace(),"textures/cloth/"+ cloth.id().getPath() + ".png"))), light, pack, 1, 1, 1, 1);

	}
}
