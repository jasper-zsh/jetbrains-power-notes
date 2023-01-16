package tech.ztimes.powernotes.util;

import com.intellij.util.ui.JBDimension;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

public class SwingUtils {
    private final static AffineTransform AFFINE_TRANSFORM = new AffineTransform();
    private final static FontRenderContext FONT_RENDER_CONTEXT = new FontRenderContext(AFFINE_TRANSFORM, true, true);

    public static Rectangle2D getTextRectangle(String text, Font font) {
        if (StringUtils.isEmpty(text)) return null;

        return font.getStringBounds(text, FONT_RENDER_CONTEXT);
    }

    public static JBDimension createDimension(String text, Font font, int minW, int maxW, int minH, int maxH) {
        var rect = getTextRectangle(text, font);
        if (rect == null) {
            return new JBDimension(minW, minH);
        }

        var width = minW;
        if (!(rect.getWidth() < minW)) {
            if (rect.getWidth() > maxW) {
                width = maxW;
            } else {
                width = (int) rect.getWidth();
            }
        }

        var height = minH;
        if (!(rect.getHeight() < minH)) {
            if (rect.getHeight() > maxH) {
                height = maxH;
            } else {
                height = (int) rect.getHeight();
            }
        }

        return new JBDimension(width, height);
    }
}
