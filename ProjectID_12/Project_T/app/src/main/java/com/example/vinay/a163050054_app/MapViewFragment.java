package com.example.vinay.a163050054_app;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;

import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;



/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MapViewFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MapViewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapViewFragment extends Fragment implements OnMapReadyCallback {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    GoogleMap googleMap;
    Thread getLocationThread;
    boolean running;
    private OnFragmentInteractionListener mListener;

    public MapViewFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MapFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MapViewFragment newInstance(String param1, String param2) {
        MapViewFragment fragment = new MapViewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }



    private class GetLocation extends AsyncTask<Void, Void, Void> {


        ArrayList<Pair<Double, Double>> busLocations;
        ArrayList<Marker> Markerlist;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            busLocations = new ArrayList<>();
            Markerlist=new ArrayList<>();
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            while (running) {

                RequestQueue queue = Volley.newRequestQueue(getContext());
                String url = "http://dumy.pythonanywhere.com/data/location";


                // Request a string response from the provided URL.
                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                // Display the first 500 characters of the response string.

                                Log.d("Mapviewfragment", "response:" + response);

                                String jsonStr = response;


                                if (jsonStr != null) {
                                    try {
                                        JSONObject jsonObj = new JSONObject(jsonStr);

                                        // Getting JSON Array node
                                        JSONArray busLocation = jsonObj.getJSONArray("busLocation");
                                        //int loc_list=busLocation.length();
                                        for (int j=0; j<Markerlist.size(); j++){
                                            Markerlist.get(j).remove();
                                        }
                                        Markerlist.clear();

                                        // looping through All Contacts
                                        //busLocations.clear();
                                        for (int i = 0; i < busLocation.length(); i++) {
                                            Marker now;
                                            JSONObject c = busLocation.getJSONObject(i);
                                            Double latitude = c.getDouble("Lat");
                                            Double longitude = c.getDouble("Long");
                                            //busLocations.add(new Pair<Double, Double>(latitude,longitude));
                                            LatLng bus = new LatLng(latitude, longitude);
                                            now=googleMap.addMarker(new MarkerOptions().position(bus).title("bus" + String.valueOf(i)));
//                                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.))));
                                            Markerlist.add(now);
                                        }
                                    } catch (final JSONException e) {
                                        Log.e("", "Json parsing error: " + e.getMessage());
                                    }

                                } else {
                                    Log.e("TAG", "Couldn't get json from server.");

                                }


                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("SendLocationService", error.toString());
                    }
                });
                // Add the request to the RequestQueue.
                queue.add(stringRequest);

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            Log.d("MapFragment","onPostExecute");
        }

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        SupportMapFragment supportMapFragment;


        supportMapFragment = ((SupportMapFragment)getChildFragmentManager().findFragmentById(R.id.map));
        supportMapFragment.getMapAsync(this);


        //gets location from server and updates map fragment
        new GetLocation().execute();  // No need during data collection

        running =true;
        getLocationThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(running){
                    // to be implemented;
                }
            }
        });

    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onMapReady(GoogleMap map) {

        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.setBuildingsEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(true);


        LatLng mainB = new LatLng(19.132390, 72.914885);
        map.addMarker(new MarkerOptions().position(mainB).title("IITB Main building").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).alpha(0.0f));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(mainB,16));
        map.setPadding(0,0,0,140);
        googleMap = map;

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_map, container, false);

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onPause(){
        super.onPause();
        //running =false;
        Log.d("MapViewFragment","Paused");
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
