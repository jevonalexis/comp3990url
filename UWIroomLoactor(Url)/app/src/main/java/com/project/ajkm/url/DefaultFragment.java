package com.project.ajkm.url;

//import android.app.Fragment;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

/**
 * Created by Jevon on 24/03/2015.
 */
public class DefaultFragment extends Fragment {
    ImageView iv_logo;
    //RippleButton btn_searchRoom;
    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState){
        return inflater.inflate(R.layout.default_fragment,container,false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        iv_logo= (ImageView) getActivity().findViewById(R.id.iv_logo);
    }

}
