package com.project.ajkm.datacollection;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.xgc1986.ripplebutton.widget.RippleButton;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        AdapterView.OnItemSelectedListener{
    EditText etRoom;
    Spinner spinnerFac,spinnerBldng;
    RippleButton save,map;
    boolean loc_set = false;
    Toolbar toolbar;
    String faculty,building;
    protected Location mLastLocation;
    Context ctx=this;
    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar= (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar()!=null) {
            getSupportActionBar().setElevation(6);
            getSupportActionBar().setLogo(getResources().getDrawable(R.mipmap.ic_launcher));
        }
        initViews();
        spinnerFn();
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 1, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1, locationListener);
    }

    private void initViews(){
        etRoom =(EditText) findViewById(R.id.etRoom);
        spinnerBldng=(Spinner) findViewById(R.id.spinnerBldng);
        spinnerFac=(Spinner) findViewById(R.id.spinnerFaculty);
        save=(RippleButton) findViewById(R.id.btn_save);
        map=(RippleButton) findViewById(R.id.btn_map1);
        save.setOnClickListener(this);
        map.setOnClickListener(this);
    }

    private void spinnerFn() {
        spinnerFac.setPrompt("Select a Faculty");
        ArrayAdapter<CharSequence> adapter_fac = ArrayAdapter.createFromResource(ctx,R.array.faculties, android.R.layout.simple_spinner_item);
        adapter_fac.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFac.setAdapter(adapter_fac);
        spinnerFac.setOnItemSelectedListener(this);

        spinnerBldng.setPrompt("Select a Building");
        ArrayAdapter<CharSequence> adapter_bldng = ArrayAdapter.createFromResource(ctx,R.array.buildings, android.R.layout.simple_spinner_item);
        adapter_bldng.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBldng.setAdapter(adapter_bldng);
        spinnerBldng.setOnItemSelectedListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        locationManager.removeUpdates(locationListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(locationListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
//////////////////////////////////////ON ClICK LISTENER//////////////////////////////////
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_save:
                faculty=spinnerFac.getSelectedItem().toString();
                building=spinnerBldng.getSelectedItem().toString();
                if(isValid() && locationIsSet() && isConnected()) {
                    double mLat = mLastLocation.getLatitude();
                    double mLong = mLastLocation.getLongitude();
                    sendToSvr(etRoom.getText().toString(), mLat,mLong, faculty, building);
                    Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
                    etRoom.setText("");
                }
                break;
            case R.id.btn_map1:
                startActivity(new Intent(MainActivity.this, Map.class));
                break;
        }
    }

    private boolean isValid(){
        if(etRoom.getText().toString().equals("")){
            Toast.makeText(this, "Room name can't be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean locationIsSet(){
        if(mLastLocation == null){
            Toast.makeText(this, "Location not yet set", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean isConnected() {
        ConnectivityManager check = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        Boolean connected = false;
        NetworkInfo networkInfo = check.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            connected = true;
        }
        if (!connected)
            Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
        return connected;
    }

///////////////////////////////////////////////////////////////////////////////////////////////////


    public void sendToSvr(String name,double lat,double lng,String faculty, String building){
        String url = "http://hosangproject.herokuapp.com/save";
        RequestQueue queue=VolleySingleton.getInstance().getRequestQueue();

        JSONObject obj= new JSONObject();
        try {
            obj.put("room", name);
            obj.put("building",building);
            obj.put("faculty",faculty);
            obj.put("latitude",lat);
            obj.put("longitude",lng);
        }
        catch (JSONException e){
            e.printStackTrace();
        }

        JsonObjectRequest postRequest=new JsonObjectRequest(Request.Method.POST,url,obj,
            new Response.Listener<JSONObject>(){
                @Override
                public void onResponse(JSONObject response) {
                    Toast.makeText(ctx,"saved",Toast.LENGTH_SHORT).show();
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("ErrorResponse", error.getMessage());
                }
            }
        );
        queue.add(postRequest);
    }


/////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }
    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

///////////////////////////////////////////////////////////////////////////////////////

    LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            mLastLocation=location;
            if(!loc_set) {
                Toast.makeText(ctx, "Location set", Toast.LENGTH_LONG).show();
                loc_set = true;
            }
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {}

        public void onProviderEnabled(String provider) {}

        public void onProviderDisabled(String provider) {}
    };

}
