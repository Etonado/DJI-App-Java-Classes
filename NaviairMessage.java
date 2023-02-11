package com.dji.sdk.sample.demo.logdata;


import static org.jboss.netty.handler.codec.http.HttpMethod.POST;

import android.content.Context;
import android.util.Log;


import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.dji.sdk.sample.internal.utils.ToastUtils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;


public class NaviairMessage {
    private int messageType;
    private String receiverId;
    private double speed = 20;     // delete and replace function call with speed input
    private double vSpeed = 0.2;       // delete and replace function call with vspeed input
    private double heading = 0.332;      // delete and replace function call with heading input
    private double qnh = 200;    // idk what this is, initialize it properly
    private String naviairId;
    private String model;
    private String manufacturer = "DJI";
    private int batteryStatus = 10;  // delete and replace function call with battery status input
    private boolean stopSending = false;


    private String apiKey = ""; //insert API key here!
    private HttpURLConnection client = null;

    public NaviairMessage(int messageType,String receiverID, String naviairId,String model){
        this.messageType = messageType;
        this.receiverId = receiverID;
        this.naviairId = naviairId;
        this.model = model;
    }

    public int getMessageType() {
        return messageType;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public String getNaviairId() {
        return naviairId;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setNaviairId(String naviairId) {
        this.naviairId = naviairId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }


//csv message -> when received all decimals cut off (. and , interpreted as seperators) 
    public void sendToNaviar(Context context, double altitude, double longitude, double latitude, double heading) {

        String cm = ",";
        String naviairMessageString =
                Integer.toString(messageType) + cm +
                        Long.toString(System.currentTimeMillis() / 1000L) + cm +
                        receiverId + cm +
                        speed + cm +
                        altitude + cm +
                        latitude + cm +
                        longitude + cm +
                        "111.07" + cm +
                        heading + cm +
                        qnh + cm +
                        naviairId + cm +
                        model + cm +
                        manufacturer + cm +
                        batteryStatus;
        Log.i("Naviair", naviairMessageString);

        String url ="https://api.dev-naviair-utm.dk//lwt/post-track";
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest request = new StringRequest(Request.Method.POST, url, new com.android.volley.Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                // response
                Log.i("Naviair Reply CSV:", response);
            }
        },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        Log.i("ERROR","error => "+error.toString());
                    }
                }
        ){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accept", "application/json");
                params.put("Content-Type", "application/json");
                params.put("Ocp-Apim-Subscription-Key", apiKey);

                return params;
            }

            @Override
            public String getBodyContentType() {
                return "text/csv";
            }
            @Override
            public byte[] getBody(){
                return naviairMessageString.getBytes();
            }
        };
        queue.add(request);
    }


//json message -> messages are sent correctly
    public void sendToNaviarJSON2(Context context, double altitude, double longitude, double latitude, double heading) {

        Map<String, String> params = new HashMap();
        params.put("messageType", Integer.toString(messageType));
        params.put("timestamp", Long.toString(System.currentTimeMillis() / 1000L));
        params.put("receiverId",receiverId);
        params.put("speed",Double.toString(speed));
        params.put("altitude",Double.toString(altitude));
        params.put("lat",Double.toString(latitude));
        params.put("long",Double.toString(longitude));
        params.put("vSpeed","111.07");
        params.put("heading",Double.toString(heading));
        params.put("qnh",Double.toString(qnh));
        params.put("id",naviairId);
        params.put("model",model);
        params.put("manufacturer",manufacturer);
        params.put("batteryStatus",Integer.toString(batteryStatus));

        JSONObject json = new JSONObject(params);

        String url ="https://api.dev-naviair-utm.dk//lwt/post-track";

        RequestQueue queue = Volley.newRequestQueue(context);

        JsonObjectRequest jsonRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                json,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i("Naviar Reply JSON2:",response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i("ERROR","error => "+error.toString());
                        ToastUtils.setResultToToast("Unable to reach server");
                    }
                })
        {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("accept", "application/json");
                headers.put("Content-Type", "application/json");
                headers.put("Ocp-Apim-Subscription-Key", apiKey);
                return headers;
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
            @Override
            public byte[] getBody(){
                return json.toString().getBytes();
            }



        };
        queue.add(jsonRequest);
    }


    public void setStopSending(){
        stopSending = true;
    }











}


