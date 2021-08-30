/**
 * Copyright (c) 2006, Sun Microsystems, Inc
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *   * Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above
 *     copyright notice, this list of conditions and the following 
 *     disclaimer in the documentation and/or other materials provided 
 *     with the distribution.
 *   * Neither the name of the TimingFramework project nor the names of its
 *     contributors may be used to endorse or promote products derived 
 *     from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


package appscripter;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author sky
 */
public class TranslucentLabel extends JLabel {
    private float alpha = 1f;
    private BufferedImage image;
    private Color fg;
    private int kw;
    private int kh;
    private float blurFactor;
    private int shiftW;
    private int shiftH;
    private boolean drawBlur;
    
    public TranslucentLabel() {
        init();
    }

    public TranslucentLabel(String message) {
        super(message);
        init();
    }
    
    private void init() {
        kw = kh = 5;
        blurFactor = .015f;
        shiftW = -1;
        shiftH = -3;
        fg = Color.BLACK;
        setOpaque(false);
        setBorder(new EmptyBorder(0, 5, 2, 5));
        drawBlur = true;
    }
    
    public void setDrawBlur(boolean drawBlur) {
        this.drawBlur = drawBlur;
        repaint();
    }
    
    public boolean getDrawBlur() {
        return drawBlur;
    }
    
    public void setShiftW(int shiftW) {
        this.shiftW = shiftW;
        image = null;
        revalidate();
        repaint();
    }
    
    public int getShiftW() {
        return shiftW;
    }
    
    public void setShiftH(int shiftH) {
        this.shiftH = shiftH;
        image = null;
        revalidate();
        repaint();
    }
    
    public int getShiftH() {
        return shiftH;
    }
    
    public Dimension getPreferredSize() {
        return super.getPreferredSize();
    }
    
    public void setKernelWidth(int width) {
        this.kw = width;
        image = null;
        repaint();
    }
    
    public int getKernelWidth() {
        return kw;
    }
    
    public void setKernelHeight(int height) {
        this.kh = height;
        image = null;
        repaint();
    }
    
    public int getKernelHeight() {
        return kh;
    }
    
    public void setBlurFactor(float factor) {
        blurFactor = factor;
        image = null;
        repaint();
    }
    
    public float getBlurFactor() {
        return blurFactor;
    }
    
    public void setText(String text) {
        image = null;
        super.setText(text);
    }
    
    protected void paintComponent(Graphics g) {
        float alpha = getAlpha();
        Composite oldC = null;
        if (alpha != 1f) {
            oldC = ((Graphics2D)g).getComposite();
            ((Graphics2D)g).setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, alpha));
        }
        if (!drawBlur) {
            super.paintComponent(g);
        } else {
            updateImageIfNecessary();
            if (image != null) {
                g.drawImage(image, 0, 0, null);
            }
        }
        if (alpha != 1f) {
            ((Graphics2D)g).setComposite(oldC);
        }
    }
    
    private void updateImageIfNecessary() {
        String text = getText();
        if (text != null && !" ".equals(text)) {
            if (image == null || image.getWidth() != getWidth() ||
                    image.getHeight() != getHeight()) {
                updateImage();
            }
        }
    }
    
    private void updateImage() {
        image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        fg = Color.BLACK;
        Graphics g = image.getGraphics();
        g.setFont(getFont());
        getUI().paint(g, this);
        g.dispose();
        
        float[] kernelData = new float[kw * kh];
        for (int i = 0; i < kernelData.length; i++) {
            kernelData[i] = blurFactor;
        }
        ConvolveOp blur = new ConvolveOp(new Kernel(kw, kh, kernelData));
        BufferedImage targetImage = new BufferedImage(getWidth(), getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        ((Graphics2D)targetImage.getGraphics()).drawImage(image, blur, -shiftW, -shiftH);

//        fg = Color.WHITE;
        g = targetImage.getGraphics();
        g.setFont(getFont());
        getUI().paint(g, this);
        g.dispose();
        image = targetImage;
    }

    public Color getForeground() {
        return fg;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
        repaint();
    }
    
    public float getAlpha() {
        return alpha;
    }
    
    public boolean isOpaque() {
        return (super.isOpaque() && (getAlpha() == 1f));
    }
}
