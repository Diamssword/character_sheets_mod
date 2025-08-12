package com.diamssword.characters.fabric.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIParsing;
import io.wispforest.owo.util.pond.OwoEntityRenderDispatcherExtension;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.RotationAxis;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.w3c.dom.Element;

import java.util.Map;
import java.util.function.Consumer;

public class PlayerComponent extends BaseComponent {

	protected final EntityRenderDispatcher dispatcher;
	protected final VertexConsumerProvider.Immediate entityBuffers;
	protected final PlayerEntity entity;

	protected float mouseRotation = 0;
	protected float scale = 1;
	protected boolean lookAtCursor = false;
	protected boolean allowMouseRotation = false;
	protected boolean scaleToFit = false;
	protected int rotation = 0;
	protected boolean showNametag = false;
	protected Consumer<MatrixStack> transform = matrixStack -> {
	};

	protected PlayerComponent(Sizing sizing, PlayerEntity entity) {
		final var client = MinecraftClient.getInstance();
		this.dispatcher = client.getEntityRenderDispatcher();
		this.entityBuffers = client.getBufferBuilders().getEntityVertexConsumers();

		this.entity = entity;

		this.sizing(sizing);
	}

	protected PlayerComponent(Sizing sizing) {
		this(sizing, new OtherClientPlayerEntity(MinecraftClient.getInstance().world, MinecraftClient.getInstance().player.getGameProfile()) {
			@Override
			public boolean isSpectator() {
				return false;
			}

			@Override
			public boolean isCreative() {
				return false;
			}
		});

	}

	@Override
	protected int determineHorizontalContentSize(Sizing sizing) {
		return this.height;
	}

	@Override
	protected void applySizing() {
		final var horizontalSizing = this.horizontalSizing.get();
		final var verticalSizing = this.verticalSizing.get();

		final var margins = this.margins.get();
		this.height = verticalSizing.inflate(this.space.height() - margins.vertical(), this::determineVerticalContentSize);
		this.width = horizontalSizing.inflate(this.space.width() - margins.horizontal(), this::determineHorizontalContentSize);
	}

	@Override
	public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
		var matrices = context.getMatrices();
		matrices.push();

		matrices.translate(x + this.width / 2f, y + this.height * 0.6f, 100);
		matrices.scale(75 * this.scale * this.width / 64f, -75 * this.scale * this.height / 64f, 75 * this.scale);

		matrices.translate(0, entity.getHeight() / -2f, 0);

		this.transform.accept(matrices);

		if (this.lookAtCursor) {
			float xRotation = (float) Math.toDegrees(Math.atan((mouseY - this.y - this.height / 2f) / 40f));
			float yRotation = (float) Math.toDegrees(Math.atan((mouseX - this.x - this.width / 2f) / 40f));
			this.entity.prevHeadYaw = -yRotation;
			this.entity.prevYaw = -yRotation;
			this.entity.prevPitch = xRotation * .65f;

			// We make sure the xRotation never becomes 0, as the lighting otherwise becomes very unhappy
			if (xRotation == 0) xRotation = .1f;
			matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(xRotation * .15f));
			matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yRotation * .15f));
		} else {
			matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(35));
			matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-45 + this.mouseRotation));
		}
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotation));
		var dispatcher = (OwoEntityRenderDispatcherExtension) this.dispatcher;
		dispatcher.owo$setCounterRotate(true);
		dispatcher.owo$setShowNametag(this.showNametag);

		RenderSystem.setShaderLights(new Vector3f(.15f, 1, 0), new Vector3f(.15f, -1, 0));
		this.dispatcher.setRenderShadows(false);
		this.dispatcher.render(this.entity, 0, 0, 0, 0, 0, matrices, this.entityBuffers, LightmapTextureManager.MAX_LIGHT_COORDINATE);
		this.dispatcher.setRenderShadows(true);
		this.entityBuffers.draw();
		DiffuseLighting.enableGuiDepthLighting();

		matrices.pop();

		dispatcher.owo$setCounterRotate(false);
		dispatcher.owo$setShowNametag(true);
	}

	@Override
	public boolean onMouseDrag(double mouseX, double mouseY, double deltaX, double deltaY, int button) {
		if (this.allowMouseRotation && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
			this.mouseRotation += deltaX;

			super.onMouseDrag(mouseX, mouseY, deltaX, deltaY, button);
			return true;
		} else {
			return super.onMouseDrag(mouseX, mouseY, deltaX, deltaY, button);
		}
	}

	public PlayerEntity entity() {
		return this.entity;
	}

	public PlayerComponent allowMouseRotation(boolean allowMouseRotation) {
		this.allowMouseRotation = allowMouseRotation;
		return this;
	}

	public PlayerComponent rotation(int rotation) {
		this.rotation = rotation;
		return this;
	}

	public boolean allowMouseRotation() {
		return this.allowMouseRotation;
	}

	public PlayerComponent lookAtCursor(boolean lookAtCursor) {
		this.lookAtCursor = lookAtCursor;
		return this;
	}

	public boolean lookAtCursor() {
		return this.lookAtCursor;
	}

	public PlayerComponent scale(float scale) {
		this.scale = scale;
		return this;
	}

	public float scale() {
		return this.scale;
	}

	public PlayerComponent scaleToFit(boolean scaleToFit) {
		this.scaleToFit = scaleToFit;

		if (scaleToFit) {
			float xScale = .6f / entity.getWidth();
			float yScale = .6f / entity.getHeight();

			this.scale(Math.min(xScale, yScale));
		}

		return this;
	}

	public boolean scaleToFit() {
		return this.scaleToFit;
	}

	public PlayerComponent transform(Consumer<MatrixStack> transform) {
		this.transform = transform;
		return this;
	}

	public Consumer<MatrixStack> transform() {
		return transform;
	}

	public PlayerComponent showNametag(boolean showNametag) {
		this.showNametag = showNametag;
		return this;
	}

	public boolean showNametag() {
		return showNametag;
	}

	@Override
	public boolean canFocus(FocusSource source) {
		return source == FocusSource.MOUSE_CLICK;
	}

	@Override
	public void parseProperties(UIModel model, Element element, Map<String, Element> children) {
		super.parseProperties(model, element, children);

		UIParsing.apply(children, "scale", UIParsing::parseFloat, this::scale);
		UIParsing.apply(children, "look-at-cursor", UIParsing::parseBool, this::lookAtCursor);
		UIParsing.apply(children, "mouse-rotation", UIParsing::parseBool, this::allowMouseRotation);
		UIParsing.apply(children, "scale-to-fit", UIParsing::parseBool, this::scaleToFit);
	}

	public static PlayerComponent parse(Element element) {
		return new PlayerComponent(Sizing.content());
	}
}
