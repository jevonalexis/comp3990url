package com.project.ajkm.url;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SearchResult extends AppCompatActivity{
    private Context ctx=this;
    private ListView lv_results;
    private Toolbar toolbar;
    private myAdapter myAdapt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);
        toolbar= (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar()!=null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setElevation(7);
            getSupportActionBar().setLogo(getResources().getDrawable(R.drawable.ic_launcher));
        }
        Bundle extras=getIntent().getExtras();
        String key,typeString;
        int type=extras.getInt("searchType");
        if(type==1) {
            key = extras.getString("room");
            typeString="room";
        }
        else if(type==2) {
            key = extras.getString("building");
            typeString="building";
        }
        else {
            key = extras.getString("faculty");
            typeString="faculty";
        }
        lv_results= (ListView) findViewById(R.id.lv_resultlist);

        ArrayList<ListRow> list= new ArrayList<ListRow>();
        makeRequest(typeString, key, list);
        myAdapt= new myAdapter(this,list);
        lv_results.setAdapter(myAdapt);
        lv_results.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final ListRow selectedRoom = (ListRow) parent.getAdapter().getItem(position);
                SweetAlertDialog dialog = new SweetAlertDialog(ctx, SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                    .setTitleText("Room: " + selectedRoom.roomName)
                    .setContentText("Faculty: " + selectedRoom.fac + "\nBuilding: " + selectedRoom.build)
                    .setCancelText("close")
                    .showCancelButton(true)
                    .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            sDialog.dismiss();
                        }
                    })
                    .setConfirmText("View on map")
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            Intent i = new Intent(ctx, Map.class);
                            Bundle extras = new Bundle();
                            extras.putDouble("lat", selectedRoom.lat);
                            extras.putDouble("lng", selectedRoom.lng);
                            extras.putString("name", selectedRoom.roomName);
                            //extras.putString("desc",selectedRoom.desc);
                            i.putExtras(extras);
                            startActivity(i);
                        }
                    });
                dialog.setCancelable(true);
                dialog.show();
            }
        });
    }


    //inner class to represent the info in a row of the listview
    class ListRow{
        String roomName,build,fac;
        Double lat,lng;
        ListRow(String roomName,Double lat,Double lng, String fac, String build){
            this.roomName=roomName;
            this.fac=fac;
            this.build=build;
            this.lng=lng;
            this.lat=lat;
        }

        @Override
        public String toString() {
            return "ListRow{" + "roomName='" + roomName + '\'' + ", build='" + build + '\'' +
                    ", fac='" + fac + '\'' + ", lat=" + lat + ", lng=" + lng + '}';
        }
    }


    //searches server via 'typeString' for rooms with matching 'key' and stores results in 'list'
    public void makeRequest (String type,String key,final ArrayList<ListRow> list){
        final ProgressDialog progressDialog=new ProgressDialog(ctx);
        progressDialog.setMessage("loading...");

        String base_url = "http://hosangproject.herokuapp.com/";
        String url=base_url+type+"/"+key;
        url=url.replaceAll(" ", "%20");

        RequestQueue queue=VolleySingleton.getInstance().getRequestQueue();
        JsonArrayRequest getReq = new JsonArrayRequest(Request.Method.GET, url,
            new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    progressDialog.dismiss();
                    myAdapt.notifyDataSetChanged(); // update the listview when the data is found
//                    Log.d("Get request", response.toString());
                    try {
                        if(response.length()==0)
                            Toast.makeText(ctx,"No Rooms Found",Toast.LENGTH_LONG).show();
                        else{
                            for(int i = 0; i < response.length(); i++) {
                                JSONObject room = (JSONObject) response.get(i);
                                ListRow temp=new ListRow(room.getString("room"),Double.parseDouble(room.getString("latitude")),
                                        Double.parseDouble(room.getString("longitude")),
                                        room.getString("faculty"),room.getString("building"));
                                list.add(temp);
                            }
                        }
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(ctx,"Error loading rooms",Toast.LENGTH_LONG).show();
                    }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    progressDialog.dismiss();
                    Toast.makeText(ctx,"Connection Error",Toast.LENGTH_LONG).show();
                }
            }
        );
        queue.add(getReq);
        progressDialog.show();
    }


    class myAdapter extends BaseAdapter {
        ArrayList<ListRow> list;
        Context ctx;
        myAdapter(Context context,ArrayList<ListRow> list) {
            ctx=context;
            this.list=list;
        }

        @Override
        public int getCount() {
            return list.size();
        }
        @Override
        public Object getItem(int position) {
            return list.get(position);
        }
        @Override
        public long getItemId(int position) {
            return position;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater= (LayoutInflater) ctx.getSystemService(LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listview_row,parent,false);
            TextView roomname=(TextView) convertView.findViewById(R.id.tv_roomname);
            TextView faculty=(TextView) convertView.findViewById(R.id.tv_faculty);
            TextView building=(TextView) convertView.findViewById(R.id.tv_building);
            ImageView image=(ImageView) convertView.findViewById(R.id.iv_image);
            ListRow temp= list.get(position);

            if(temp.roomName==null) roomname.setText("room name is blank");
            else   roomname.setText(temp.roomName);

            if(temp.fac==null) faculty.setText("faculty name is blank");
            else faculty.setText(temp.fac);

            if (temp.build==null) building.setText("building name is blank");
            else building.setText(temp.build);

            image.setImageResource(R.drawable.map_icon);
            return convertView;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search_result, menu);
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
}
