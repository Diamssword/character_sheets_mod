package com.diamssword.characters.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
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

	public PlayerComponent(Sizing sizingX,Sizing sizingY, PlayerEntity entity) {
		super(sizingX,sizingY);
		final var client = MinecraftClient.getInstance();
		this.dispatcher = client.getEntityRenderDispatcher();
		this.entityBuffers = client.getBufferBuilders().getEntityVertexConsumers();

		this.entity = entity;
	}

	public PlayerComponent(Sizing sizingX,Sizing sizingY) {
		this(sizingX,sizingY, new OtherClientPlayerEntity(MinecraftClient.getInstance().world, MinecraftClient.getInstance().player.getGameProfile()) {
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
	protected int determineVerticalContentSize(Sizing sizing) {
		return this.width;
	}

	@Override
	public void mount(int x, int y, int width, int height) {
		final var horizontalSizing = this.sizeX;
		final var verticalSizing = this.sizeY;
		this.x = x;
		this.y = y;
		this.width = horizontalSizing.inflate(width, this::determineHorizontalContentSize);
		this.height = verticalSizing.inflate(height, this::determineVerticalContentSize);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
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

		RenderSystem.setShaderLights(new Vector3f(.15f, 1, 0), new Vector3f(.15f, -1, 0));
		this.dispatcher.setRenderShadows(false);
		this.dispatcher.render(this.entity, 0, 0, 0, 0, 0, matrices, this.entityBuffers, LightmapTextureManager.MAX_LIGHT_COORDINATE);
		this.dispatcher.setRenderShadows(true);
		this.entityBuffers.draw();
		DiffuseLighting.enableGuiDepthLighting();

		matrices.pop();
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


}