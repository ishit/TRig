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
import java.util.Arrays;
import java.util.List;


public class DiscoverActivity extends Activity {

    private ArrayAdapter<String> servicesAdapter;
    private final String LOG_TAG = "DiscoverActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover);

        new RefreshList().execute();
        String[] servicesArray = {};
        List<String> services = new ArrayList<String>(Arrays.asList(servicesArray));
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

    private class RefreshList extends AsyncTask<Void, Void, String[]> {

        @Override
        protected String[] doInBackground(Void... voids) {
//            try {
//                Thread.sleep(1000);                 //1000 milliseconds is one second.
//            } catch (InterruptedException ex) {
//                Thread.currentThread().interrupt();
//            }
            String[] servicesArray = new String[ServicesList.getInstance().getServices().size()];
            Log.e(LOG_TAG, "Size " + servicesArray.length);
            servicesArray = ServicesList.getInstance().getServices().toArray(servicesArray);
            return servicesArray;
        }

        @Override
        protected void onPostExecute(String[] services) {
            if (services != null) {
                servicesAdapter.clear();
                for (String service : services)
                    servicesAdapter.add(service);
            }
        }
    }

    ;

}
