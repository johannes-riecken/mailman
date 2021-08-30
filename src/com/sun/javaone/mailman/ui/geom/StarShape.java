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
package com.sun.javaone.mailman.ui.geom;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class StarShape implements Shape {
    private Shape starShape;
    private double x;
    private double y;
    private double innerRadius;
    private double outerRadius;
    private int branchesCount;

    public StarShape(double x, double y,
                     double innerRadius, double outerRadius,
                     int branchesCount) {
        if (branchesCount < 2) {
            throw new IllegalArgumentException("The number of branches must be >= 3.");
        } else if (innerRadius >= outerRadius) {
            throw new IllegalArgumentException("The inner radius must be < outer radius.");
        }

        this.x = x;
        this.y = y;
        this.innerRadius = innerRadius;
        this.outerRadius = outerRadius;
        this.branchesCount = branchesCount;

        starShape = generateStar(x, y, innerRadius, outerRadius, branchesCount);
    }

    private static Shape generateStar(double x, double y,
                                      double innerRadius, double outerRadius,
                                      int branchesCount) {
        GeneralPath path = new GeneralPath();

        double outerAngleIncrement = 2 * Math.PI / branchesCount;

        double outerAngle = 0.0;
        double innerAngle = outerAngleIncrement / 2.0;

        x += outerRadius;
        y += outerRadius;

        double x1 = Math.cos(outerAngle) * outerRadius + x;
        double y1 = Math.sin(outerAngle) * outerRadius + y;

        double x2 = Math.cos(innerAngle) * innerRadius + x;
        double y2 = Math.sin(innerAngle) * innerRadius + y;

        path.moveTo(x1, y1);
        path.lineTo(x2, y2);

        outerAngle += outerAngleIncrement;
        innerAngle += outerAngleIncrement;

        for (int i = 1; i < branchesCount; i++) {
            x1 = Math.cos(outerAngle) * outerRadius + x;
            y1 = Math.sin(outerAngle) * outerRadius + y;

            path.lineTo(x1, y1);

            x2 = Math.cos(innerAngle) * innerRadius + x;
            y2 = Math.sin(innerAngle) * innerRadius + y;

            path.lineTo(x2, y2);

            outerAngle += outerAngleIncrement;
            innerAngle += outerAngleIncrement;
        }

        path.closePath();
        return path;
    }

    public void setInnerRadius(double innerRadius) {
        if (innerRadius >= outerRadius) {
            throw new IllegalArgumentException("The inner radius must be < outer radius.");
        }

        this.innerRadius = innerRadius;
        starShape = generateStar(getX(), getY(), innerRadius, getOuterRadius(), getBranchesCount());
    }

    public void setX(double x) {
        this.x = x;
        starShape = generateStar(x, getY(), getInnerRadius(), getOuterRadius(), getBranchesCount());
    }

    public void setY(double y) {
        this.y = y;
        starShape = generateStar(getX(), y, getInnerRadius(), getOuterRadius(), getBranchesCount());
    }

    public void setOuterRadius(double outerRadius) {
        if (innerRadius >= outerRadius) {
            throw new IllegalArgumentException("The outer radius must be > inner radius.");
        }

        this.outerRadius = outerRadius;
        starShape = generateStar(getX(), getY(), getInnerRadius(), outerRadius, getBranchesCount());
    }

    public void setBranchesCount(int branchesCount) {
        if (branchesCount < 2) {
            throw new IllegalArgumentException("The number of branches must be >= 3.");
        }

        this.branchesCount = branchesCount;
        starShape = generateStar(getX(), getY(), getInnerRadius(), getOuterRadius(), branchesCount);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getInnerRadius() {
        return innerRadius;
    }

    public double getOuterRadius() {
        return outerRadius;
    }

    public int getBranchesCount() {
        return branchesCount;
    }

    public Rectangle getBounds() {
        return starShape.getBounds();
    }

    public Rectangle2D getBounds2D() {
        return starShape.getBounds2D();
    }

    public boolean contains(double x, double y) {
        return starShape.contains(x, y);
    }

    public boolean contains(Point2D p) {
        return starShape.contains(p);
    }

    public boolean intersects(double x, double y, double w, double h) {
        return starShape.intersects(x, y, w, h);
    }

    public boolean intersects(Rectangle2D r) {
        return starShape.intersects(r);
    }

    public boolean contains(double x, double y, double w, double h) {
        return starShape.contains(x, y, w, h);
    }

    public boolean contains(Rectangle2D r) {
        return starShape.contains(r);
    }

    public PathIterator getPathIterator(AffineTransform at) {
        return starShape.getPathIterator(at);
    }

    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        return starShape.getPathIterator(at, flatness);
    }
}
