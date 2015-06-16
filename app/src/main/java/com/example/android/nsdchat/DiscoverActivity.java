package com.example.android.nsdchat;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdServiceInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DiscoverActivity extends Activity {

    public ArrayAdapter<String> servicesAdapter;
    private final String LOG_TAG = "DiscoverActivity";
    public List<String> services;
    ActionBar.Tab discoverTab, connectedTab;
    Fragment discoverFragment = new DiscoverFragment();
    Fragment connectedFragment = new ConnectedFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover);
        /**
         * Initialize Tabs
         * */
        ActionBar actionbar = getActionBar();
        actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        discoverTab = actionbar.newTab().setText("Discover");
        connectedTab = actionbar.newTab().setText("Connected");

        discoverTab.setTabListener(new DiscoverTabListener(discoverFragment));
        connectedTab.setTabListener(new DiscoverTabListener(connectedFragment));

        actionbar.addTab(discoverTab);
        actionbar.addTab(connectedTab);
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

    private class RefreshDiscoveredList extends AsyncTask<Void, Void, List<String>> {

        @Override
        protected List<String> doInBackground(Void... voids) {
//            try {
//                Thread.sleep(1000);                 //1000 milliseconds is one second.
//            } catch (InterruptedException ex) {
//                Thread.currentThread().interrupt();
//            }
            Log.e(LOG_TAG, "Size " + NsdHelper.getInstance(getApplicationContext()).getServices().size());

            return NsdHelper.getInstance(getApplicationContext()).getServices();
        }

        @Override
        protected void onPostExecute(List<String> servicesList) {
            if (services != null) {
                services.clear();
                services.addAll(servicesList);
                servicesAdapter.notifyDataSetChanged();
                Log.d(LOG_TAG, "List Changed");
            }
        }
    }

    @SuppressLint("ValidFragment")
    public class DiscoverFragment extends Fragment {

        public void onAttach(Activity activity) {
            super.onAttach(activity);
        }

        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            getActionBar().setTitle("Discover");
            View view = inflater.inflate(R.layout.tab_discover, container, false);
            new RefreshDiscoveredList().execute();

            services = new ArrayList<String>();
            servicesAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, services);
            final ListView serviceList = (ListView) view.findViewById(R.id.serviceList);
            serviceList.setAdapter(servicesAdapter);

            //Item listener
            serviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    String selectedItem = (String) serviceList.getItemAtPosition(position);
                    NsdHelper mNsdHelper = NsdHelper.getInstance(getApplicationContext());
                    for (NsdServiceInfo service : mNsdHelper.getServiceInfoList()) {
                        if (service.getServiceName().equals(selectedItem)) {
                            mNsdHelper.setChosenServiceInfo(service);
                            break;
                        }
                    }
                    Log.d(LOG_TAG, "Resolved Service: " + mNsdHelper.getChosenServiceInfo());
                    Intent intent = new Intent(DiscoverActivity.this, NsdChatActivity.class);
                    intent.putExtra("selected", selectedItem);
                    startActivity(intent);
                }
            });
            return view;
        }
    }

    @SuppressLint("ValidFragment")
    public class ConnectedFragment extends Fragment {
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            getActionBar().setTitle("Connected");
            View view = inflater.inflate(R.layout.tab_connected, container, false);
            HashMap<NsdServiceInfo, String> connectedList = (HashMap<NsdServiceInfo, String>) NsdHelper.getInstance(getActivity()).getConnectedServices();
            BaseAdapter connectedAdapter = new ConnectedAdapter(connectedList);
            ListView listview = (ListView) view.findViewById(R.id.connected_list);
            listview.setAdapter(connectedAdapter);
            return view;
        }

        public class ConnectedAdapter extends BaseAdapter {

            private HashMap<NsdServiceInfo, String> connectedList;
            private ArrayList<String> serviceNames = new ArrayList<String>();

            public ConnectedAdapter(HashMap<NsdServiceInfo, String> connectedList) {
                this.connectedList = connectedList;
                for (NsdServiceInfo service :
                        connectedList.keySet()) {
                    serviceNames.add(service.getServiceName());
                }
            }

            @Override
            public int getCount() {
                return connectedList.size();
            }

            @Override
            public Object getItem(int i) {
                return null;
            }

            @Override
            public long getItemId(int i) {
                String item = serviceNames.get(i);
                return 0;
            }

            @Override
            public View getView(int arg0, View arg1, ViewGroup arg2) {
                if (arg1 == null) {
                    LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    arg1 = inflater.inflate(R.layout.connected_list_items, arg2, false);
                }

                TextView serviceName = (TextView) arg1.findViewById(R.id.firstLine);
                TextView serviceStatus = (TextView) arg1.findViewById(R.id.secondLine);

                String service = serviceNames.get(arg0);
                serviceName.setText(service);

                String status = null;
                for (NsdServiceInfo serviceInfo :
                        connectedList.keySet()) {
                    if (service.equals(serviceInfo.getServiceName())) {
                        status = connectedList.get(serviceInfo);
                        break;
                    }

                }

                String secondLine = null;

                if (status.equals("connecting")) {
                    secondLine = "Connecting..";

                } else {
                    secondLine = "";

                }

                serviceStatus.setText(secondLine);
                Log.d(LOG_TAG, "Status " + status);
                return arg1;
            }
        }
    }

    public class DiscoverTabListener implements ActionBar.TabListener {
        Fragment fragment;

        public DiscoverTabListener(Fragment fragment) {
            this.fragment = fragment;
        }

        @Override
        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
            fragmentTransaction.replace(R.id.activity_discover, fragment);
        }

        @Override
        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
            fragmentTransaction.remove(fragment);
        }

        @Override
        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

        }
    }
}
