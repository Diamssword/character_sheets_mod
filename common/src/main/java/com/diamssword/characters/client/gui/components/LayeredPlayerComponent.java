package com.diamssword.characters.client.gui.components;

import com.diamssword.characters.client.renders.LayeredPlayer;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

import java.util.function.Consumer;

public class LayeredPlayerComponent extends BaseComponent {

	protected final EntityRenderDispatcher dispatcher;
	protected final VertexConsumerProvider.Immediate entityBuffers;
	protected final AbstractClientPlayerEntity entity;
	protected final LayeredPlayer renderer;
	protected float mouseRotation = 0;
	protected float scale = 1;
	protected boolean lookAtCursor = false;
	protected boolean allowMouseRotation = false;
	protected boolean scaleToFit = false;
	protected int rotation = 0;
	protected boolean showNametag = false;
	protected Consumer<MatrixStack> transform = matrixStack -> {
	};
	public LayeredPlayer getRenderer()
	{
		return renderer;
	}
	public LayeredPlayerComponent(Sizing sizingX, Sizing sizingY, AbstractClientPlayerEntity entity) {
		super(sizingX,sizingY);
		final var client = MinecraftClient.getInstance();
		this.dispatcher = client.getEntityRenderDispatcher();
		this.entityBuffers = client.getBufferBuilders().getEntityVertexConsumers();
		this.entity = entity;
		var mc=MinecraftClient.getInstance();
		EntityRendererFactory.Context context = new EntityRendererFactory.Context(mc.getEntityRenderDispatcher(), mc.getItemRenderer(), mc.getBlockRenderManager(), mc.getEntityRenderDispatcher().getHeldItemRenderer(),  mc.getResourceManager(),  mc.getEntityModelLoader(), mc.textRenderer);
		renderer = new LayeredPlayer(context,true);
	}

	public LayeredPlayerComponent(Sizing sizingX, Sizing sizingY) {
		this(sizingX,sizingY, new OtherClientPlayerEntity(MinecraftClient.getInstance().world, new GameProfile(MinecraftClient.getInstance().player.getGameProfile().getId(),"")) {
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
			matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-8));
			matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-45 + this.mouseRotation));
		}
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotation));

		RenderSystem.setShaderLights(new Vector3f(.15f, 1, 0), new Vector3f(.15f, -1, 0));
		this.dispatcher.setRenderShadows(false);
		this.render( 0, 0, 0, 0, 0, matrices, this.entityBuffers, LightmapTextureManager.MAX_LIGHT_COORDINATE);
		this.dispatcher.setRenderShadows(true);
		this.entityBuffers.draw();
		DiffuseLighting.enableGuiDepthLighting();

		matrices.pop();
	}


	public PlayerEntity entity() {
		return this.entity;
	}

	public LayeredPlayerComponent allowMouseRotation(boolean allowMouseRotation) {
		this.allowMouseRotation = allowMouseRotation;
		return this;
	}

	public LayeredPlayerComponent rotation(int rotation) {
		this.rotation = rotation;
		return this;
	}

	public boolean allowMouseRotation() {
		return this.allowMouseRotation;
	}

	public LayeredPlayerComponent lookAtCursor(boolean lookAtCursor) {
		this.lookAtCursor = lookAtCursor;
		return this;
	}

	public boolean lookAtCursor() {
		return this.lookAtCursor;
	}

	public LayeredPlayerComponent scale(float scale) {
		this.scale = scale;
		return this;
	}

	public float scale() {
		return this.scale;
	}

	public LayeredPlayerComponent scaleToFit(boolean scaleToFit) {
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

	public LayeredPlayerComponent transform(Consumer<MatrixStack> transform) {
		this.transform = transform;
		return this;
	}

	public Consumer<MatrixStack> transform() {
		return transform;
	}

	public void render( double x, double y, double z, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light
	) {


		try {
			Vec3d vec3d = renderer.getPositionOffset(entity, tickDelta);
			double d = x + vec3d.getX();
			double e = y + vec3d.getY();
			double f = z + vec3d.getZ();
			matrices.push();
			matrices.translate(d, e, f);
			renderer.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);


			matrices.translate(-vec3d.getX(), -vec3d.getY(), -vec3d.getZ());


			matrices.pop();
		} catch (Throwable var24) {
			CrashReport crashReport = CrashReport.create(var24, "Rendering entity in world");
			CrashReportSection crashReportSection = crashReport.addElement("Entity being rendered");
			entity.populateCrashReport(crashReportSection);
			CrashReportSection crashReportSection2 = crashReport.addElement("Renderer details");
			crashReportSection2.add("Assigned renderer", renderer);
			crashReportSection2.add("Rotation", yaw);
			crashReportSection2.add("Delta", tickDelta);
			throw new CrashException(crashReport);
		}
	}
}