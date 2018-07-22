package com.example.vinay.a163050054_app;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

public class RecordService extends Service  {
    public RecordService() {
    }

    RecordThread recordThread;
    Thread thread;
    int check;
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d("RecordService","service start");

        recordThread = new RecordThread();



        SensorsData sensorsData = new SensorsData(this);
        recordThread.setSensorsData(sensorsData);
        thread = new Thread(recordThread);
        thread.start();

        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        Log.d("RecordService","service destroy");

        recordThread.setRunningFlag(false);
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }
}
