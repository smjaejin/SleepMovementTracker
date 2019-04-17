package com.example.sleepmovementtracker;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        this.sensor = this.sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        this.x = findViewById(R.id.textViewX);
        this.y = findViewById(R.id.textViewY);
        this.z = findViewById(R.id.textViewZ);
        this.isRecording = false;
        this.button = findViewById(R.id.button);
        this.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRecording){
                    Log.d(TAG, "onClick: " + !isRecording);
                    sensorManager.registerListener(MainActivity.this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
                    button.setText("Stop Recording");
                    accel= new ArrayList<>();
                    isRecording = true;
                }

                else if (isRecording){
                    Log.d(TAG, "onClick: "+isRecording);
                    sensorManager.unregisterListener(MainActivity.this);
                    button.setText("Start Recording");
                    isRecording = false;
                    sendData();
                    Log.d(TAG, "onClick: "+getFilesDir());
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

        String filename="sleepMovementData.txt";
        String fileContents = gson.toJson(accel);
        Log.d(TAG, "saveData: "+fileContents);
        FileOutputStream outputStream;

        try {
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(fileContents.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (timestamp != 0) {
            final float dT = (event.timestamp - timestamp) * NS2S;
            float axisX = event.values[0];
            float axisY = event.values[1];
            float axisZ = event.values[2];
            accel.add(new float[]{axisX,axisY,axisZ, timestamp});
            x.setText(""+event.values[0]);
            y.setText(""+event.values[1]);
            z.setText(""+event.values[2]);
        }
        timestamp = event.timestamp;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
