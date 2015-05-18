package com.project.ajkm.url;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.speech.tts.TextToSpeech;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.enums.SnackbarType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;


public class Map extends FragmentActivity {
    private LocationManager locationManager;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    protected Location mLastLocation;
    private Context ctx=this;
    private Polyline line;
    private LatLng destination;
    private TextToSpeech textToSpeech;
    private boolean gotLocation = false;
    private ArrayList<Landmark> globalLandmarks= new ArrayList<Landmark>();
    private Marker mMyMarker;
    private int next_landmark = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        setUpMapIfNeeded();
        textToSpeech = new TextToSpeech(getApplicationContext(),
                new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if (status != TextToSpeech.ERROR)
                            textToSpeech.setLanguage(Locale.UK);
                            textToSpeech.setSpeechRate((float) 1.2);
                            //textToSpeech.setPitch((float)1.1);
                    }
                }
        );
    }

    @Override
    protected void onDestroy() {
        if(textToSpeech !=null){
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        locationManager.removeUpdates(locationListener);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    @Override
    protected void onStop() {
        if(textToSpeech !=null){
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        locationManager.removeUpdates(locationListener);
        super.onStart();
    }

    @Override
    public void onPause(){
        if(textToSpeech !=null){
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onPause();
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0 , 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
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

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {

        mMap.setMyLocationEnabled(false);
        Bundle extras = getIntent().getExtras();
        destination = new LatLng(extras.getDouble("lat"),extras.getDouble("lng"));
        CameraUpdate cameraUpdate= CameraUpdateFactory.newLatLngZoom(destination, 18);
        mMap.animateCamera(cameraUpdate);
        mMap.addMarker(new MarkerOptions().position(destination).title(extras.getString("name"))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.endflag)));
    }

    private void checkLandMarks(){
            String msg="";
            boolean show = false;
            //if at start position
            if(next_landmark == -1){
                next_landmark = 0;
                msg = "Go "+ getDirection(globalLandmarks.get(next_landmark).getLoc()) +" to "+globalLandmarks.get(next_landmark).description ;
                show = true;
            }
            else {
                Location next_landmark_loc = new Location(mLastLocation);
                next_landmark_loc.setLongitude(globalLandmarks.get(next_landmark).getLoc().longitude);
                next_landmark_loc.setLatitude(globalLandmarks.get(next_landmark).getLoc().latitude);

                Location end_loc = new Location(mLastLocation);
                end_loc.setLatitude(destination.latitude);
                end_loc.setLongitude(destination.longitude);

                //notify if at destination
                if (mLastLocation.distanceTo(end_loc) < 4) {
                    msg = "You have arrived at your destination";
                    show = true;
                    locationManager.removeUpdates(locationListener);
                }

                //if 'next land mark' is the last and we are there then go to destination
                else if (next_landmark == globalLandmarks.size() - 1 && mLastLocation.distanceTo(next_landmark_loc) < 6) {
                    msg = "Go " + getDirection(destination) + " to your destination";
                    show = true;
                }

                //if at 'next land mark' go to next one
                else if (mLastLocation.distanceTo(next_landmark_loc) < 6) {
                    next_landmark++;
                    msg = "Go " + getDirection(globalLandmarks.get(next_landmark).getLoc()) + " to " + globalLandmarks.get(next_landmark).description;
                    show = true;
                }

                else
                    //if user at any other then landmark recalculate 'next landmark'
                    for (int i = next_landmark; i < globalLandmarks.size() -1; i++) {
                        Location tempLoc = new Location(mLastLocation);
                        tempLoc.setLongitude(globalLandmarks.get(i).getLoc().longitude);
                        tempLoc.setLatitude(globalLandmarks.get(i).getLoc().latitude);

                        //if at ith land mark make i+1 the 'next landmark'
                        if (mLastLocation.distanceTo(tempLoc) < 6) {
                            next_landmark = i + 1;
                            msg = "Go " + getDirection(globalLandmarks.get(next_landmark).getLoc()) + " to " + globalLandmarks.get(next_landmark).description;
                            show = true;
                        }
                    }
            }
            if(show) {
                showSnackBar(msg,5000);
                textToSpeech.speak(msg, TextToSpeech.QUEUE_FLUSH, null);
            }
    }

    public void checkForDestination(){
        Location end_loc = new Location(mLastLocation);
        end_loc.setLatitude(destination.latitude);
        end_loc.setLongitude(destination.longitude);

        //notify if at destination
        if (mLastLocation.distanceTo(end_loc) < 4) {
            String msg = "You have arrived at your destination";
            showSnackBar(msg,3000);
            textToSpeech.speak(msg, TextToSpeech.QUEUE_FLUSH, null);
            locationManager.removeUpdates(locationListener);
        }
    }

    LocationListener locationListener = new LocationListener() {

        public void onLocationChanged(Location location) {
            mLastLocation=location;
            if (mLastLocation != null) {
                //remove and redraw marker for current location
                if(mMyMarker!=null)
                    mMyMarker.remove();
                mMyMarker = mMap.addMarker(new MarkerOptions().title("Me")
                    .position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.me)));

                //if 1st time location is being set
                if(!gotLocation){
                    gotLocation = true;
                    getLandMarks(globalLandmarks, destination);

                }
                //check where to go
                if(globalLandmarks.size() > 0)
                    checkLandMarks();
                else
                    checkForDestination();

            }
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {}

        public void onProviderEnabled(String provider) {}

        public void onProviderDisabled(String provider) {}
    };

    //Creates a polyline from current location to a destination and displays the distance to it
    private void drawPolyline(ArrayList<Landmark> list){
        //if there is an existing line remove it
        Log.e("list",list.size()+"");
        if(line!=null)
            line.remove();
        //since this would be called in the listener mLastLocation would not be null
        LatLng myLoc= new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());
        //create line and add it to the map
        PolylineOptions p = new PolylineOptions();
        for(int i=0;i<list.size();i++){
            if(i==0)
                p.add(myLoc,list.get(i).getLoc()).color(getResources().getColor(R.color.polyline)).width(6);
            if(i==list.size()-1)
                p.add(list.get(i).getLoc(),destination).color(getResources().getColor(R.color.polyline)).width(6);
            else if(i>0 && i<list.size()-1)
                p.add(list.get(i-1).getLoc(),list.get(i).getLoc()).color(getResources().getColor(R.color.polyline)).width(6);
        }
        line= mMap.addPolyline(p);
    }

    private void showSnackBar(String msg, int duration){
        SnackbarManager.show(
                Snackbar.with(getApplicationContext())
                        .type(SnackbarType.MULTI_LINE)
                        .text(msg)
                        .textColor(getResources().getColor(R.color.white))
                        .swipeToDismiss(true)
                        .duration(duration)
                        .color(getResources().getColor(R.color.dark_grey))
                , Map.this);
    }

    private String getDirection(LatLng destination){
        Location destLoc = new Location(mLastLocation);
        destLoc.setLatitude(destination.latitude);
        destLoc.setLongitude(destination.longitude);

       double dist = mLastLocation.distanceTo(destLoc);
        String unit;
        if(dist>1000){
            unit = " km ";
            dist/=1000;
        }
        else unit= " metres ";
        dist = Math.round(dist * 100.0) / 100.0;
        return dist+unit+" "+bearing2Dir(mLastLocation.bearingTo(destLoc));
    }

    private String bearing2Dir(float bearing){
        if(bearing>=350 && bearing<=360 || bearing>=0 && bearing<=10) return "North";
        if(bearing>10 && bearing <80) return "North-East";
        if(bearing>=80 && bearing<=100) return "East";
        if(bearing>100 && bearing<170) return "South-East";
        if(bearing>=170 && bearing<=190) return "South";
        if(bearing>190 && bearing<260) return "South-West";
        if(bearing>=260 && bearing<=280) return "West";
        if(bearing>280 && bearing<350)return "North-West";
        return "";
    }

    private void getLandMarks(final ArrayList<Landmark> landmarkList, LatLng destination){
        String url = "http://hosangproject.herokuapp.com/path/";
        url += mLastLocation.getLatitude() +"/"+ mLastLocation.getLongitude()+"/"+destination.latitude+"/" +destination.longitude;
        RequestQueue queue = VolleySingleton.getInstance().getRequestQueue();
/*        JSONObject body = new JSONObject();
        try {
            body.put("startlat", mLastLocation.getLatitude());
            body.put("startlong", mLastLocation.getLongitude() );
            body.put("endlat", end.latitude);
            body.put("endlong", end.longitude);
        }
        catch (JSONException e){
            e.printStackTrace();
        }*/
        JsonArrayRequest landmarks_request= new JsonArrayRequest(Request.Method.GET,url,
            new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    if(response.length()>0){
                        String msg = "Use the landmarks as a guide to get to your destination";
                        showSnackBar(msg,3000);
                        /*SnackbarManager.show(
                                Snackbar.with(getApplicationContext())
                                        .type(SnackbarType.MULTI_LINE)
                                        .text(msg)
                                        .duration(3000)
                                        .swipeToDismiss(true)
                                        .textColor(getResources().getColor(R.color.white))
                                        .color(getResources().getColor(R.color.dark_grey))
                                , Map.this);*/
                        textToSpeech.speak(msg, TextToSpeech.QUEUE_FLUSH, null);

                        try {
                            Thread.sleep(3000);
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }
                        for(int i=0;i<response.length();i++){
                            try {
                                JSONObject temp = (JSONObject) response.get(i);
                                String name = temp.getString("landmark");
                                double lat = temp.getDouble("latitude");
                                double lng = temp.getDouble("longitude");
                                String desc = temp.getString("description");
                                landmarkList.add(new Landmark(lat,lng,name,desc));
                            }
                            catch (JSONException e){
                                e.printStackTrace();
                            }
                        }
                        showLandMarkers(landmarkList);

                        //drawPolyline(landmarkList);
                    }
                    else
                        Toast.makeText(ctx,"No landmarks found", Toast.LENGTH_LONG).show();
                }
            },
            new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error.getMessage()!=null)
                    Log.e("volleyerror",error.getMessage());
            }
        });
        queue.add(landmarks_request);
    }

    private void showLandMarkers(ArrayList<Landmark> landmarks){
        for(int i=0;i<landmarks.size();i++){
            Landmark temp = landmarks.get(i);
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(temp.getLoc());
            markerOptions.title(temp.getDescription());
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.landmarksmall));
            mMap.addMarker(markerOptions);
        }
    }

    private class Landmark{
        private LatLng loc;
        private String name;
        private String description;

        public Landmark(double lat, double lng, String name, String description){
            this.name = name;
            this.description = description;
            this.loc = new LatLng(lat,lng);
        }

        public LatLng getLoc() {
            return loc;
        }
        public String getName() {
            return name;
        }
        public String getDescription() {
            return description;
        }
    }
}
