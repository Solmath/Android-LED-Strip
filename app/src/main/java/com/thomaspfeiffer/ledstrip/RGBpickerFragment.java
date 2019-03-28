package com.thomaspfeiffer.ledstrip;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;

import com.google.gson.Gson;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.SaturationBar;
import com.larswerkman.holocolorpicker.ValueBar;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RGBpickerFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RGBpickerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RGBpickerFragment extends Fragment implements ColorPicker.OnColorChangedListener, ColorPicker.OnHueChangedListener {

    final String LOG_TAG = RGBpickerFragment.class.getSimpleName();

    ColorPicker picker;


    public RGBpickerFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment RGBpickerFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RGBpickerFragment newInstance() {
        RGBpickerFragment fragment = new RGBpickerFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_picker_rgb, container, false);

        picker = rootView.findViewById(R.id.picker);
        SaturationBar saturationBar = rootView.findViewById(R.id.saturationbar);
        ValueBar valueBar = rootView.findViewById(R.id.valuebar);

        picker.addSaturationBar(saturationBar);
        picker.addValueBar(valueBar);

        // turn of showing the old color
        picker.setShowOldCenterColor(false);
        picker.setOnColorChangedListener(this);
        picker.setOnHueChangedListener(this);

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Make sure to call the super method so that the states of our views are saved
        super.onSaveInstanceState(outState);
        // Save our own state now
    }

    @Override
    public void onColorChanged(int color) {
        // gives the color when it's actually changed (by any bar)
        // called in setNewCenterColor()
        // TODO: call when all HSV-values should be changed
        float[] hsvColor = new float[3];
        Color.colorToHSV(color, hsvColor);

        ColorMessage msg = new ColorMessage();
        msg.setHue(hsvColor[0]);
        msg.setSetHue(true);
        msg.setSaturation(hsvColor[1]);
        msg.setSetSaturation(true);
        msg.setBrightness(hsvColor[2]);
        msg.setSetBrightness(true);

        Gson gson = new Gson();
        String json = gson.toJson(msg);
        changeColor(json);
    }

    @Override
    public void onHueChanged(float hue) {
        // gives the color when it's actually changed (by any bar)
        // called in setNewCenterColor()
        // TODO: call when only hue should be changed
        ColorMessage msg = new ColorMessage();
        msg.setHue(hue);
        msg.setSetHue(true);
        // msg.setMode(ColorMessage.STATIC_COLOR);

        Gson gson = new Gson();
        String json = gson.toJson(msg);

        changeColor(json);
    }

    /**
     * Gets color data from seekbars and starts ChangeColorTask
     * to send a packet and change the color of the LEDs
     */
    public void changeColor(String json) {
        int color = picker.getColor();

        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);

        new ChangeColorTask(getContext().getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, 0)).execute(json);
    }
}