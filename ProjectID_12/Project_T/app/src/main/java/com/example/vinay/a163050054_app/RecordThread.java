package com.example.vinay.a163050054_app;


import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.SensorEvent;
import android.location.Location;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static android.os.ParcelFileDescriptor.MODE_APPEND;

/**
 * Created by vinay on 1/3/18.
 */

public class RecordThread implements Runnable {


    boolean runningFlag;
    Context context;

    public void setSensorsData(SensorsData sensorsData) {
        this.sensorsData = sensorsData;
    }

    SensorsData sensorsData;

    public void setRunningFlag(boolean flag){
        runningFlag = flag;
    }



    private void writeToFile(String fileName,String text,Context context) throws IOException {

        //Log.d("writeTofile","recordThread");

        Log.d("writeTofile","record in IF"+checkpermission(Manifest.permission.WRITE_EXTERNAL_STORAGE));
        if(isExternalStorageWritable() && checkpermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ){

            File Root = Environment.getExternalStorageDirectory();
            File Dir = new File(Root.getAbsoluteFile()+ "/AVT");
            Log.d("dir",""+Dir.exists());
            if(!Dir.exists())
            {
                Dir.mkdir();
            }
            File file = new File(Dir, fileName);

            try {

                FileOutputStream stream = new FileOutputStream(file, true);
                stream.write(text.getBytes());
                stream.close();

            }
            catch(IOException e){
                e.printStackTrace();
            }


            Log.d("file pathname",file.getParent());
        }

    }


    public boolean checkpermission(String p)
    {
        int check = ContextCompat.checkSelfPermission(context, p);
        return (check == PackageManager.PERMISSION_GRANTED);
    }

    private boolean isExternalStorageWritable() {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return true;
        }
        else{
          Log.d("isExternal","not available");
            return false;
        }
    }

    public void run() {

        runningFlag = true;

        context = sensorsData.getContext();
        SharedPreferences prefs= context.getSharedPreferences("userData",Context.MODE_PRIVATE);

        StringBuilder text= new StringBuilder();
        String filename = "Recording_" + new SimpleDateFormat("HH:mm:ss.SSS").format(Calendar.getInstance().getTime()) + ".csv";

        text.setLength(0); //clear the string builder

        while(runningFlag) {

            SharedPreferences prefstag = context.getSharedPreferences("userData",Context.MODE_PRIVATE);

            Location location = sensorsData.getLocation();
            SensorEvent sensorEvent = sensorsData.getSensorEvent();


            text.append(new SimpleDateFormat("HH:mm:ss.SSS").format(Calendar.getInstance().getTime()));

            if(location != null){
                double lat = location.getLatitude();
                double lon = location.getLongitude();
                double speed= location.getSpeed();
                text.append(","+lat+","+lon+","+ speed);
            }
            else{
                text.append(",,");
            }


            if(sensorEvent == null){
                text.append(",,,");
            }
            else{
               text.append(","+sensorEvent.values[0] + ","+sensorEvent.values[1] + ","+sensorEvent.values[2]);
            }

            text.append(",");
            text.append(prefstag.getString("tag",""));
            text.append("\n");

            try {
                writeToFile(filename,text.toString(),context);
            } catch (IOException e) {
                Log.d("Thread","Error writing to file");
            }



            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.d("Thread",text.toString());

            text.setLength(0); // clear the string builder
        }

        Log.d("Thread","Exited");
    }






}
