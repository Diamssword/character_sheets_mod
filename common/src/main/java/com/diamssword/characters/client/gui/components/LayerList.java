package com.diamssword.characters.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

import java.util.function.Consumer;

public class LayerList  extends ClickableWidget implements Drawable, Element {

	private final int MAX=20;
	private int offset=0;
	private int layersCount=1;
	private int selected=0;
	private int hovered=-1;
	private LayerSelectEvent listener;
	public LayerList(int x, int y, int width, int height) {
		super(x, y, width, height, Text.literal(""));
	}
	public void setListener(LayerSelectEvent listener)
	{
		this.listener=listener;
	}
	@Override
	protected void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
		hovered=getMouseComponentIndex(mouseX,mouseY);
		MinecraftClient minecraftClient = MinecraftClient.getInstance();
		int spaces=displaySpaces();
		int y=this.getY();
		if(hasUpArrow())
		{
			drawButton(context,this.getY(),minecraftClient,-1);
			y+=this.width-5;
			spaces-=1;
		}

		if(hasDownArrow())
		{
			drawButton(context,this.getY()+this.height-(this.width-5),minecraftClient,-2);
			spaces-=1;
		}
		else
			drawButton(context,y+( Math.min(spaces-1,layersCount)*(this.width-5)),minecraftClient,layersCount);
		for(int i = 0; i < Math.min(spaces,layersCount); i++) {
			if(offset+i<layersCount)
				drawButton(context,y+(i*(this.width-5)),minecraftClient,offset+i);

		}
	applyTooltip(mouseX,mouseY);
	}
	private void applyTooltip( int mouseX,int mouseY) {
		Screen screen = MinecraftClient.getInstance().currentScreen;
		if (screen != null && hovered>-1 && hovered <=layersCount) {
			String t="Layer "+(hovered+1);
			if(hovered==layersCount)
				t="Add New Layer";
			else if(selected==hovered)
			{
				var s=selected+1;
				if(offset>0)
					s=selected-offset+2;
				if(mouseX>this.getX()+this.width-8 && mouseY<this.getY()+(s*(this.width-5))- 8)
					t="Delete layer";
			}
			screen.setTooltip(Tooltip.of(Text.literal(t)), this.getTooltipPositioner(), this.isFocused());
		}
	}

	@Override
	protected TooltipPositioner getTooltipPositioner() {
		return new ScrollTooltipPositioner();
	}

	protected boolean isWithinBounds(double mouseX, double mouseY) {
		return mouseX >= this.getX() && mouseX < this.getX() + this.width && mouseY >= this.getY() && mouseY < this.getY() + this.height;
	}
	private boolean hasUpArrow()
	{
		return offset>0;
	}
	private boolean hasDownArrow()
	{
		var sp=displaySpaces();
		if(hasUpArrow())
			sp--;
		return (layersCount + 1)-offset > sp;
	}
	private int displaySpaces()
	{
		return (this.getHeight()/(this.width-5))+1;
	}
	private int getMouseComponentIndex(double mouseX, double mouseY)
	{
		if(this.isWithinBounds(mouseX, mouseY))
		{
				int m= (int) ((mouseY-this.getY())/(this.getWidth()-5));
				if(m>=0)
				{
					if(m==0 && hasUpArrow())
						return -1;

					var spaces=displaySpaces();
					if(hasDownArrow() && m==spaces-1 )
					{
						return -2;
					}
					if(offset==0)
						return m;
					if(m<=layersCount)
						return m+offset-1;
				}

		}
		return -10;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		var m=getMouseComponentIndex(mouseX,mouseY);
				if(m>-1){
					if(m<layersCount)
					{
						if(m==selected && layersCount>1)
						{
							var s=selected+1;
							if(offset>0)
								s=selected-offset+2;
							if(mouseX>this.getX()+this.width-8 && mouseY<this.getY()+(s*(this.width-5))- 8)
							{
								if(listener != null)
									listener.onLayerRemoved(selected);
								layersCount--;
								if(selected>=layersCount)
									selected--;
								if(listener != null)
									listener.onLayerSelected(selected);
							}
						}
						else {
							selected = m;
							if(listener != null)
								listener.onLayerSelected(selected);
						}
						this.playDownSound(MinecraftClient.getInstance().getSoundManager());
						return true;
					}
					else if(m==layersCount)
					{
						layersCount= Math.min(layersCount+1,MAX);
						selected=layersCount-1;
						if(listener!=null) {
							listener.onLayerAdded(selected);
							listener.onLayerSelected(selected);
						}
						this.playDownSound(MinecraftClient.getInstance().getSoundManager());
						return true;
					}
				}
				else if(m==-2)
				{
					offset++;
					this.playDownSound(MinecraftClient.getInstance().getSoundManager());
					return true;
				}
				else if(m==-1) {
					offset--;
					this.playDownSound(MinecraftClient.getInstance().getSoundManager());
					return true;
				}
		return false;
	}

	public void drawButton(DrawContext context,int y, MinecraftClient client, int index)
	{
		int x=getX()+5;
		if(index==this.selected)
			x=getX();
		context.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
		RenderSystem.enableBlend();
		RenderSystem.enableDepthTest();
		context.drawNineSlicedTexture(WIDGETS_TEXTURE, x,y, width-(index==this.selected?0:5), width-5, 20, 4, 200, 20, 0, this.getTextureY(index));
		context.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		int i = this.active ? 16777215 : 10526880;
		var text=(index+1)+"";
		if(index==-1)
			text="▲";
		else if(index==-2)
			text="▼";
		else if(index>=layersCount)
			text="+";

		context.drawCenteredTextWithShadow(client.textRenderer,text,x+((width-5)/2),y+6,i);
		if(index==selected)
		{
			context.drawText(client.textRenderer,"×",this.getX()+width-7,y+1,0xFF5555,false);
		}
	}
	private int getTextureY(int index) {
		int i = 1;
		if (!this.active) {
			i = 0;
		} else if (hovered==index || selected==index) {
			i = 2;
		}

		return 46 + i * 20;
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {

	}

	public void setState(int selected, int count) {
		this.layersCount=count;
		this.selected=selected;
	}

	public int getSelected() {
		return this.selected;
	}

	public static interface LayerSelectEvent{
		public void onLayerAdded(int layerIndex);
		public void onLayerRemoved(int layerIndex);
		public void onLayerSelected(int layerIndex);
	}
}
