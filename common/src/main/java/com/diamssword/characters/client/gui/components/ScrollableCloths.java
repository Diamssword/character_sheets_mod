package com.diamssword.characters.client.gui.components;

import com.diamssword.characters.api.appearence.Cloth;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ScrollableWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ScrollableCloths extends ScrollableWidget {

	public List<ClothButtonComponent> components=new ArrayList<>();
	public ScrollableCloths(int x, int y, int width, int height) {
		super(x, y, width, height, Text.literal(""));
	}

	public double getScrollY() {
		return super.getScrollY();
	}
	public void setCloths(List<Cloth> cloths, Function<Cloth,ClothButtonComponent> builder)
	{
		this.setScrollY(0);
		components.clear();
			for(var i=0;i<cloths.size();i++)
			{
				var b=builder.apply(cloths.get(i));
				components.add(b);
				var d=(width-2)/6;
				var h=(height-2)/3;
				b.setWidth(d);
				b.setHeight(h);
				b.setX(this.getX()+d*(i%6)+2);
				b.setY(this.getY()+h*(i/6)+2);
			}
	}
	@Override
	protected void drawBox(DrawContext context, int x, int y, int width, int height) {
		super.drawBox(context,x,y,width,height);
	}
	@Override
	protected int getContentsHeight() {
		var d=components.size()/6;
		if(components.size()%6>0)
			d=d+1;
		return d*(height/3);
	}

	private ClothButtonComponent getMouseComponentIndex(double mouseX, double mouseY)
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
		return height/6d;
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

	public List<ClothButtonComponent> children() {
		return components;
	}
}
