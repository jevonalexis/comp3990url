package com.project.ajkm.datacollection;

import android.database.Cursor;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Map extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }


    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }
    protected class MarkerData{
        LatLng latLng;
        String name;
        public MarkerData(double lat,double lng,String name){
            this.name=name;
            this.latLng=new LatLng(lat,lng);
        }
    }

    public void getAllRooms(){
        String url = "http://hosangproject.herokuapp.com/data";
        RequestQueue queue=VolleySingleton.getInstance().getRequestQueue();

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                if(response.length() > 0){
                    ArrayList<MarkerData> rooms= new ArrayList<MarkerData>();
                    for(int i= 0;i < response.length(); i++){
                        try {
                            JSONObject temp= (JSONObject) response.get(i);
                            MarkerData m = new MarkerData(Double.parseDouble(temp.getString("latitude")),
                                    Double.parseDouble(temp.getString("longitude")),temp.getString("room"));
                            rooms.add(m);
                        }
                        catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                    if(rooms.size()>0){
                        showMarkers(rooms);
                    }
                }

            }
        },
        new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(request);
    }

    private void showMarkers(ArrayList<MarkerData> markers){
        for(int i=0; i<markers.size(); i++){
            MarkerData m =markers.get(i);
            MarkerOptions markerOptions = new MarkerOptions().title(m.name)
                    .position(m.latLng);
            mMap.addMarker(markerOptions);
        }
    }

    private void setUpMap() {
        mMap.setMyLocationEnabled(true);
        CameraUpdate cameraUpdate=CameraUpdateFactory.newLatLngZoom(new LatLng(10.64108047, -61.40024364), 18);
        mMap.animateCamera(cameraUpdate);
        getAllRooms();

    }
}
