/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.wearable.sustain;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.ph1ash.sustain.Sustain;
import com.example.android.wearable.datalayer.R;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

/**
 * Receives its own events using a listener API designed for foreground activities. Updates a data
 * item every second while it is open. Also allows user to take a photo and send that as an asset
 * to the paired wearable.
 */
public class MainActivity extends Activity{

    private GoogleApiClient mGoogleApiClient;

    private Sustain updater = new Sustain();

    View.OnClickListener handler = new View.OnClickListener()
    {
        public void onClick(View v)
        {
            if(mGoogleApiClient != null)
            {
                updater.updateSustainData();
                updateStats();
            }
            else
            {
                Log.e("MobileSustain", "Google API Client = null. Cannot proceed to update");
            }
        }
    };

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.main_activity);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
        Button button = (Button) findViewById(R.id.update_button);
        button.setOnClickListener(handler);

        //Update API client to begin operations
        updater.setGoogleApiClient(mGoogleApiClient);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void updateStats()
    {
        TextView currentView = (TextView) findViewById(R.id.temperatureText);
        currentView.setText(updater.getTemperature()+"°F");
        currentView = (TextView) findViewById(R.id.humidityText);
        currentView.setText(updater.getHumidity()+"%");
        currentView = (TextView) findViewById(R.id.fanstateText);
        currentView.setText(updater.getFanState());
    }
}
