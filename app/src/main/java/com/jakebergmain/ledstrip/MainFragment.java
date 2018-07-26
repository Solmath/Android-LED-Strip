package com.jakebergmain.ledstrip;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.LinearGradient;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.graphics.Color;

import android.graphics.Rect;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MainFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainFragment extends Fragment implements DiscoverTask.DiscoverCallback, SeekBar.OnSeekBarChangeListener {

    final String LOG_TAG = MainFragment.class.getSimpleName();

    private OnFragmentInteractionListener mListener;

    SeekBar redBar, greenBar, blueBar, hueBar, saturationBar, brightnessBar;

    static boolean deviceDetectionRun = false;

    public MainFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MainFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MainFragment newInstance() {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        rootView.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        rootView.layout(0, 0,
                rootView.getMeasuredWidth(),
                rootView.getMeasuredHeight());

        // seekbars
        redBar = rootView.findViewById(R.id.seekBarRed);
        greenBar = rootView.findViewById(R.id.seekBarGreen);
        blueBar = rootView.findViewById(R.id.seekBarBlue);

        redBar.setOnSeekBarChangeListener(this);
        blueBar.setOnSeekBarChangeListener(this);
        greenBar.setOnSeekBarChangeListener(this);

        hueBar = rootView.findViewById(R.id.seekBarHue);

        hueBar.post(new Runnable() {
            @Override
            public void run() {
            //width is ready
            float hueBarWidth = (float) hueBar.getWidth();
            float hueBarPadding = (float) (hueBar.getPaddingStart() + hueBar.getPaddingEnd());

            LinearGradient gradient = new LinearGradient(0.f, 0.f, hueBarWidth - hueBarPadding, 0.f,

                    new int[]{0xFFFF0000, 0xFFFFFF00, 0xFF00FF00, 0xFF00FFFF,
                            0xFF0000FF, 0xFFFF00FF, 0xFFFF0000},
                    null, TileMode.MIRROR);

            ShapeDrawable gradientRect = new ShapeDrawable(new RectShape());
            gradientRect.getPaint().setShader(gradient);

            hueBar.setProgressDrawable(gradientRect);
            }
        });

        saturationBar = rootView.findViewById(R.id.seekBarSaturation);

        saturationBar.post(new Runnable() {
            @Override
            public void run() {
                //width is ready
                float saturationBarWidth = (float) saturationBar.getWidth();
                float saturationBarPadding = (float) (saturationBar.getPaddingStart() + saturationBar.getPaddingEnd());

                LinearGradient gradient = new LinearGradient(0.f, 0.f, saturationBarWidth - saturationBarPadding, 0.f,

                        new int[]{0xFF000000, 0xFF000000},
                        null, TileMode.MIRROR);

                ShapeDrawable gradientRect = new ShapeDrawable(new RectShape());
                gradientRect.getPaint().setShader(gradient);
                saturationBar.setProgressDrawable(gradientRect);
            }
        });

        brightnessBar = rootView.findViewById(R.id.seekBarBrightness);

        brightnessBar.post(new Runnable() {
            @Override
            public void run() {
                //width is ready
                float brightnessBarWidth = (float) brightnessBar.getWidth();
                float brightnessBarPadding = (float) (brightnessBar.getPaddingStart() + brightnessBar.getPaddingEnd());

                LinearGradient gradient = new LinearGradient(0.f, 0.f, brightnessBarWidth - brightnessBarPadding, 0.f,

                        new int[]{0xFF000000, 0xFFFFFFFF},
                        null, TileMode.MIRROR);


                ShapeDrawable gradientRect = new ShapeDrawable(new RectShape());
                gradientRect.getPaint().setShader(gradient);
                brightnessBar.setProgressDrawable(gradientRect);
            }
        });

        HSBSeekbarListener HSB = new HSBSeekbarListener();

        hueBar.setOnSeekBarChangeListener(HSB);
        saturationBar.setOnSeekBarChangeListener(HSB);
        brightnessBar.setOnSeekBarChangeListener(HSB);

        if (!deviceDetectionRun) {
            searchForDevices();
            deviceDetectionRun = true;
        }

        return rootView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    // methods for seek bar listener
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        if (fromUser) {
            // RGB-bars have been changed by user --> change HSB-bars
            int r = redBar.getProgress();
            int g = greenBar.getProgress();
            int b = blueBar.getProgress();

            float[] hsv = new float[3];

            Color.RGBToHSV(r, g, b, hsv);

            int h = (int) (hsv[0] / 360.f * 255.f);
            int s = (int) (hsv[1] * 255.f);
            int v = (int) (hsv[2] * 255.f);

            // Calculate RGB-Values from HSB-values and change RGB-seekbars
            hueBar.setProgress(h);
            saturationBar.setProgress(0); // workaround to ensure gradient is updated
            saturationBar.setProgress(s);
            brightnessBar.setProgress(0); // workaround to ensure gradient is updated
            brightnessBar.setProgress(v);
        }

        // colors changed so send packet to led strip
        changeColor();
    }

    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    /**
     * method for Discover task callback
     */
    public void onFoundDevice() {
        // we found a LED strip!
        // what do we do now?

        // for debug only
        SharedPreferences preferences = getContext().getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, 0);
        String ipString = preferences.getString(Constants.PREFERENCES_IP_ADDR, "");
        Log.v(LOG_TAG, "onDeviceFound() ipAddr: " + ipString);
    }


    /**
     * Start DiscoverTask to search for devices on local network.
     */
    public void searchForDevices() {
        Log.v(LOG_TAG, "starting DiscoverTask");
        new DiscoverTask(getContext(), this).execute(null, null);
    }

    /**
     * Gets color data from seekbars and starts ChangeColorTask
     * to send a packet and change the color of the LEDs
     */
    public void changeColor() {
        int r = redBar.getProgress();
        int g = greenBar.getProgress();
        int b = blueBar.getProgress();
        new ChangeColorTask(getContext().getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, 0)).execute(r, g, b);
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    /**
     * Seperate listener for HSB-bars, with its own definition of the onProgressChanged method.
     */
    private class HSBSeekbarListener implements SeekBar.OnSeekBarChangeListener {
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

            float[] hsvTemp = new float[3];
            System.arraycopy( hsv, 0, hsvTemp, 0, hsv.length );

            // saturationBar.setHueAndBrightness(h, v);
            // Rect bounds = saturationBar.getProgressDrawable().getBounds(); // can be used for LinearGradient instead of width and padding?
            float saturationBarWidth = (float) saturationBar.getWidth();
            float saturationBarPadding = (float) (saturationBar.getPaddingStart() + saturationBar.getPaddingEnd());

            // gradient from (h, 0, v) to (h, 255, v)
            hsvTemp[1] = 0.f;
            int ColorStart = Color.HSVToColor(hsvTemp);
            hsvTemp[1] = 1.f;
            int ColorEnd = Color.HSVToColor(hsvTemp);

            LinearGradient satGradient = new LinearGradient(0.f, 0.f, saturationBarWidth - saturationBarPadding, 0.f,

                    ColorStart, ColorEnd,
                    TileMode.MIRROR);

            ShapeDrawable satGradientRect = new ShapeDrawable(new RectShape());
            satGradientRect.getPaint().setShader(satGradient);
            saturationBar.setProgressDrawable(satGradientRect);
            // saturationBar.getProgressDrawable().setBounds(bounds);

            // brightnessBar.setHueAndSaturation(h, s);
            // Rect bounds = brightnessBar.getProgressDrawable().getBounds(); // can be used for LinearGradient instead of width and padding?
            float brightnessBarWidth = (float) brightnessBar.getWidth();
            float brightnessBarPadding = (float) (brightnessBar.getPaddingStart() + brightnessBar.getPaddingEnd());

            // gradient from (h, s, 0) to (h, s, 255)
            hsvTemp[1] = hsv[1];
            hsvTemp[2] = 0.f;
            ColorStart = Color.HSVToColor(hsvTemp);
            hsvTemp[2] = 1.f;
            ColorEnd = Color.HSVToColor(hsvTemp);

            LinearGradient brightGradient = new LinearGradient(0.f, 0.f, brightnessBarWidth - brightnessBarPadding, 0.f,

                    ColorStart, ColorEnd,
                    TileMode.MIRROR);

            ShapeDrawable brightGradientRect = new ShapeDrawable(new RectShape());
            brightGradientRect.getPaint().setShader(brightGradient);
            brightnessBar.setProgressDrawable(brightGradientRect);

            if (fromUser){
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
                saturationBar.setProgress((int)s);
                brightnessBar.setProgress(0);
                brightnessBar.setProgress((int)v);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    }
}