package com.jakebergmain.ledstrip;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.graphics.Color;


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

    SeekBar redBar, greenBar, blueBar;
    HSBSeekBar hueBar, saturationBar, brightnessBar;

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
        hueBar.setOnSeekBarChangeListener(hueBar);
        hueBar.post(new Runnable() {
            @Override
            public void run() {
                hueBar.setGradient(new int[]{0xFFFF0000, 0xFFFFFF00, 0xFF00FF00, 0xFF00FFFF,
                        0xFF0000FF, 0xFFFF00FF, 0xFFFF0000});
            }
        });

        saturationBar = rootView.findViewById(R.id.seekBarSaturation);
        saturationBar.setOnSeekBarChangeListener(saturationBar);

        brightnessBar = rootView.findViewById(R.id.seekBarBrightness);
        brightnessBar.setOnSeekBarChangeListener(brightnessBar);

        if (savedInstanceState != null) {

            final float hue = (float) savedInstanceState.getInt("hue", 0);
            final float saturation = (float) savedInstanceState.getInt("saturation", 0);
            final float brightness = (float) savedInstanceState.getInt("brightness", 0);

            saturationBar.post(new Runnable() {
                @Override
                public void run() {
                    saturationBar.setGradient(saturationBar.getSaturationGradientBoundaries(hue, brightness));
                }
            });
            brightnessBar.post(new Runnable() {
                @Override
                public void run() {
                    brightnessBar.setGradient(brightnessBar.getBrightnessGradientBoundaries(hue, saturation));
                }
            });
        } else {
            saturationBar.post(new Runnable() {
                @Override
                public void run() {
                    saturationBar.setGradient(new int[]{0xFF000000, 0xFF000000});
                }
            });

            brightnessBar.post(new Runnable() {
                @Override
                public void run() {
                    brightnessBar.setGradient(new int[]{0xFF000000, 0xFFFFFFFF});
                }
            });
        }






        // pass references to HSB seekbar objects
        hueBar.initSeekBars();
        saturationBar.initSeekBars();
        brightnessBar.initSeekBars();

        if (!deviceDetectionRun) {
            searchForDevices();
            deviceDetectionRun = true;
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Make sure to call the super method so that the states of our views are saved
        super.onSaveInstanceState(outState);
        // Save our own state now
        outState.putInt("hue", hueBar.getProgress());
        outState.putInt("saturation", saturationBar.getProgress());
        outState.putInt("brightness", brightnessBar.getProgress());
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
}