package com.example.sleepmovementtracker;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private static final float NS2S = 1.0E-9f;
    private String TAG = "MainActivity";
    private ArrayList<float[]> accel;
    private Button button;
    private boolean isRecording;
    private Sensor sensor;
    private SensorManager sensorManager;
    private float timestamp;
    private TextView x;
    private TextView y;
    private TextView z;
    private String currentFileTime;


    //TODO
    //Fix WakeLock.release, currently crashes the app???
    PowerManager powerManager;
    PowerManager.WakeLock wakeLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"myapp:recording");

        //Appends the save file every 30 seconds with the acceleration data
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                appendSaveFile();
                handler.postDelayed(this, 30000);
            }
        }, 20000);

        this.sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        this.sensor = this.sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        this.isRecording = false;
        this.button = findViewById(R.id.button);
        this.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isRecording){
                    sensorManager.registerListener(MainActivity.this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
                    button.setText("Stop Recording");
                    accel= new ArrayList<>();
                    isRecording = true;
                    currentFileTime = Calendar.getInstance().getTime().toString().substring(0,20)
                            .replaceAll(":","_").replace(" ","");
                    wakeLock.acquire((long)(3.6E7));
                    saveData();
                }

                else if (isRecording){
                    sensorManager.unregisterListener(MainActivity.this);
                    button.setText("Start Recording");
                    isRecording = false;
                    Log.d(TAG, "onClick wakeLock: "+wakeLock.isHeld());
                    if (wakeLock.isHeld())
                        wakeLock.release();
                    //sendData();
                }
            }
        });
    }


    private void sendData() {
        saveData();
        String email = "smjaejin@gmail.com";
        String subject = "Sleep Data";
        String body = "See attached.";
        String chooserTitle = "Send Email";

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + email));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, body);

        Uri filePathUri = null;
        File file = getApplicationContext().getFileStreamPath("sleepMovementData.txt");
        Log.d(TAG, "sendData: "+file.exists());

        try{
            Log.d(TAG, "readData: "+
                    new BufferedReader(new FileReader(file)).readLine());
        }catch (Exception e){
            e.printStackTrace();
        }

        try {
            Log.d(TAG, "sendData: inside the try statement");
            filePathUri = FileProvider.getUriForFile(this,
                    "com.example.sleepmovementtracker.fileprovider", file);

        }
        catch (Exception e){
            e.printStackTrace();
            Log.d(TAG, "sendData: ERROR");
        }
        Log.d(TAG, "sendData: "+filePathUri);
        emailIntent.putExtra(Intent.EXTRA_STREAM, filePathUri);
        emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        emailIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        startActivity(Intent.createChooser(emailIntent, chooserTitle));
    }

    private void saveData() {
        Gson gson = new Gson();
        String filename= "sleepMovementData" +currentFileTime+".txt";
        String fileContents = gson.toJson(accel);
        Log.d(TAG, "saveData: "+fileContents);

        try{
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(MainActivity.this.openFileOutput(filename
                    ,Context.MODE_PRIVATE));
            outputStreamWriter.write(fileContents);
            outputStreamWriter.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void appendSaveFile(){
        Gson gson = new Gson();
        String filename= "sleepMovementData" +currentFileTime+".txt";
        String fileContents = gson.toJson(accel);

        try{
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(MainActivity.this.openFileOutput(filename
                    ,Context.MODE_APPEND));
            outputStreamWriter.write(fileContents);
            outputStreamWriter.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
        accel.clear();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (timestamp != 0) {
            final float dT = (event.timestamp - timestamp) * NS2S;
            float axisX = event.values[0];
            float axisY = event.values[1];
            float axisZ = event.values[2];
            accel.add(new float[]{axisX,axisY,axisZ, timestamp});
        }
        checkTimeForSave();
    }

    private void checkTimeForSave() {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onPause() {
        appendSaveFile();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        appendSaveFile();
        wakeLock.release();
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        appendSaveFile();
        super.onStop();
    }
}
