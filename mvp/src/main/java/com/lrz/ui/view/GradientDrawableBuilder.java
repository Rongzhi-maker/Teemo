package com.lrz.ui.view;

import android.graphics.drawable.GradientDrawable;


/**
 * Author And Date: liurongzhi on 2022/3/8.
 * Description: com.yilan.sdk.ui.view
 */
public class GradientDrawableBuilder {
    private int shape = GradientDrawable.RECTANGLE;
    private int stroke = -1;
    private int strokeColor = 0x00000000;
    private int radius = 0;
    private int solidColor = 0x00000000;
    private int[] gradientColors = null;
    private int gradientType = GradientDrawable.LINEAR_GRADIENT;

    private GradientDrawableBuilder() {

    }

    public static GradientDrawableBuilder newBuilder() {
        return new GradientDrawableBuilder();
    }


    public GradientDrawableBuilder shape(int shape) {
        this.shape = shape;
        return this;
    }

    public GradientDrawableBuilder stroke(int stroke, int color) {
        this.stroke = stroke;
        this.strokeColor = color;
        return this;
    }

    public GradientDrawableBuilder solidColor(int solidColor) {
        this.solidColor = solidColor;
        return this;
    }

    public GradientDrawableBuilder cornerRadius(int radius) {
        this.radius = radius;
        return this;
    }

    public GradientDrawableBuilder gradientColors(int[] gradientColors) {
        this.gradientColors = gradientColors;
        return this;
    }

    public GradientDrawableBuilder gradientType(int gradientType) {
        this.gradientType = gradientType;
        return this;
    }


    public GradientDrawable build() {
        GradientDrawable drawable;
        if (gradientColors != null) {
            drawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, gradientColors);
            drawable.setGradientType(gradientType);
        } else {
            drawable = new GradientDrawable();
        }
        drawable.setDither(true);
        drawable.setShape(shape);
        if (stroke > 0) {
            drawable.setStroke(stroke, strokeColor);
        }
        drawable.setCornerRadius(radius);
        if (gradientColors == null) {
            drawable.setColor(solidColor);
        }
        return drawable;
    }
}
