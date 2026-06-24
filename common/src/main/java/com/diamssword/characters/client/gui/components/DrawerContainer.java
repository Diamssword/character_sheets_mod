package com.diamssword.characters.client.gui.components;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ScrollableWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class DrawerContainer<T,U extends ButtonWidget & ScrollableCloths.HoverableElement> extends ScrollableWidget {
	public List<U> components=new ArrayList<>();
	private int colsC=6;
	private int linesC=3;
	private int compSize;
	private final int maxWidth;
	private final int maxHeight;
	public DrawerContainer(int x, int y, int compSize,int maxWidth,int maxHeight) {
		super(x,y,maxWidth,maxHeight,Text.literal(""));
		this.compSize=compSize;
		this.maxHeight=maxHeight;
		this.maxWidth=maxWidth;
	}
	public void setContent(List<T> content, Function<T,U> builder)
	{
		this.setScrollY(0);
		components.clear();

		for(T t : content) {
			var b = builder.apply(t);
			components.add(b);

		}
		refreshElementsPos();
	}
	private void refreshElementsPos()
	{
		var tot=(components.size()*compSize)+4;
		colsC= (int) ((maxWidth/(double)tot)*components.size());
		if(colsC<=0)
			colsC=1;
		this.width=(colsC*compSize)+4;
		if((maxWidth/(double)tot)<1 )
		{
			linesC= (int) Math.ceil(1d/Math.max(0.00001,(maxWidth/(double)tot)));
			this.height=Math.min(maxHeight,(linesC*compSize)+4);
		}
		else {
			this.height = compSize + 4;
			linesC=1;
		}
		for(var i=0;i<components.size();i++)
		{
			var b=components.get(i);
			b.setWidth(compSize);
			b.setHeight(compSize);
			b.setX(getX()+compSize*(i%colsC)+2);

			b.setY(getY()+compSize*(i/colsC)+2);
		}
	}
	@Override
	public void setX(int x) {
		super.setX(x);
		refreshElementsPos();
	}

	@Override
	public void setY(int y) {
		super.setY(y);
		refreshElementsPos();
	}

	@Override
	protected int getContentsHeight() {
		return linesC*compSize+4;
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
	public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
	super.renderButton(context,mouseX,mouseY,delta);
	}

	@Override
	protected void renderContents(DrawContext context, int mouseX, int mouseY, float delta) {
		var c1=getMouseComponentIndex(mouseX,mouseY);
		components.forEach(c-> {
			c.render(context,mouseX, (int) (mouseY+getScrollY()),delta);
			if(c != c1)
				c.setHover(false);
		});
		if(c1!=null)
			c1.setHover(true);
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {

	}

	public List<U> children() {
		return components;
	}

}
