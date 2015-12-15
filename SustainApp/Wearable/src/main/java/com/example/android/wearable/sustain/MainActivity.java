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
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.example.android.wearable.datalayer.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import static com.example.android.wearable.sustain.DataLayerListenerService.LOGD;

/**
 * The main activity with a view pager, containing three pages:<p/>
 * <ul>
 * <li>
 * Page 1: shows a list of DataItems received from the phone application
 * </li>
 * <li>
 * Page 2: shows the photo that is sent from the phone application
 * </li>
 * <li>
 * Page 3: includes two buttons to show the connected phone and watch devices
 * </li>
 * </ul>
 */
public class MainActivity extends Activity implements ConnectionCallbacks,
        OnConnectionFailedListener, DataApi.DataListener, MessageApi.MessageListener,
        NodeApi.NodeListener {

    private static final String TAG = "MainActivity";

    private GoogleApiClient mGoogleApiClient;
    private Handler mHandler;

    private String temperature;
    private String humidity;
    private String fanstate;
    private String vwc;

    private static final String UPDATE_SUSTAIN_DATA = "/update_sustain_data";

    Node mNode;

    View.OnClickListener handler = new View.OnClickListener()
    {
        public void onClick(View v)
        {
            sendMessage();
        }
    };

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        mHandler = new Handler();
        setContentView(R.layout.discovery_fragment);
        Button b1 = (Button) findViewById(R.id.update_button);
        b1.setOnClickListener(handler);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //setupViews();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    /**
     * Send message to mobile handheld
     */
    private void sendMessage() {

        if (mNode != null && mGoogleApiClient!=null && mGoogleApiClient.isConnected()) {
            Wearable.MessageApi.sendMessage(
                    mGoogleApiClient, mNode.getId(), UPDATE_SUSTAIN_DATA, null).setResultCallback(
                    new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                            if (!sendMessageResult.getStatus().isSuccess()) {
                                Log.e("TAG", "Failed to send message with status code: "
                                        + sendMessageResult.getStatus().getStatusCode());
                            }
                        }
                    }
            );
        }else{
            //Improve your code
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Wearable.DataApi.removeListener(mGoogleApiClient, this);
        Wearable.MessageApi.removeListener(mGoogleApiClient, this);
        Wearable.NodeApi.removeListener(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        LOGD(TAG, "onConnected(): Successfully connected to Google API client");
        Wearable.DataApi.addListener(mGoogleApiClient, this);
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
        Wearable.NodeApi.addListener(mGoogleApiClient, this);
        resolveNode();
    }

    /*
     * Resolve the node = the connected device to send the message to
     */
    private void resolveNode() {
        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult nodes) {
                for (Node node : nodes.getNodes()) {
                    mNode = node;
                }
            }
        });
    }

    @Override
    public void onConnectionSuspended(int cause) {
        LOGD(TAG, "onConnectionSuspended(): Connection to Google API client was suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.e(TAG, "onConnectionFailed(): Failed to connect, with result: " + result);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        LOGD(TAG, "onDataChanged(): " + dataEvents);

        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                String path = event.getDataItem().getUri().getPath();
                if (DataLayerListenerService.TEMPERATURE_PATH.equals(path))
                {
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                    temperature = dataMapItem.getDataMap().getString(DataLayerListenerService.TEMPERATURE_PATH);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "Setting temperature params to "+temperature);
                            TextView currentView = (TextView) findViewById(R.id.temperatureText);
                            currentView.setText(temperature+"°F");
                        }
                    });
                }
                if (DataLayerListenerService.HUMIDITY_PATH.equals(path))
                {
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                    humidity = dataMapItem.getDataMap().getString(DataLayerListenerService.HUMIDITY_PATH);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "Setting humidity params to "+humidity);
                            TextView currentView = (TextView) findViewById(R.id.humidityText);
                            currentView.setText(humidity+"%");
                        }
                    });
                }

                if (DataLayerListenerService.FANSTATE_PATH.equals(path))
                {
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                    fanstate = dataMapItem.getDataMap().getString(DataLayerListenerService.FANSTATE_PATH);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "Setting fan state params to "+fanstate);
                            TextView currentView = (TextView) findViewById(R.id.fanstateText);
                            currentView.setText(fanstate);
                        }
                    });
                }

                if (DataLayerListenerService.VWC_PATH.equals(path))
                {
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                    vwc = dataMapItem.getDataMap().getString(DataLayerListenerService.VWC_PATH);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "Setting fan state params to "+vwc);
                            TextView currentView = (TextView) findViewById(R.id.vwcText);
                            currentView.setText(vwc+"vol/vol");
                        }
                    });
                }
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                //generateEvent("DataItem Deleted", event.getDataItem().toString());
            } else {
                //generateEvent("Unknown data event type", "Type = " + event.getType());
            }
        }
    }

    @Override
    public void onMessageReceived(MessageEvent event) {
        LOGD(TAG, "onMessageReceived: " + event);
        //generateEvent("Message", event.toString());
    }

    @Override
    public void onPeerConnected(Node node) {
        //generateEvent("Node Connected", node.getId());
    }

    @Override
    public void onPeerDisconnected(Node node) {
        //generateEvent("Node Disconnected", node.getId());
    }
}
