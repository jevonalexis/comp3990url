package com.project.ajkm.url;


import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
//import android.widget.Toast;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton;


public class Start extends AppCompatActivity{
    private Context ctx=this;
    private Toolbar toolbar;
    private RoomFragment roomFragment;
    private FacultyFragment facultyFragment;
    private BuildingFragment buildingFragment;
    private DefaultFragment defaultFragment;
    private FragmentManager manager;
    private FragmentTransaction transaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setElevation(7);
            getSupportActionBar().setLogo(getResources().getDrawable(R.drawable.ic_launcher));
        }
        manager = getFragmentManager();
        defaultFragment = new DefaultFragment();
        transaction = manager.beginTransaction();
        transaction.add(R.id.layout_start, defaultFragment, "defaultFragment");
        transaction.commit();
        setupButtons();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void setupButtons(){
        manager = getFragmentManager();
        ImageView icon = new ImageView(this);
        icon.setImageDrawable(getResources().getDrawable(R.drawable.search_icon1));
        FloatingActionButton actionButton = new FloatingActionButton.Builder(this)
                .setContentView(icon)
                .build();
        SubActionButton.Builder itemBuilder = new SubActionButton.Builder(this);

        ImageView itemRoom = new ImageView(this);
        itemRoom.setImageDrawable( getResources().getDrawable(R.drawable.room) );
        SubActionButton button1 = itemBuilder.setContentView(itemRoom).build();

        ImageView itemFac = new ImageView(this);
        itemFac.setImageDrawable( getResources().getDrawable(R.drawable.faculty) );
        SubActionButton button2 = itemBuilder.setContentView(itemFac).build();

        ImageView itemBuilding = new ImageView(this);
        itemBuilding.setImageDrawable( getResources().getDrawable(R.drawable.building) );
        SubActionButton button3 = itemBuilder.setContentView(itemBuilding).build();

        FloatingActionMenu actionMenu = new FloatingActionMenu.Builder(this)
                .addSubActionView(button1)
                .addSubActionView(button2)
                .addSubActionView(button3)
                .attachTo(actionButton)
                .build();
        itemRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                roomFragment = new RoomFragment();
                transaction=manager.beginTransaction();
                transaction.replace(R.id.layout_start,roomFragment);
                transaction.commit();
            }
        });
        itemFac.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                facultyFragment=new FacultyFragment();
                transaction=manager.beginTransaction();
                transaction.replace(R.id.layout_start,facultyFragment);
                transaction.commit();
            }
        });
        itemBuilding.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buildingFragment=new BuildingFragment();
                transaction=manager.beginTransaction();
                transaction.replace(R.id.layout_start, buildingFragment);
                transaction.commit();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_start, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
