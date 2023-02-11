package com.dji.sdk.sample.demo.logdata;


import android.app.Activity;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.GnssMeasurement;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewDebug;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.dji.sdk.sample.R;
import com.dji.sdk.sample.internal.controller.DJISampleApplication;
import com.dji.sdk.sample.internal.utils.ModuleVerificationUtil;
import com.dji.sdk.sample.internal.utils.ToastUtils;
import com.dji.sdk.sample.internal.view.PresentableView;

import java.util.List;

import dji.common.flightcontroller.CompassState;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.flightcontroller.flightassistant.SmartTrackPositionInfo;
import dji.common.perception.POSCoordinate;
import dji.common.perception.POSLocation;
import dji.common.perception.POSStatus;
import dji.common.remotecontroller.GPSData;
import dji.internal.rtk.GNSS.RawSignal;
import dji.liveviewar.jni.GPSPos;
import dji.sdk.flightcontroller.Compass;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.flightcontroller.RTK;

//public class LogData implements PresentableView {
public class LogData extends RelativeLayout implements PresentableView{
    private TextView rtkLogInfoTV;
    private Button logDataBtn;
    private Button stopSendingBtn;
    private Button startSendingBtn;
    private Button launchAppBtn;
    private FlightController flightController = null;
    private double latitude;
    private double longitude;
    private double altitude;
    private double heading;

    //private boolean isRTKSupported = DJISampleApplication.getAircraftInstance().getFlightController().isRTKSupported();
    private RTK rtk = null;
    private boolean isNetowrkRTKSet = false;
    private boolean isCoordinateSystemSet = false;


    public LogData(Context context) {
        super(context);
        initUI(context);
        initRTK();
        initFlightController();
    }

    private void initFlightController(){

        if (flightController == null) {
            if (ModuleVerificationUtil.isFlightControllerAvailable()) {
                flightController = DJISampleApplication.getAircraftInstance().getFlightController();
                ToastUtils.setResultToToast("Flight Controller initialized");
            }
        }
    }
    private void initRTK() {
        if (ModuleVerificationUtil.isRTKAvailable()) {
            rtk = DJISampleApplication.getAircraftInstance().getFlightController().getRTK();
            ToastUtils.setResultToToast("RTK is available");
        }else {
            ToastUtils.setResultToToast("RTK is not Available");
        }
    }

    @Override
    public int getDescription() {
        return R.string.data_logger_listview_log;
    }

    @NonNull
    @Override
    public String getHint() {
        return this.getClass().getSimpleName() + ".java";
    }



    private void initUI(Context context) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.view_rtk_logging, this, true);
        rtkLogInfoTV = (TextView) findViewById(R.id.textview_logging);
        logDataBtn = (Button) findViewById(R.id.btn_single_logging);
        startSendingBtn = (Button) findViewById(R.id.btn_start_sending);
        stopSendingBtn = (Button) findViewById(R.id.btn_stop_sending);
        launchAppBtn = (Button) findViewById(R.id.btn_launch_app);

        logDataBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                latitude = flightController.getState().getAircraftLocation().getLatitude();
                longitude = flightController.getState().getAircraftLocation().getLongitude();
                altitude = flightController.getState().getAircraftLocation().getAltitude();

                heading = flightController.getCompass().getHeading();
                message.sendToNaviar(getContext().getApplicationContext(), altitude,longitude,latitude,heading);
            }
        });

        startSendingBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context,ThreadHandler.class);
                context.startService(intent);
            }
        });

        stopSendingBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        launchAppBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                startNewActivity(context,"dji.go.v4");
            }
        });

    }

    private NaviairMessage message = new NaviairMessage(30,"NAV_EKCH","D510293","MaviacAir");

    private void startNewActivity(Context context, String packageName) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent != null) {
            //activity found -> start
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                context.startActivity(intent);
            }catch(ActivityNotFoundException e)
            {
                ToastUtils.setResultToToast("App not found");
            }

        } else {
            //redirect to Play Store
            intent = new Intent(Intent.CATEGORY_LAUNCHER);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.getType();


            String title = getResources().getString(R.string.app_find_dji);
            Intent chooser = Intent.createChooser(intent, title);
            try {
                context.startActivity(chooser);
            }catch(ActivityNotFoundException e)
            {
                ToastUtils.setResultToToast("App not found");
            }
        }
    }
}

