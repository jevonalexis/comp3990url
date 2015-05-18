package com.project.ajkm.url;


import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.xgc1986.ripplebutton.widget.RippleButton;

/**
 * Created by Jevon on 25/03/2015.
 */
public class BuildingFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    RippleButton btn_searchBuild;
    Spinner spinner_building;
    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState){
        return inflater.inflate(R.layout.building_fragment_layout,container,false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        spinner_building= (Spinner) getActivity().findViewById(R.id.spinner_building);
        btn_searchBuild= (RippleButton) getActivity().findViewById(R.id.btn_searchBuild);
        spinner_building.setBackgroundColor(getResources().getColor(R.color.white_pressed));
        spinner_building.setPrompt("Choose a Building");
        setupSpinner();
        btn_searchBuild.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(getActivity().getApplicationContext(),SearchResult.class);
                Bundle extras=new Bundle();
                extras.putInt("searchType",2);
                String building_name=spinner_building.getSelectedItem().toString();
                extras.putString("building", building_name);
                i.putExtras(extras);
                ConnectivityManager check = (ConnectivityManager)
                        getActivity().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                Boolean connected=false;
                NetworkInfo networkInfo = check.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    startActivity(i);
                    connected=true;
                }
                if(!connected)
                    Toast.makeText(getActivity().getApplicationContext(),"No Internet Connection",Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void setupSpinner(){
        Resources res = getResources();
        String[] buildings = res.getStringArray(R.array.buildings);
        ArrayAdapter<String> adapter_bldng = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_list_item_1, buildings);
        spinner_building.setAdapter(adapter_bldng);
        spinner_building.setPrompt("Select a Building");
        spinner_building.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
