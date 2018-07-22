package com.example.vinay.a163050054_app;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.SensorEvent;
import android.location.Location;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Scanner;
import java.util.*;//trishal
import java.lang.*;//trishal
import java.io.*;//trishal

import umich.cse.yctung.androidlibsvm.LibSVM;

public class SendLocationService extends Service {


    boolean running;
    Thread  thread;
    public SendLocationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        running = true;

        final SensorsData sensorsData = new SensorsData(this);


        thread = new Thread(new Runnable() {
            String systemPath;
            String appFolderPath;

            Context context;
            private void writeToFile(String fileName,String text,Context context) throws IOException {

                //Log.d("writeTofile","recordThread");

                Log.d("writeTofile","record in IF"+checkpermission(Manifest.permission.WRITE_EXTERNAL_STORAGE));
                if(isExternalStorageWritable() && checkpermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ){

                    File Root = Environment.getExternalStorageDirectory();
                    File Dir = new File(Root.getAbsoluteFile()+ "/AVT/Model");
                    Log.d("dir",""+Dir.exists());
                    if(!Dir.exists())
                    {
                        Dir.mkdir();
                    }
                    File file = new File(Dir, fileName);

                    try {

                        FileOutputStream stream = new FileOutputStream(file, false);
                        stream.write(text.getBytes());
                        stream.close();

                    }
                    catch(IOException e){
                        e.printStackTrace();
                    }


                    //Log.d("file pathname",file.getParent());
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

            @Override
            public void run() {
                context = sensorsData.getContext();
                String uniqueID = UUID.randomUUID().toString();
                while (running){

                    StringBuilder text= new StringBuilder();
                    StringBuilder textp= new StringBuilder();

                    Location location = sensorsData.getLocation();
                    SensorEvent sensorEvent=sensorsData.getSensorEvent();
                    String acclx,accly,acclz,speed;
                    text.setLength(0);
                    text.append(uniqueID);
                    text.append(",");
                    text.append(new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()));

                    if(location != null){
                        double lat = location.getLatitude();
                        double lon = location.getLongitude();
                        double speed1=location.getSpeed();
                        speed=String.valueOf(speed1);
                        text.append(","+lat+","+lon);
                        if(isclose(lat, lon) == false)
                        {
                            //Log.d("isclose", "false ");
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            continue;
                        }
                    }
                    else{
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        continue;
                        //text.append(",,");
                        //speed="null";
                    }

                    if(sensorEvent == null){
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        continue;
                        //acclx="";
                        //accly="";
                        //acclz="";

                    }
                    else{
                        acclx=String.valueOf(sensorEvent.values[0]);
                        accly=String.valueOf(sensorEvent.values[1]);
                        acclz=String.valueOf(sensorEvent.values[2]);

                    }
                    if((acclx.isEmpty()) || (accly.isEmpty() )|| (acclz.isEmpty()) ){
                        continue;
                    }
                    else{
                        textp.append("1 ");
                        textp.append("1:" + acclx + " ");
                        textp.append("2:" + accly + " ");
                        textp.append("3:" + acclz + " ");
                        textp.append("4:" + speed + " ");

                    }
                    textp.append("\n");

                    String filename="currentfeatures.csv";
                    try {
                        writeToFile(filename,textp.toString(),context);
                    } catch (IOException e) {
                        Log.d("Thread","Error writing to file");
                    }
                    Log.d("Thread",text.toString());


                    textp.setLength(0); // clear the string builder
                    LibSVM svm = new LibSVM();
                    systemPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" ;
                    appFolderPath = systemPath + "AVT/Model/"; // your datasets folder
                    File file1 = new File(appFolderPath+"result.csv");
                    if(file1.delete())
                    {
                        //Log.d("Deleting   ", "File deleted successfully");
                    }
                    else
                    {
                        Log.d("Deleting   ", "Failed to delete file");
                    }


                    svm.predict(appFolderPath + "currentfeatures.csv " + appFolderPath + "model " + appFolderPath + "result.csv");

                    //File file = new File("C:\\Users\\pankaj\\Desktop\\test.txt");
                    String label="";
                    try{
                        Scanner sc = new Scanner(file1);
                        while (sc.hasNextLine())
                            label=sc.nextLine();
                    } catch(IOException e){
                        e.printStackTrace();
                    }

                    Log.d("predicted ",label);

                    RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                    String url ="http://dumy.pythonanywhere.com/data/location.csv/"+text.toString();

                    Log.d("ThreadShareLoc",text.toString());
                    // Request a string response from the provided URL.
                    StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    // Display the first 500 characters of the response string.

                                    Log.d("SendLocationService","response:"+response);
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("SendLocationService",error.toString());
                        }
                    });
                    // Add the request to the RequestQueue.
                    //queue.add(stringRequest);
                    if(Integer.valueOf(label) == +1){
                        queue.add(stringRequest);
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        thread.start();

        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        Log.d("RecordService","service destroy");

        running = false;
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        super.onDestroy();
    }

    public double[] iitblat = new double[]{
            19.135535,19.135520,19.135492,19.135482,19.135433,19.135418,19.135357,19.135350,19.135324,19.135329,19.135311,19.135270,19.135242,19.135240,19.135230,19.135199,19.135184,19.135171,19.135169,19.135177,19.135144,19.135121,19.135103,19.135045,19.135047,19.135030,19.135017,19.134999,19.134994,19.134959,19.134946,19.134910,19.134875,19.134832,19.134804,19.134761,19.134761,19.134708,19.134695,19.134677,19.134655,19.134634,19.134604,19.134558,19.134541,19.134502,19.134502,19.134457,19.134421,19.134394,19.134376,19.134348,19.134320,19.134290,19.134241,19.134241,19.134193,19.134171,19.134140,19.134105,19.134069,19.134018,19.133986,19.133960,19.133922,19.133889,19.133869,19.133806,19.133760,19.133760,19.133757,19.133735,19.133697,19.133664,19.133628,19.133585,19.133547,19.133519,19.133494,19.133456,19.133441,19.133418,19.133395,19.133347,19.133322,19.133289,19.133261,19.133243,19.133230,19.133215,19.133190,19.133190,19.133177,19.133152,19.133109,19.133096,19.133045,19.133010,19.132995,19.132947,19.132931,19.132929,19.132873,19.132858,19.132843,19.132802,19.132754,19.132698,19.132650,19.132602,19.132493,19.132493,19.132470,19.132417,19.132316,19.132250,19.132202,19.132156,19.132103,19.132029,19.131948,19.131877,19.131842,19.131771,19.131702,19.131702,19.131702,19.131487,19.131383,19.131353,19.131267,19.131221,19.131158,19.131097,19.131028,19.130975,19.130919,19.130919,19.130843,19.130727,19.130676,19.130623,19.130537,19.130537,19.130402,19.130291,19.130281,19.130291,19.130326,19.130362,19.130400,19.130453,19.130499,19.130537,19.130610,19.130643,19.130689,19.130790,19.130879,19.130947,19.131003,19.131084,19.131082,19.131165,19.131244,19.131332,19.131426,19.131517,19.131548,19.131553,19.131558,19.131576,19.131586,19.131609,19.131621,19.131644,19.131644,19.131644,19.131644,
            19.137221,19.137147,19.137107,19.137048,19.137013,19.136962,19.136942,19.136876,19.136815,19.136772,19.136737,19.136701,19.136653,19.136587,19.136509,19.136458,19.136412,19.136331,19.136331,19.136311,19.136248,19.136151,19.136060,19.135987,19.135989,19.135921,19.135867,19.135796,19.135695,19.135634,19.135543,19.135477,19.135437,19.135378,19.135249,19.135168,19.135095,19.135089,19.135092,19.135016,19.134950,19.134912,19.134841,19.134790,19.134722,19.134654,19.134580,19.134529,19.134464,19.134413,19.134357,19.134296,19.134203,19.134187,19.134160,19.134089,19.134033,19.133969,19.133899,19.133802,19.133739,19.133698,19.133617,19.133554,19.133508,19.133483,19.133412,19.133394,19.133394,19.133328,19.133273,19.133222,19.133159,19.133072,19.132981,19.132976,19.132948,19.132953,19.132922,19.132897,19.132879,19.132862,19.132841,19.132811,19.132793,19.132758,19.132732,19.132699,19.132672,19.132616,19.132580,19.132530,19.132482,19.132482,19.132416,19.132332,19.132279,19.132241,19.132195,19.132155,19.132099,19.132056,19.131952,19.131909,19.131853,19.131785,19.131704,19.131698,19.131678,19.131564,19.131513,19.131437,19.131341,19.131260,19.131199,19.131111,19.131090,19.131078,19.131004,19.130936,19.130875,19.130781,19.130710,19.130634,19.130535,19.130535,19.130535,19.130535,19.130388,19.130312,19.130272,19.130272,19.130307,19.130330,19.130363,19.130401,19.130444,19.130487,19.130553,19.130629,19.130682,19.130766,19.130834,19.130915,19.130999,19.131103,19.131103,19.131133,19.131209,19.131280,19.131369,19.131435,19.131516,19.131554,19.131554,19.131564,19.131587,19.131602,19.131622,19.131630,19.131653,19.131653,
            19.137221,19.137147,19.137107,19.137048,19.137013,19.136962,19.136942,19.136876,19.136815,19.136772,19.136737,19.136701,19.136653,19.136587,19.136509,19.136458,19.136412,19.136331,19.136331,19.136311,19.136248,19.136151,19.136060,19.135987,19.135989,19.135921,19.135867,19.135796,19.135695,19.135634,19.135543,19.135477,19.135437,19.135378,19.135249,19.135168,19.135095,19.135089,19.135092,19.135016,19.134950,19.134912,19.134841,19.134790,19.134722,19.134654,19.134580,19.134529,19.134464,19.134413,19.134357,19.134296,19.134203,19.134187,19.134160,19.134089,19.134033,19.133969,19.133899,19.133802,19.133739,19.133698,19.133617,19.133554,19.133508,19.133483,19.133412,19.133394,19.133394,19.133328,19.133273,19.133222,19.133159,19.133072,19.132981,19.132976,19.132948,19.132953,19.132922,19.132897,19.132879,19.132862,19.132841,19.132811,19.132793,19.132758,19.132732,19.132699,19.132672,19.132616,19.132580,19.132530,19.132482,19.132482,19.132416,19.132332,19.132279,19.132241,19.132195,19.132155,19.132099,19.132056,19.131952,19.131909,19.131853,19.131785,19.131704,19.131698,19.131678,19.131564,19.131513,19.131437,19.131341,19.131260,19.131199,19.131111,19.131090,19.131078,19.131004,19.130936,19.130875,19.130781,19.130710,19.130634,19.130535,19.130535,19.130535,19.130535,19.130388,19.130312,19.130272,19.130272,19.130307,19.130330,19.130363,19.130401,19.130444,19.130487,19.130553,19.130629,19.130682,19.130766,19.130834,19.130915,19.130999,19.131103,19.131103,19.131133,19.131209,19.131280,19.131369,19.131435,19.131516,19.131554,19.131554,19.131564,19.131587,19.131602,19.131622,19.131630,19.131653,19.131653
    };

    public double[] iitblong = new double[]{
            72.905697, 72.905735, 72.905842, 72.905936, 72.906052, 72.906175, 72.906352, 72.906368, 72.906484, 72.906585, 72.906695, 72.906808, 72.906915, 72.906990, 72.907076, 72.907181, 72.907283, 72.907404, 72.907404, 72.907444, 72.907514, 72.907615, 72.907728, 72.907841, 72.907833, 72.907919, 72.907978, 72.908034, 72.908028, 72.908160, 72.908187, 72.908267, 72.908372, 72.908471, 72.908592, 72.908721, 72.908729, 72.908839, 72.908927, 72.908978, 72.909058, 72.909112, 72.909209, 72.909316, 72.909364, 72.909458, 72.909458, 72.909549, 72.909673, 72.909769, 72.909823, 72.909909, 72.910021, 72.910091, 72.910201, 72.910201, 72.910290, 72.910397, 72.910499, 72.910590, 72.910711, 72.910813, 72.910925, 72.910995, 72.911094, 72.911185, 72.911309, 72.911408, 72.911545, 72.911545, 72.911550, 72.911625, 72.911695, 72.911802, 72.911920, 72.912049, 72.912162, 72.912221, 72.912301, 72.912368, 72.912427, 72.912510, 72.912604, 72.912709, 72.912784, 72.912924, 72.912969, 72.913028, 72.913066, 72.913125, 72.913208, 72.913208, 72.913256, 72.913320, 72.913433, 72.913508, 72.913629, 72.913720, 72.913795, 72.913881, 72.913959, 72.914007, 72.914085, 72.914176, 72.914235, 72.914308, 72.914401, 72.914498, 72.914570, 72.914656, 72.914758, 72.914758, 72.914785, 72.914849, 72.914924, 72.914978, 72.915002, 72.915018, 72.915077, 72.915134, 72.915174, 72.915187, 72.915214, 72.915252, 72.915281, 72.915281, 72.915281, 72.915335, 72.915354, 72.915378, 72.915378, 72.915380, 72.915388, 72.915386, 72.915397, 72.915397, 72.915413, 72.915413, 72.915426, 72.915426, 72.915421, 72.915421, 72.915429, 72.915429, 72.915442, 72.915456, 72.915359, 72.915262, 72.915187, 72.915096, 72.915021, 72.914951, 72.914919, 72.914908, 72.914890, 72.914884, 72.914882, 72.914852, 72.914847, 72.914847, 72.914847, 72.914847, 72.914849, 72.914836, 72.914828, 72.914836, 72.914849, 72.914865, 72.914855, 72.914903, 72.914930, 72.914994, 72.915059, 72.915104, 72.915169, 72.915249, 72.915279, 72.915279, 72.915279,
            72.915645, 72.914986, 72.914959, 72.914951, 72.914945, 72.914956, 72.914945, 72.914937, 72.914937, 72.914919, 72.914919, 72.914908, 72.914892, 72.914865, 72.914862, 72.914844, 72.914833, 72.914817, 72.914817, 72.914803, 72.914771, 72.914739, 72.914728, 72.914715, 72.914712, 72.914669, 72.914666, 72.914632, 72.914624, 72.914597, 72.914559, 72.914548, 72.914524, 72.914514, 72.914455, 72.914449, 72.914428, 72.914425, 72.914420, 72.914379, 72.914366, 72.914358, 72.914339, 72.914334, 72.914312, 72.914288, 72.914272, 72.914259, 72.914229, 72.914224, 72.914194, 72.914170, 72.914141, 72.914160, 72.914143, 72.914101, 72.914087, 72.914055, 72.914052, 72.914023, 72.914009, 72.913993, 72.913966, 72.913956, 72.913950, 72.913937, 72.913915, 72.913897, 72.913897, 72.913867, 72.913851, 72.913846, 72.913838, 72.913808, 72.913771, 72.913832, 72.913899, 72.913961, 72.914007, 72.914063, 72.914133, 72.914200, 72.914267, 72.914291, 72.914329, 72.914396, 72.914430, 72.914492, 72.914546, 72.914607, 72.914672, 72.914739, 72.914784, 72.914784, 72.914833, 72.914897, 72.914951, 72.914967, 72.914988, 72.915031, 72.915082, 72.915109, 72.915173, 72.915200, 72.915230, 72.915249, 72.915270, 72.915273, 72.915281, 72.915324, 72.915345, 72.915361, 72.915369, 72.915375, 72.915383, 72.915385, 72.915391, 72.915383, 72.915385, 72.915401, 72.915409, 72.915423, 72.915426, 72.915423, 72.915428, 72.915428, 72.915428, 72.915426, 72.915434, 72.915436, 72.915423, 72.915289, 72.915203, 72.915139, 72.915077, 72.915021, 72.914978, 72.914919, 72.914900, 72.914903, 72.914897, 72.914897, 72.914865, 72.914846, 72.914860, 72.914846, 72.914846, 72.914841, 72.914819, 72.914827, 72.914846, 72.914849, 72.914849, 72.914857, 72.914857, 72.914878, 72.914986, 72.915045, 72.915139, 72.915208, 72.915270, 72.915278,
            72.915645, 72.914986, 72.914959, 72.914951, 72.914945, 72.914956, 72.914945, 72.914937, 72.914937, 72.914919, 72.914919, 72.914908, 72.914892, 72.914865, 72.914862, 72.914844, 72.914833, 72.914817, 72.914817, 72.914803, 72.914771, 72.914739, 72.914728, 72.914715, 72.914712, 72.914669, 72.914666, 72.914632, 72.914624, 72.914597, 72.914559, 72.914548, 72.914524, 72.914514, 72.914455, 72.914449, 72.914428, 72.914425, 72.914420, 72.914379, 72.914366, 72.914358, 72.914339, 72.914334, 72.914312, 72.914288, 72.914272, 72.914259, 72.914229, 72.914224, 72.914194, 72.914170, 72.914141, 72.914160, 72.914143, 72.914101, 72.914087, 72.914055, 72.914052, 72.914023, 72.914009, 72.913993, 72.913966, 72.913956, 72.913950, 72.913937, 72.913915, 72.913897, 72.913897, 72.913867, 72.913851, 72.913846, 72.913838, 72.913808, 72.913771, 72.913832, 72.913899, 72.913961, 72.914007, 72.914063, 72.914133, 72.914200, 72.914267, 72.914291, 72.914329, 72.914396, 72.914430, 72.914492, 72.914546, 72.914607, 72.914672, 72.914739, 72.914784, 72.914784, 72.914833, 72.914897, 72.914951, 72.914967, 72.914988, 72.915031, 72.915082, 72.915109, 72.915173, 72.915200, 72.915230, 72.915249, 72.915270, 72.915273, 72.915281, 72.915324, 72.915345, 72.915361, 72.915369, 72.915375, 72.915383, 72.915385, 72.915391, 72.915383, 72.915385, 72.915401, 72.915409, 72.915423, 72.915426, 72.915423, 72.915428, 72.915428, 72.915428, 72.915426, 72.915434, 72.915436, 72.915423, 72.915289, 72.915203, 72.915139, 72.915077, 72.915021, 72.914978, 72.914919, 72.914900, 72.914903, 72.914897, 72.914897, 72.914865, 72.914846, 72.914860, 72.914846, 72.914846, 72.914841, 72.914819, 72.914827, 72.914846, 72.914849, 72.914849, 72.914857, 72.914857, 72.914878, 72.914986, 72.915045, 72.915139, 72.915208, 72.915270, 72.915278
    };

    public double distance(double lat1, double lat2, double lon1,
                           double lon2, double el1, double el2) {

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        double height = el1 - el2;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return Math.sqrt(distance);
    }

    public boolean isclose(double lat, double lon)
    {
        int i;
        final double threshold = 5.0; //in meters
        for(i = 0; i < iitblat.length; i++)
        {
            if( distance(lat, iitblat[i], lon, iitblong[i], 14.0,14.0) < threshold )
                return true;
        }
        return false;
    }


}