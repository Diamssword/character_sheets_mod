package com.diamssword.characters.fabric.client.gui.components;

import io.wispforest.owo.ui.base.BaseParentComponent;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Size;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIParsing;
import org.apache.commons.lang3.mutable.MutableInt;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.*;

public class FreeRowGridLayout extends BaseParentComponent {

    protected final int columns;
    private boolean inverted = false;

    protected final List<Component> children = new ArrayList<>();

    protected Size contentSize = Size.zero();

    protected FreeRowGridLayout(Sizing horizontalSizing, Sizing verticalSizing, int columns) {
        super(horizontalSizing, verticalSizing);
        this.columns = columns;
    }

    @Override
    protected int determineHorizontalContentSize(Sizing sizing) {
        return this.contentSize.width() + this.padding.get().right();
    }

    @Override
    protected int determineVerticalContentSize(Sizing sizing) {
        return this.contentSize.height() + this.padding.get().bottom();
    }

    @Override
    public void layout(Size space) {
        int[] columnSizes = new int[this.columns];
        int[] rowSizes = new int[rows()];

        var childSpace = this.calculateChildSpace(space);
        for (var child : this.children) {
            if (child != null) {
                child.inflate(childSpace);
            }
        }

        this.determineSizes(columnSizes, false);
        this.determineSizes(rowSizes, true);

        var mountingOffset = this.childMountingOffset();
        var layoutX = new MutableInt(this.x + mountingOffset.width());
        var layoutY = new MutableInt(this.y + mountingOffset.height());
        var layoutYInv = new MutableInt(this.y + mountingOffset.height());

        if (inverted)
            for (var dy : rowSizes)
                layoutYInv.add(dy);
        for (int row = 0; row < rows(); row++) {
            layoutX.setValue(this.x + mountingOffset.width());
            if (inverted)
                layoutYInv.add(-rowSizes[row]);
            for (int column = 0; column < this.columns; column++) {
                int columnSize = columnSizes[column];
                int rowSize = rowSizes[row];

                this.mountChild(this.getChild((row * columns) + column), childSpace, child -> {
                    var yC = inverted ? layoutYInv.intValue() + child.margins().get().top() + this.verticalAlignment().align(child.fullSize().height(), rowSize) :
                            layoutY.intValue() + child.margins().get().bottom() + this.verticalAlignment().align(child.fullSize().height(), rowSize);

                    child.mount(
                            this,
                            layoutX.intValue() + child.margins().get().left() + this.horizontalAlignment().align(child.fullSize().width(), columnSize), yC

                    );
                });

                layoutX.add(columnSizes[column]);
            }
            layoutY.add(rowSizes[row]);
        }

        this.contentSize = Size.of(layoutX.intValue() - this.x, layoutY.intValue() - this.y);
    }

    public int rows() {
        return (int) Math.ceil(this.children.size() / (float) columns);
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        super.draw(context, mouseX, mouseY, partialTicks, delta);
        this.drawChildren(context, mouseX, mouseY, partialTicks, delta, children);
    }

    protected void determineSizes(int[] sizes, boolean rows) {
        if ((rows ? this.verticalSizing : this.horizontalSizing).get().method != Sizing.Method.CONTENT) {
            Arrays.fill(sizes, (rows ? this.height - this.padding().get().vertical() : this.width - this.padding().get().horizontal()) / (rows ? this.rows() : this.columns));
        } else {
            for (int row = 0; row < rows(); row++) {
                for (int column = 0; column < this.columns; column++) {
                    var d = (row * this.columns) + column;
                    if (d < this.children.size()) {
                        final var child = this.children.get(d);
                        if (child == null) continue;

                        if (rows) {
                            sizes[row] = Math.max(sizes[row], child.fullSize().height());
                        } else {
                            sizes[column] = Math.max(sizes[column], child.fullSize().width());
                        }
                    }
                }
            }
        }
    }

    public Component getChild(int index) {
        if (index < children.size())
            return children.get(index);
        return null;
    }

    public FreeRowGridLayout child(Component child) {
        if (child != null) {
            this.children.add(child);
            this.updateLayout();
        }
        return this;
    }

    public FreeRowGridLayout setInverted(boolean inverted) {
        this.inverted = inverted;
        this.updateLayout();

        return this;
    }

    public FreeRowGridLayout removeChild(int index) {
        var currentChild = children.get(index);
        if (currentChild != null) {
            currentChild.dismount(DismountReason.REMOVED);
            children.remove(index);
            this.updateLayout();
        }

        return this;
    }

    @Override
    public FreeRowGridLayout removeChild(Component child) {
        for (int i = 0; i < this.children.size(); i++) {
            if (Objects.equals(this.children.get(i), child)) {
                this.removeChild(i);
                break;
            }
        }
        return this;
    }

    public FreeRowGridLayout clear() {
        for (Component child : this.children) {
            child.dismount(DismountReason.REMOVED);
        }
        children.clear();
        this.updateLayout();
        return this;
    }

    @Override
    public List<Component> children() {
        return this.children;
    }

    @Override
    public void parseProperties(UIModel model, Element element, Map<String, Element> children) {
        super.parseProperties(model, element, children);

        final var components = UIParsing
                .get(children, "children", e -> UIParsing.<Element>allChildrenOfType(e, Node.ELEMENT_NODE))
                .orElse(Collections.emptyList());

        for (var child : components) {
            this.child(model.parseComponent(Component.class, child));
        }
    }

    public static FreeRowGridLayout parse(Element element) {
        UIParsing.expectAttributes(element, "columns");
        int columns = UIParsing.parseUnsignedInt(element.getAttributeNode("columns"));

        return new FreeRowGridLayout(Sizing.content(), Sizing.content(), columns);
    }
}
