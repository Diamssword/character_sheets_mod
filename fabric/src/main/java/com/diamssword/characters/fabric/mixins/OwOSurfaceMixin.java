package com.diamssword.characters.fabric.mixins;

import com.diamssword.characters.fabric.client.gui.components.Panels;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.Surface;
import io.wispforest.owo.ui.parsing.UIModelParsingException;
import io.wispforest.owo.ui.parsing.UIParsing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

@Mixin(Surface.class)
public interface OwOSurfaceMixin {

    @Inject(at = @At("HEAD"), method = "parse",remap=false,cancellable = true)
    private static void parse(Element surfaceElement, CallbackInfoReturnable<Surface> cir) {
        var children = UIParsing.<Element>allChildrenOfType(surfaceElement, Node.ELEMENT_NODE);
        var surface = Surface.BLANK;

        for (var child : children) {
            surface = switch (child.getNodeName()) {
                case "gpanel" -> surface.and(child.getAttribute("white").equalsIgnoreCase("true")? Panels.PANEL_WHITE:Panels.PANEL);
                case "panel" -> surface.and(child.getAttribute("dark").equalsIgnoreCase("true")
                        ? Surface.DARK_PANEL
                        : Surface.PANEL);
                case "tiled" -> {
                    UIParsing.expectAttributes(child, "texture-width", "texture-height");
                    yield surface.and(Surface.tiled(
                            UIParsing.parseIdentifier(child),
                            UIParsing.parseUnsignedInt(child.getAttributeNode("texture-width")),
                            UIParsing.parseUnsignedInt(child.getAttributeNode("texture-height")))
                    );
                }
                case "blur" -> {
                    UIParsing.expectAttributes(child, "size", "quality");
                    yield surface.and(Surface.blur(
                            UIParsing.parseFloat(child.getAttributeNode("quality")),
                            UIParsing.parseFloat(child.getAttributeNode("size"))
                    ));
                }
                case "options-background" -> surface.and(Surface.OPTIONS_BACKGROUND);
                case "vanilla-translucent" -> surface.and(Surface.VANILLA_TRANSLUCENT);
                case "panel-inset" -> surface.and(Surface.PANEL_INSET);
                case "tooltip" -> surface.and(Surface.TOOLTIP);
                case "outline" -> surface.and(Surface.outline(Color.parseAndPack(child)));
                case "flat" -> surface.and(Surface.flat(Color.parseAndPack(child)));
                default -> throw new UIModelParsingException("Unknown surface type '" + child.getNodeName() + "'");
            };
            cir.setReturnValue(surface);
        }
    }
}
