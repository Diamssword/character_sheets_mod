package com.diamssword.characters.client.gui.components;

import com.diamssword.characters.Characters;
import com.diamssword.characters.api.http.SkinLayerValue;
import com.diamssword.characters.api.skin.BodyLayerImageGroup;
import com.diamssword.characters.client.renders.ClothingModel;
import com.diamssword.characters.storage.BodyPartsLoader;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class BodyPartButtonComponent extends ButtonWidget implements  ScrollableCloths.HoverableElement {
	public final static int SIZE=25;

	public static final Identifier TEXTURE=Characters.asRessource("textures/gui/cloth_bg.png");
	private BodyPartRef part;
	private final ClothingModel<AbstractClientPlayerEntity> model = new ClothingModel<>(false, 0, false);
	private final ClothingModel<AbstractClientPlayerEntity> model1 = new ClothingModel<>(false, 1, true);
	protected final EntityRenderDispatcher dispatcher;
	protected final VertexConsumerProvider.Immediate entityBuffers;
	private boolean hoveredSent = false;
	private final List<Consumer<BodyPartRef>> onHovered = new ArrayList<>();
	private Identifier partIcon;
	public final ScrollableBodyParts<BodyPartRef,BodyPartButtonComponent> parent;
	public DrawerContainer<BodyPartRef,BodyPartButtonComponent> drawer;
	public Consumer<DrawerContainer<BodyPartButtonComponent.BodyPartRef,BodyPartButtonComponent>> drawerEditor;
	public Supplier<DrawerContainer<BodyPartRef,BodyPartButtonComponent>> drawerGetter;
	public Function<BodyPartRef,Boolean> isSelectedFn;
	private boolean myHovered;
	public boolean isEquipped=false;
	private Tooltip myTooltip;
	public BodyPartButtonComponent(BodyPartRef part, PressAction action, ScrollableBodyParts<BodyPartRef,BodyPartButtonComponent> parent, Consumer<DrawerContainer<BodyPartButtonComponent.BodyPartRef,BodyPartButtonComponent>> drawerEditor, Supplier<DrawerContainer<BodyPartRef,BodyPartButtonComponent>> drawerGetter,  Function<BodyPartRef,Boolean> isSelectedFn) {
		super(0,0,0,0,Text.literal(""),action,ButtonWidget.DEFAULT_NARRATION_SUPPLIER);
		this.parent=parent;
		final var client = MinecraftClient.getInstance();
		this.dispatcher = client.getEntityRenderDispatcher();
		this.entityBuffers = client.getBufferBuilders().getEntityVertexConsumers();
		this.drawerEditor=drawerEditor;
		this.drawerGetter=drawerGetter;
		this.isSelectedFn=isSelectedFn;
		setPart(part);
	}
	public void setHeight(int height) {
		this.height = height;
	}
	public void setPart(BodyPartRef part) {
		this.part = part;
		var tool=Text.literal((part.image.id().replace("/"," ").replace("_"," ")));
		myTooltip=Tooltip.of(tool);

		var val=new SkinLayerValue();
		val.layer=part.layer;
		val.id=part.image.id();
		if(part.image.hasSubs())
		{
			val.id=part.image.subs()[0].id();
			val.parent=part.image.id();
		}
		if(part.parent!=null)
			val.parent=part.parent.id();
		val.category=part.category;
		var texture=val.getTexturePath();
		int i=texture.indexOf(":");
		var path=texture;
		var id= BodyPartsLoader.instance.getDefaultDomain();
		if(i!=-1)
		{
			id=texture.substring(0,i);
			path=texture.substring(i+1);
		}
		if(part.image.id().equals("clear"))
			this.partIcon=Characters.asRessource("textures/gui/clear.png");
		else
			this.partIcon=new Identifier(id,"textures/bodyparts/icons/"+path+".png");
		this.isEquipped=this.isSelectedFn.apply(part);
	}

	@Override
	public void onClick(double mouseX, double mouseY) {
		if(part.image.hasSubs())
		{
			if(drawer!=null && drawerGetter.get() ==drawer)
			{
				drawer=null;
				drawerEditor.accept(null);
			}
			else {
				drawer = new DrawerContainer<>(getX(), getY(), 30, parent.getWidth() - 4, 60);
				drawer.setContent(Stream.of(part.image.subs()).map(c -> new BodyPartRef(c, part.layer, part.image, part.category)).toList(), c ->{
					var b=new BodyPartButtonComponent(c, onPress, parent, drawerEditor,drawerGetter,isSelectedFn);
					b.onHovered.addAll(this.onHovered);
					return b;
				} );
				drawerEditor.accept(drawer);
			}
		}
		else
		{
			drawerEditor.accept(null);
		}
			super.onClick(mouseX, mouseY);
	}

	public BodyPartRef getPart() {
		return part;
	}

	public void onPartHovered(Consumer<BodyPartRef> callback) {

		onHovered.add(callback);
	}
	public void setHover(boolean hovered)
	{
		this.myHovered=hovered;
		if (this.myHovered) {
			this.setTooltip(myTooltip);
			if (!this.hoveredSent)
				this.onHovered.forEach(c->c.accept(part));
			this.hoveredSent = true;
		} else if (this.hoveredSent) {
			this.setTooltip(null);
			this.onHovered.forEach(c->c.accept(null));
			this.hoveredSent = false;
		}
	}

	@Override
	public boolean isHovered() {
		return myHovered;
	}
	private int getTextureY() {
		int i = 1;
		if (!this.active) {
			i = 0;
		} else if (this.isSelected() || isEquipped) {
			i = 2;
		}
		return 46 + i * 20;
	}
	@Override
	public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
		if(isEquipped)
			context.setShaderColor(1.0F, 1.0F, 0.7F, this.alpha);
		else
			context.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
		RenderSystem.enableBlend();
		RenderSystem.enableDepthTest();
		context.drawNineSlicedTexture(WIDGETS_TEXTURE, this.getX(), this.getY(), this.getWidth(), this.getHeight(), 20, 4, 200, 20, 0, this.getTextureY());
		context.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		var border=6;
		var m=Math.min(this.width,this.height);
		var x1=(this.getX()+(this.width/2))-((m-border)/2);
		var y1=(this.getY()+(this.height/2))-((m-border)/2);
		context.drawTexture( this.partIcon,x1,y1,m-border,m-border,0,0,m,m,m,m);
		if(part.image().hasSubs())
		{
			var rend=MinecraftClient.getInstance().textRenderer;

			context.drawText(rend,"▼",this.getX()+(this.width/2)-(rend.getWidth("▼")/2),this.getY()+this.height-rend.fontHeight,0xB29B2C,false);
		}

		if(drawer!=null && drawer==drawerGetter.get())
		{

			Vector2i vector2i = new Vector2i(getX()- (drawer.getWidth()/2), getY()+getHeight());
			if(vector2i.x<parent.getX()+2)
				vector2i.x= parent.getX()+2;
			if(vector2i.x+drawer.getWidth()>parent.getX()+parent.getWidth()-5)
				vector2i.x=(parent.getX()+parent.getWidth()-5)-drawer.getWidth();
			drawer.setX(vector2i.x);
			drawer.setY(vector2i.y);
		}
	}

	private int getIconX() {
		return this.getX() + (this.width / 2 - SIZE / 2);
	}

	private int getIconY() {
		return this.getY();
	}

	 @Override
	 protected TooltipPositioner getTooltipPositioner() {
		 return new ScrollTooltipPositioner();
	 }
	public static record BodyPartRef(BodyLayerImageGroup image, String layer,@Nullable BodyLayerImageGroup parent, @Nullable String category){

	}
}
