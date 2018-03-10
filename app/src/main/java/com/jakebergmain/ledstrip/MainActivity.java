package com.jakebergmain.ledstrip;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;

public class MainActivity extends Activity implements MainFragment.OnFragmentInteractionListener {

    // @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}