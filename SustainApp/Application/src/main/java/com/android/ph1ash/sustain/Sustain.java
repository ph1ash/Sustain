package com.android.ph1ash.sustain;

import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

/**
 * Created by dexter on 11/29/15.
 */
public class Sustain {

    String webServer = "http://ph1a5h.asuscomm.com/environment.php";

    private String humidity;
    private String temperature;
    private String fanState;
    private String vwc;

    private GoogleApiClient mClient;

    private static final String TEMPERATURE_PATH = "/temperature";
    private static final String HUMIDITY_PATH = "/humidity";
    private static final String FANSTATE_PATH = "/fanstate";
    private static final String VWC_PATH = "/vwc";

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

        String vwcHolder[] = data.split("Content</br>");
        if(vwcHolder.length > 1)
        {
            vwcHolder = vwcHolder[1].split("</br");
            vwc = vwcHolder[0].trim();
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
        Wearable.DataApi.putDataItem(mClient, requestData);
    }

    // Send data using DataMap interface
    private void sendEnvironmentData()
    {
        putDataMapString(TEMPERATURE_PATH, temperature);
        putDataMapString(HUMIDITY_PATH, humidity);
        putDataMapString(FANSTATE_PATH, fanState);
        putDataMapString(VWC_PATH, vwc);
    }

    /**
     * Retrieves given URL's HTML and then calls parser after
     */
    public void updateSustainData() {
        if(mClient != null)
        {
            String result = httpGet(webServer);
            environmentParser(result);
            sendEnvironmentData();
        }
        else
        {
            Log.e("UpdateSustain", "Incoming Google API Client = null");
        }
    }

    public String getTemperature()
    {
        return temperature;
    }

    public String getHumidity()
    {
        return humidity;
    }

    public String getFanState()
    {
        return fanState;
    }

    public String getVwc()
    {
        return vwc;
    }

    public void setGoogleApiClient(GoogleApiClient client)
    {
        mClient = client;
    }
}
