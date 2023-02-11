package com.dji.sdk.sample.demo.logdata;


import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;

import android.os.HandlerThread;
import android.os.IBinder;
import androidx.annotation.Nullable;

import com.dji.sdk.sample.internal.controller.DJISampleApplication;
import com.dji.sdk.sample.internal.utils.ModuleVerificationUtil;
import com.dji.sdk.sample.internal.utils.ToastUtils;

import dji.sdk.flightcontroller.FlightController;


public class ThreadHandler extends Service {
    private NaviairMessage message = new NaviairMessage(30,"NAV_EKCH","D510293","MaviacAir");

    private FlightController flightController = null;
    private double latitude;
    private double longitude;
    private double altitude;
    private double heading;

    private Looper serviceLooper;
    private ServiceHandler serviceHandler;

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            for (int i =0;i<10;i++) {
                try {
                    /*
                    latitude = flightController.getState().getAircraftLocation().getLatitude();
                    longitude = flightController.getState().getAircraftLocation().getLongitude();
                    altitude = flightController.getState().getAircraftLocation().getAltitude();
                    heading = flightController.getCompass().getHeading();

                    message.sendToNaviarJSON2(getApplicationContext(), altitude,longitude,latitude,heading);

                     */
                    message.sendToNaviarJSON2(getApplicationContext(),3.3,3.2,3.3,44.8);
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // Restore interrupt status.
                    Thread.currentThread().interrupt();
                }
            }
            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            stopSelf(msg.arg1);
        }
    }


    @Override
    public void onCreate(){
        //initFlightController();
        HandlerThread thread = new HandlerThread("ServiceStartArguments",Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        Message msg = serviceHandler.obtainMessage();
        msg.arg1 = startId;
        serviceHandler.sendMessage(msg);

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        // stopping the process
        message.setStopSending();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void initFlightController(){

        if (flightController == null) {
            if (ModuleVerificationUtil.isFlightControllerAvailable()) {
                flightController = DJISampleApplication.getAircraftInstance().getFlightController();
                ToastUtils.setResultToToast("Flight Controller initialized");
            }
        }
    }

}