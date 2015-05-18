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
public class FacultyFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    Spinner spinner_fac;
    RippleButton btn_searchFac;
    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState){
        return inflater.inflate(R.layout.faculty_fragment_layout,container,false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        spinner_fac= (Spinner) getActivity().findViewById(R.id.spinner_faculty);
        spinner_fac.setBackgroundColor(getResources().getColor(R.color.white_pressed));
        spinner_fac.setPrompt("Choose a faculty");
        btn_searchFac= (RippleButton) getActivity().findViewById(R.id.btn_searchFac);
        setupSpinner();
        btn_searchFac.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(getActivity().getApplicationContext(),SearchResult.class);
                Bundle extras=new Bundle();
                extras.putInt("searchType",3);
                String building_name=spinner_fac.getSelectedItem().toString();
                extras.putString("faculty",building_name);
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

    private void setupSpinner(){
        Resources res = getResources();
        String[] faculties = res.getStringArray(R.array.faculties);
        ArrayAdapter<String> adapter_fac = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_list_item_1, faculties);
        adapter_fac.setDropDownViewResource(android.R.layout.simple_list_item_1);
        spinner_fac.setPrompt("Select a Faculty");
        spinner_fac.setAdapter(adapter_fac);
        spinner_fac.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
