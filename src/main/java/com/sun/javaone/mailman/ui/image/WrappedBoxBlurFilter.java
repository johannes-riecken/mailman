package com.sun.javaone.mailman.ui.image;

import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

import com.jhlabs.image.AbstractBufferedImageOp;

/**
 * Derived from JHLabs Filters' BrushedMetalFilter.
 * This file is licensed under Apache License 2.0.
 */

public class WrappedBoxBlurFilter extends AbstractBufferedImageOp {
    private int radius = 20;

    public WrappedBoxBlurFilter() {
    }

    public BufferedImage filter(BufferedImage src, BufferedImage dst) {
        int width = src.getWidth();
        int height = src.getHeight();
        if (dst == null) {
            dst = createCompatibleDestImage(src, null);
        }
        int[] inPixels = new int[width];
        int[] outPixels = new int[width];
        for (int y = 0; y < height; y++) {
            getRGB( src, 0, y, width, 1, inPixels );
            blur(inPixels, outPixels, width, radius);
            setRGB(dst, 0, y, width, 1, outPixels);
        }
        return dst;
    }

    private static int mod(int a, int b) {
        int n = a / b;
        a -= n * b;
        if (a < 0) {
            return a + b;
        }
        return a;
    }

    public static void blur(int[] in, int[] out, int width, int radius) {
        int widthMinus1 = width - 1;
        int r2 = 2 * radius + 1;
        int tr = 0, tg = 0, tb = 0;
        for (int i = -radius; i <= radius; i++) {
            int rgb = in[mod(i, width)];
            tr += (rgb >> 16) & 0xff;
            tg += (rgb >> 8) & 0xff;
            tb += rgb & 0xff;
        }
        for (int x = 0; x < width; x++) {
            out[x] = 0xff000000 | ((tr / r2) << 16) | ((tg / r2) << 8) |
                     (tb / r2);
            int i1 = x + radius + 1;
            if (i1 > widthMinus1) {
                i1 = mod(i1, width);
            }
            int i2 = x - radius;
            if (i2 < 0) {
                i2 = mod(i2, width);
            }
            int rgb1 = in[i1];
            int rgb2 = in[i2];
            tr += ((rgb1 & 0xff0000) - (rgb2 & 0xff0000)) >> 16;
            tg += ((rgb1 & 0xff00) - (rgb2 & 0xff00)) >> 8;
            tb += (rgb1 & 0xff) - (rgb2 & 0xff);
        }
    }

    @Override
    public BufferedImage createCompatibleDestImage(BufferedImage src,
                                                   ColorModel dstCM) {
        if (dstCM == null) {
            dstCM = src.getColorModel();
        }
        return new BufferedImage(dstCM, dstCM.createCompatibleWritableRaster(
                src.getWidth(), src.getHeight()), dstCM.isAlphaPremultiplied(),
                                                  null);
    }

    @Override
    public Rectangle2D getBounds2D(BufferedImage src) {
        return new Rectangle(0, 0, src.getWidth(), src.getHeight());
    }

    @Override
    public Point2D getPoint2D(Point2D srcPt, Point2D dstPt) {
        if (dstPt == null) {
            dstPt = new Point2D.Double();
        }
        dstPt.setLocation(srcPt.getX(), srcPt.getY());
        return dstPt;
    }

    @Override
    public RenderingHints getRenderingHints() {
        return null;
    }
}
