package com.example.android.nsdchat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;


public class DiscoverActivity extends Activity {

    private ArrayAdapter<String> servicesAdapter;
//    NsdHelper mNsdHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover);

        Intent i = getIntent();
        String[] servicesArray = i.getStringArrayExtra("servicesArray");
        servicesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, servicesArray);

        ListView serviceList = (ListView) findViewById(R.id.serviceList);
        serviceList.setAdapter(servicesAdapter);

        //Log.d("disvoveraoity", servicesArray[0]);

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
