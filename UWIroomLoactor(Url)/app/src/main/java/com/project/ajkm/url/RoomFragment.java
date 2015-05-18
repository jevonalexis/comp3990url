package com.project.ajkm.url;


import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.xgc1986.ripplebutton.widget.RippleButton;

/**
 * Created by Jevon on 24/03/2015.
 */
public class RoomFragment extends Fragment {
    EditText et_room;
    RippleButton btn_searchRoom;
    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState){
        return inflater.inflate(R.layout.room_fragment_layout,container,false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        et_room= (EditText) getActivity().findViewById(R.id.et_roomname);
        btn_searchRoom= (RippleButton) getActivity().findViewById(R.id.btn_searchRoom);
        btn_searchRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(getActivity().getApplicationContext(),SearchResult.class);
                Bundle extras=new Bundle();
                extras.putInt("searchType",1);
                String room_name=et_room.getText().toString();
                extras.putString("room",room_name);
                i.putExtras(extras);
                ConnectivityManager check = (ConnectivityManager)
                        getActivity().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
//                NetworkInfo[] info = check.getAllNetworkInfo();
                Boolean connected=false;
                /*for (int x = 0; x<info.length; x++){
                    if (info[x].getState() == NetworkInfo.State.CONNECTED){
                        startActivity(i);
                        connected = true;
                    }
                }*/
                NetworkInfo networkInfo = check.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    startActivity(i);
                    connected=true;
                }
                if(!connected)
                    Toast.makeText(getActivity().getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
