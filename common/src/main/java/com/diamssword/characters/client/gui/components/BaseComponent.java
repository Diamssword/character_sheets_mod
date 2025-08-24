package com.diamssword.characters.client.gui.components;

import net.minecraft.client.gui.Drawable;

public abstract class BaseComponent implements Drawable {
	public int width;
	public int height;
	public int x=0;
	public int y=0;
	public Sizing sizeX;
	public Sizing sizeY;
	public BaseComponent(Sizing sizingX,Sizing sizingY)
	{
		this.sizeX=sizingX;
		this.sizeY=sizingY;
	}
	public BaseComponent(Sizing sizing)
	{
		this.sizeX=sizing;
		this.sizeY=sizing;
	}
	public void sizing(Sizing sizingX,Sizing sizingY)
	{
		this.sizeX=sizingX;
		this.sizeY=sizingY;
	}
	public void sizing(Sizing sizing)
	{
		this.sizeX=sizing;
		this.sizeY=sizing;
	}
	public void mount(int x,int y,int width,int height)
	{
		this.x=x;
		this.y=y;
		this.width=this.sizeX.inflate(width,this::determineHorizontalContentSize);
		this.height=this.sizeY.inflate(height,this::determineVerticalContentSize);
	}
	protected int determineHorizontalContentSize(Sizing sizing) {
		return this.width;
	}
	protected int determineVerticalContentSize(Sizing sizing) {
		return this.height;
	}
}
