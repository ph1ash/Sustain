package com.example.android.wearable.sustain;

import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.android.ph1ash.sustain.Sustain;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Created by mtd9636 on 11/23/15.
 */
public class SustainService extends WearableListenerService{

    private GoogleApiClient mGoogleApiClient;

    private static final String UPDATE_SUSTAIN_DATA = "/update_sustain_data";

    private Sustain updater = new Sustain();

    @Override
    public void onMessageReceived(MessageEvent event)
    {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
        if (event.getPath().equals(UPDATE_SUSTAIN_DATA))
        {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable()
            {
                @Override
                public void run()
                {
                    Toast.makeText(SustainService.this.getApplicationContext(), "Updating Sustain Data", Toast.LENGTH_LONG).show();
                }
            });
        }
        //Update API client to begin operations
        updater.setGoogleApiClient(mGoogleApiClient);
        //Updater must be defined |after| Google API Client is populated
        updater.updateSustainData();
    }
}
