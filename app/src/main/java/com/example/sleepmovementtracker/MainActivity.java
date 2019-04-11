package com.example.sleepmovementtracker;

import android.arch.core.executor.TaskExecutor;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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
        this.x = (TextView) findViewById(R.id.textViewX);
        this.y = (TextView) findViewById(R.id.textViewY);
        this.z = (TextView) findViewById(R.id.textViewZ);
        this.isRecording = false;
        this.button = (Button) findViewById(R.id.button);
        this.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRecording){

                }

                if (isRecording){

                }
            }
        });


    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (timestamp != 0) {
            final float dT = (event.timestamp - timestamp) * NS2S;
            float axisX = event.values[0];
            float axisY = event.values[1];
            float axisZ = event.values[2];
        }
        timestamp = event.timestamp;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
