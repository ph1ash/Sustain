package com.example.android.wearable.datalayer;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

/**
 * Created by mtd9636 on 11/23/15.
 */
public class SustainService extends WearableListenerService{

    private GoogleApiClient mGoogleApiClient;
    private static final String TAG = SustainService.class.getSimpleName();
    String webServer = "http://ph1a5h.asuscomm.com/environment.php";

    String humidity;
    String temperature;
    String fanState;

    private static final String TEMPERATURE_PATH = "/temperature";
    private static final String HUMIDITY_PATH = "/humidity";
    private static final String FANSTATE_PATH = "/fanstate";

    private static final String UPDATE_SUSTAIN_DATA = "/update_sustain_data";

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
            new getWebsiteDataTask().execute();
        }
    }

    @Override
    public void onDataChanged(DataEventBuffer buffer)
    {

    }

    /*@Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.d("DerpWear", "Starting Wearable Service");
        return START_STICKY;
    }*/

    // Parses the HTML by splitting data at specific delimiters
    private void environmentParser(String data)
    {
        String tempHolder[] = data.split("Temperature</br>");
        if(tempHolder.length > 1)
        {
            tempHolder = tempHolder[1].split("&deg;");
            temperature = tempHolder[0].trim();
        }

        String humidHolder[] = data.split("Humidity</br>");
        if(humidHolder.length > 1)
        {
            humidHolder = humidHolder[1].split("%");
            humidity = humidHolder[0].trim();
        }

        String fanHolder[] = data.split("State</br>");
        if(fanHolder.length > 1)
        {
            fanHolder = fanHolder[1].split("</br");
            fanState = fanHolder[0].trim();
        }

    }

    //Get HTML from given URL
    public static String httpGet(String urlpath){
        try{
            URL url = new URL(urlpath); // Your given URL.
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            c.setRequestMethod("GET");
            c.setRequestProperty("Content-length", "0");
            c.setUseCaches(false);
            c.setAllowUserInteraction(false);
            c.connect();
            int status = c.getResponseCode();

            switch (status) {
                case 200:
                case 201:
                    BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    br.close();
                    return sb.toString();
            }
            c.disconnect();
        } catch (Exception e) {
            Log.d("InputStream", "It broke!");
        }
        return "Failed";
    }

    // Used for adding string data into desired data map
    private void putDataMapString(String path, String data)
    {
        PutDataMapRequest dataMapValue = PutDataMapRequest.create(path);
        dataMapValue.getDataMap().putString(path, data);
        dataMapValue.getDataMap().putLong("time", new Date().getTime());
        PutDataRequest requestData = dataMapValue.asPutDataRequest();
        Wearable.DataApi.putDataItem(mGoogleApiClient, requestData);
                /*.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                    @Override
                    public void onResult(DataApi.DataItemResult dataItemResult) {
                        Log.d(TAG, "Sending data was successful: " + dataItemResult.getStatus()
                                .isSuccess());
                    }
                });*/
    }

    // Send data using DataMap interface
    private void sendEnvironmentData()
    {
        putDataMapString(TEMPERATURE_PATH, temperature);
        putDataMapString(HUMIDITY_PATH, humidity);
        putDataMapString(FANSTATE_PATH, fanState);
    }

    /**
     * Retrieves given URL's HTML and then calls parser after
     */
    private class getWebsiteDataTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            return httpGet(webServer);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getBaseContext(), "Received!", Toast.LENGTH_LONG).show();
            environmentParser(result);
            sendEnvironmentData();
        }
    }
}
