package com.example.android.nsdchat;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class DiscoverActivity extends Activity {

//    private ArrayAdapter<String> servicesAdapter;
//    NsdHelper mNsdHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        servicesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mNsdHelper.getServices());
//        ListView serviceList = (ListView) findViewById(R.id.serviceList);
//        serviceList.setAdapter();
        setContentView(R.layout.activity_discover);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_discover, menu);
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
