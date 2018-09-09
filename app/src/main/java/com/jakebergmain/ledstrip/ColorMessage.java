package com.jakebergmain.ledstrip;

public class ColorMessage {
    public static final int STATIC_COLOR = 1;
    public static final int ANIMATION = 2;


    private float hue = 0.f;
    private boolean setHue = false;
    private float saturation = 0.f;
    private boolean setSaturation = false;
    private float brightness = 0.f;
    private boolean setBrightness = false;
    private int mode = STATIC_COLOR;

    ColorMessage(){

    }

    ColorMessage(float hue, boolean setHue, float saturation, boolean setSaturation, float brightness, boolean setBrightness, int mode){
        this.hue = hue;
        this.setHue = setHue;
        this.saturation = saturation;
        this.setSaturation = setSaturation;
        this.brightness = brightness;
        this.setBrightness = setBrightness;
        this.mode = mode;
    }

    public void setHue(float hue) {
        this.hue = hue;
    }

    public void setSetHue(boolean setHue) {
        this.setHue = setHue;
    }

    public void setSaturation(float saturation) {
        this.saturation = saturation;
    }

    public void setSetSaturation(boolean setSaturation) {
        this.setSaturation = setSaturation;
    }

    public void setBrightness(float brightness) {
        this.brightness = brightness;
    }

    public void setSetBrightness(boolean setBrightness) {
        this.setBrightness = setBrightness;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }
}
