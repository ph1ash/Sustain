package com.example.android.wearable.datalayer;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.List;

/**
 * Created by mtd9636 on 11/23/15.
 */
public class SustainService extends Service implements DataApi.DataListener,
        MessageApi.MessageListener, NodeApi.NodeListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private static final String TAG = SustainService.class.getSimpleName();
    private final Messenger mMessenger = new Messenger(new IncomingHandler(this));
    public static final int MSG_SEND_NOTIFICATION = 1;
    private String webServer = "http://ph1a5h.asuscomm.com/environment.php";

    private String humidity;
    private String temperature;
    private String fanState;

    private static final String TEMPERATURE_PATH = "/temperature";
    private static final String HUMIDITY_PATH = "/humidity";
    private static final String FANSTATE_PATH = "/fanstate";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        return START_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        mGoogleApiClient.connect();
        return mMessenger.getBinder();
    }


    /**
     * TODO: Handler for incoming messages from clients.
     */
    private static class IncomingHandler extends Handler {
        private final WeakReference<SustainService> mReference;

        IncomingHandler(SustainService service) {
            mReference = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            SustainService service = mReference.get();
            switch (msg.what) {
                case MSG_SEND_NOTIFICATION:
                    Log.d(TAG,"Message sent notification");
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }


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
            //c.setConnectTimeout(timeout);
            //c.setReadTimeout(timeout);
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
        Wearable.DataApi.putDataItem(mGoogleApiClient, requestData)
                .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                    @Override
                    public void onResult(DataApi.DataItemResult dataItemResult) {
                        Log.d(TAG, "Sending data was successful: " + dataItemResult.getStatus()
                                .isSuccess());
                    }
                });
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
