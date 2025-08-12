package com.diamssword.characters.fabric.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIParsing;
import org.w3c.dom.Element;

import java.util.Map;

public class SeparatorComponent extends BaseComponent {
    private boolean vertical=false;
    private int color=0xff606560;

    public SeparatorComponent(Sizing sizing) {
      this.sizing(sizing);
    }
    public SeparatorComponent(Sizing horizontal, Sizing vertical) {
        this.sizing(horizontal,vertical);
    }
    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        RenderSystem.enableDepthTest();
        var matrices = context.getMatrices();
        matrices.push();
        matrices.translate(x, y, 0);
        if(vertical)
            context.drawLine(this.width/2,1,this.width/2,this.height-1,2,Color.ofArgb(color));
        else
            context.drawLine(1,this.height/2,this.width-1,this.height/2,2,Color.ofArgb(color));
        matrices.pop();
    }
    public SeparatorComponent vertical(boolean vertical)
    {
        this.vertical=vertical;
        return this;
    }
    public SeparatorComponent color(int color) {
        this.color = color;
        return this;
    }
    @Override
    public void parseProperties(UIModel model, Element element, Map<String, Element> children) {
        super.parseProperties(model, element, children);
        UIParsing.apply(children, "color", Color::parseAndPack, this::color);
        this.vertical(element.getAttribute("vertical").equals("true"));
    }
    public static SeparatorComponent parse(Element element) {

        return new SeparatorComponent(Sizing.fill(100));
    }
}
