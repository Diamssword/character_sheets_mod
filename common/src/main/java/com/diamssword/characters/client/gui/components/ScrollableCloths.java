package com.diamssword.characters.client.gui.components;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ScrollableWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ScrollableCloths<T,U extends ButtonWidget & ScrollableCloths.HoverableElement> extends ScrollableWidget {

	public List<U> components=new ArrayList<>();
	private int colsC=6;
	private int linesC=3;
	public ScrollableCloths(int x, int y, int width, int height) {
		super(x, y, width, height, Text.literal(""));
	}

	public double getScrollY() {
		return super.getScrollY();
	}
	public ScrollableCloths<T, U> setGridSize(int cols, int lines)
	{
	this.colsC=cols;
	this.linesC=lines;
	return this;
	}
	public void setCloths(List<T> parts, Function<T,U> builder)
	{
		this.setScrollY(0);
		components.clear();
			for(var i=0;i<parts.size();i++)
			{
				var b=builder.apply(parts.get(i));
				components.add(b);
				var d=(width-2)/colsC;
				var h=(height-2)/linesC;
				b.setWidth(d);
				b.setHeight(h);
				b.setX(this.getX()+d*(i%colsC)+2);
				b.setY(this.getY()+h*(i/colsC)+2);
			}
	}

	@Override
	protected void drawBox(DrawContext context, int x, int y, int width, int height) {
		super.drawBox(context,x,y,width,height);
	}
	@Override
	protected int getContentsHeight() {
		var d=components.size()/colsC;
		if(components.size()%colsC>0)
			d=d+1;
		return d*(height/linesC);
	}

	private U getMouseComponentIndex(double mouseX, double mouseY)
	{
		if(this.isWithinBounds(mouseX, mouseY))
		{
			var m1=mouseY+super.getScrollY();
			for (var d : components) {
				if(mouseX>=d.getX() && mouseX<= d.getX()+d.getWidth() && m1>=d.getY() && m1<=d.getY()+d.getHeight())
					return  d;
			}
		}
		return null;
	}
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		var bl=super.mouseClicked(mouseX,mouseY,button);
		var c=getMouseComponentIndex(mouseX,mouseY);
		if(c!=null)
			c.onClick(mouseX,mouseY);
		return bl;
	}
	@Override
	protected double getDeltaYPerScroll() {
		return height/(double)colsC;
	}

	@Override
	protected void renderContents(DrawContext context, int mouseX, int mouseY, float delta) {
		var c1=getMouseComponentIndex(mouseX,mouseY);
		components.forEach(c-> {
			c.render(context,mouseX,mouseY,delta);
			c.setHover(c == c1);
		});
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {

	}

	public List<U> children() {
		return components;
	}

	public static interface HoverableElement {
		public void setHover(boolean hovered);
		public void setHeight(int height);
	}
}
