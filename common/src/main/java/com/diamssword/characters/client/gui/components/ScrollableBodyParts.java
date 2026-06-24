package com.diamssword.characters.client.gui.components;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.ScrollableWidget;
import net.minecraft.client.gui.widget.ToggleButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.*;

public class ScrollableBodyParts<T,U extends ButtonWidget & ScrollableCloths.HoverableElement> extends ScrollableWidget {

	public List<U> components=new ArrayList<>();
	private int colsC=6;
	private int linesC=3;
	private final CheckboxWidget checkboxWidget;
	private List<T> stored;
	private BiFunction<T,String,U> storedbuilder;
	private final BiFunction<Integer,Integer,Boolean> canBeHovered;
	private Consumer<Boolean> splittedListner;
	public ScrollableBodyParts(int x, int y, int width, int height, BiFunction<Integer,Integer,Boolean> canBeHoveredFn ) {
		super(x, y, width, height, Text.literal(""));
		checkboxWidget=new CheckboxWidget(getX(),getY(),getWidth(),20,Text.literal("Splitted"),false);
		this.canBeHovered=canBeHoveredFn;
	}
	private boolean splittable=false;
	private boolean splitted=false;
	public double getScrollY() {
		return super.getScrollY();
	}
	public ScrollableBodyParts<T, U> setGridSize(int cols, int lines)
	{
	this.colsC=cols;
	this.linesC=lines;
	return this;
	}
	public void setCloths(List<T> parts, BiFunction<T,String,U> builder, boolean splittable, boolean splitted)
	{
		this.stored=parts;
		this.storedbuilder=builder;
		this.splittable=splittable;
		this.splitted=splittable && splitted;
		if(this.splitted!=checkboxWidget.isChecked())
			checkboxWidget.onPress();
		this.setScrollY(0);
		components.clear();
		int baseH=this.splittable?20:0;
		for(int j=0;j<(this.splitted?2:1);j++) {
			baseH+= (int) (((getContentsHeight()/2f)+10)*j);
			for(var i = 0; i < parts.size(); i++) {
				var b = builder.apply(parts.get(i),this.splitted?(j==0?"left":"right"):null);
				components.add(b);
				var d = (width - 2) / colsC;
				var h = (height - 2) / linesC;
				b.setWidth(d);
				b.setHeight(h);
				b.setX(this.getX() + d * (i % colsC) + 2);
				b.setY(this.getY()+baseH + h * (i / colsC) + 2);
			}
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
		if(splitted)
			d*=2;
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
		if(mouseY<getY()+20 && mouseY>getY() && splittable)
			checkboxWidget.onPress();
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
	if(splittable) {
		checkboxWidget.render(context, mouseX, mouseY, delta);
		var v=checkboxWidget.isChecked();
		if(v !=splitted)
		{
			splitted=v;
			setCloths(stored,storedbuilder,splittable,splitted);
			if(splittedListner!=null)
				splittedListner.accept(splitted);
		}

	}

		var c1=getMouseComponentIndex(mouseX,mouseY);
		components.forEach(c-> {
			if(c!=c1)
				c.setHover(false);
			c.render(context,mouseX,mouseY,delta);

		});
		if(c1!=null && canBeHovered.apply(mouseX,mouseY))
			c1.setHover(true);
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {

	}
	public void setSplittedListner(Consumer<Boolean> fn)
	{
		this.splittedListner=fn;
	}
	public List<U> children() {
		return components;
	}
}
