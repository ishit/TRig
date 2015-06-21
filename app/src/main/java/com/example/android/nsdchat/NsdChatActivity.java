package com.example.android.nsdchat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class NsdChatActivity extends Activity {

    NsdHelper mNsdHelper;

    private static Context context;
    private TextView mStatusView;
    private Handler mUpdateHandler;

    public static final String TAG = "NsdChat";

    ChatConnection mConnection;

    public static Context getContext() {
        return NsdChatActivity.context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "On Create");
        NsdChatActivity.context = getApplicationContext();
        setContentView(R.layout.main);
        mStatusView = (TextView) findViewById(R.id.status);

        mUpdateHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String chatLine = msg.getData().getString("msg");
                addChatLine(chatLine);
            }
        };

        mConnection = new ChatConnection(this, mUpdateHandler);
        mNsdHelper = NsdHelper.getInstance(this);
        PreferenceManager.setDefaultValues(this, R.xml.preference, false);

        if (mConnection.getLocalPort() > -1) {
            mNsdHelper.registerService(mConnection.getLocalPort());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        getActionBar().setTitle("TRig");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.settings:
                openSettings();
                break;
            default:
                break;
        }
        return true;
    }

    private void openSettings() {
        Intent settingsIntent = new Intent(this, UserSettingsActivity.class);
        startActivity(settingsIntent);
        return;
    }

    public void clickAdvertise(View v) {
        // Register service
        if (mConnection.getLocalPort() > -1) {
            mNsdHelper.registerService(mConnection.getLocalPort());
        } else {
            Log.d(TAG, "ServerSocket isn't bound.");
        }
    }

    public void clickDiscover(View v) throws InterruptedException {
        mNsdHelper.discoverServices();
        Intent intent = new Intent(NsdChatActivity.this, DiscoverActivity.class);
        startActivity(intent);

    }
    

    public void clickCamera(View v) {
        Intent intent = new Intent(this, CameraView.class);
        startActivity(intent);
    }

    public void Connect() {
        NsdServiceInfo service = mNsdHelper.getChosenServiceInfo();
        if (service != null) {
            Log.d(TAG, "Connecting.");
            mConnection.connectToServer(service.getHost(),
                    service.getPort());
        } else {
            Log.d(TAG, "No service to connect to!");
        }
    }

    public void clickSend(View v) {
        EditText messageView = (EditText) this.findViewById(R.id.chatInput);
        if (messageView != null) {
            String messageString = messageView.getText().toString();
            if (!messageString.isEmpty()) {
                mConnection.sendMessage(messageString);
            }
            messageView.setText("");
        }
    }

    public void addChatLine(final String line) {
        Log.d(TAG, "Added Chat Line:" + line);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mStatusView.append("\n" + line);
            }
        });
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "On Pause");
//        if (mNsdHelper != null) {
//            mNsdHelper.stopDiscovery();
//        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "On Stop");

        //mConnection.tearDown();

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "On Resume");
        Intent intent = getIntent();
        if (intent.getIntExtra("selected", 0) == 1)
            Connect();
        getIntent().removeExtra("selected");
//        if (mNsdHelper != null) {
//            mNsdHelper.discoverServices();
//        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "On Destroy");
        if (mNsdHelper != null)
            mNsdHelper.tearDown();  //close all services when app closed
        if (mConnection != null)
            mConnection.tearDown();
        super.onDestroy();
    }
}

