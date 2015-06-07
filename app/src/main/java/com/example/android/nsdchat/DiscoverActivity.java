package com.example.android.nsdchat;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;


public class DiscoverActivity extends Activity {

    private ArrayAdapter<String> servicesAdapter;
    private final String LOG_TAG = "DiscoverActivity";
    private List<String> services;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover);

        new RefreshList().execute();

        services = new ArrayList<String>();
        servicesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, services);
        final ListView serviceList = (ListView) findViewById(R.id.serviceList);
        serviceList.setAdapter(servicesAdapter);

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

    private class RefreshList extends AsyncTask<Void, Void, List<String>> {

        @Override
        protected  List<String> doInBackground(Void... voids) {
//            try {
//                Thread.sleep(1000);                 //1000 milliseconds is one second.
//            } catch (InterruptedException ex) {
//                Thread.currentThread().interrupt();
//            }
            Log.e(LOG_TAG, "Size " + ServicesList.getInstance().getServices().size());

            return ServicesList.getInstance().getServices();
        }

        @Override
        protected void onPostExecute(List<String> servicesList) {
            if (services != null) {
                services.clear();
                services.addAll(servicesList);
                servicesAdapter.notifyDataSetChanged();
                Log.d(LOG_TAG,"List Changed");
            }
        }
    }

    ;

}
