package com.jakebergmain.ledstrip;

import android.content.Context;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.util.AttributeSet;
import android.widget.SeekBar;

public class HSBSeekBar extends SeekBar implements SeekBar.OnSeekBarChangeListener {

    SeekBar redBar, greenBar, blueBar;
    HSBSeekBar hueBar, saturationBar, brightnessBar;

    public HSBSeekBar(final Context context) {
        super(context);
        // setOnSeekBarChangeListener(this);
    }

    public HSBSeekBar(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        // setOnSeekBarChangeListener(this);
    }

    public HSBSeekBar(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        // setOnSeekBarChangeListener(this);
    }

    public void initSeekBars() {
        super.onAttachedToWindow();

        redBar = getRootView().findViewById(R.id.seekBarRed);
        greenBar = getRootView().findViewById(R.id.seekBarGreen);
        blueBar = getRootView().findViewById(R.id.seekBarBlue);

        hueBar = getRootView().findViewById(R.id.seekBarHue);
        saturationBar = getRootView().findViewById(R.id.seekBarSaturation);
        brightnessBar = getRootView().findViewById(R.id.seekBarBrightness);

    }

    public void setGradient(int[] gradientColors) {
        float hueBarWidth = (float) this.getWidth();
        float hueBarPadding = (float) (this.getPaddingStart() + this.getPaddingEnd());

        LinearGradient gradient = new LinearGradient(0.f, 0.f, hueBarWidth - hueBarPadding, 0.f,
                gradientColors,
                null, Shader.TileMode.MIRROR);

        ShapeDrawable gradientRect = new ShapeDrawable(new RectShape());
        gradientRect.getPaint().setShader(gradient);

        this.setProgressDrawable(gradientRect);
    }

    public int[] getSaturationGradientBoundaries(float hue, float value){

        float[] hsv = new float[3];
        int[] colors = new int[2];

        // scale colors from 0 to 1
        hsv[0] = (hue / 255.f * 360.f);
        hsv[1] = (0.f);
        hsv[2] = (value / 255.f);

        colors[0] = Color.HSVToColor(hsv);

        hsv[1] = 1.f;
        colors[1] = Color.HSVToColor(hsv);

        return colors;
    }

    public int[] getBrightnessGradientBoundaries(float hue, float saturation){

        float[] hsv = new float[3];
        int[] colors = new int[2];

        // scale colors from 0 to 1
        hsv[0] = (hue / 255.f * 360.f);
        hsv[1] = (saturation / 255.f);
        hsv[2] = (0.f);

        colors[0] = Color.HSVToColor(hsv);

        hsv[2] = 1.f;
        colors[1] = Color.HSVToColor(hsv);

        return colors;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        float h = (float) hueBar.getProgress();
        float s = (float) saturationBar.getProgress();
        float v = (float) brightnessBar.getProgress();

        float[] hsv = new float[3];

        // scale colors from 0 to 1
        hsv[0] = (h / 255.f * 360.f);
        hsv[1] = (s / 255.f);
        hsv[2] = (v / 255.f);

        int[] saturationColors = getSaturationGradientBoundaries(h, v);
        saturationBar.setGradient(saturationColors);

        int[] brightnessColors = getBrightnessGradientBoundaries(h, s);
        brightnessBar.setGradient(brightnessColors);

        if (fromUser) {
            // HSB-bars have been changed by user --> change RGB-bars
            int r, g, b;

            int rgb = Color.HSVToColor(hsv);

            r = Color.red(rgb);
            g = Color.green(rgb);
            b = Color.blue(rgb);

            // Calculate RGB-Values from HSB-values and change RGB-seekbars
            redBar.setProgress(r);
            greenBar.setProgress(g);
            blueBar.setProgress(b);

            // workaround to ensure gradient is updated
            saturationBar.setProgress(0);
            saturationBar.setProgress((int) s);
            brightnessBar.setProgress(0);
            brightnessBar.setProgress((int) v);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }
}
