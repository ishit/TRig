package com.example.android.nsdchat;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NsdHelper {

    Context mContext;

    private static NsdHelper mNsdHelper;
    private NsdManager mNsdManager;
    private NsdManager.ResolveListener mResolveListener;
    private NsdManager.DiscoveryListener mDiscoveryListener;
    private NsdManager.RegistrationListener mRegistrationListener;

    public static final String SERVICE_TYPE = "_http._tcp.";
    private List<String> servicesList = new ArrayList<String>();
    private List<NsdServiceInfo> serviceInfoList = new ArrayList<NsdServiceInfo>();

    public static final String TAG = "NsdHelper";
    public String mServiceName = "NsdChat" + new Random().nextInt(50) + 1;

    private NsdServiceInfo mService;

    private NsdHelper(Context context) {
        mContext = context;
        mNsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
    }

    public static NsdHelper getInstance(Context context) {
        if (mNsdHelper == null) {
            mNsdHelper = new NsdHelper(context);
        }

        return mNsdHelper;
    }


    public List<String> getServices() {
        return servicesList;
    }

    public List<NsdServiceInfo> getServiceInfoList() {
        return serviceInfoList;
    }

    public void setChosenServiceInfo(NsdServiceInfo service) {
        mNsdManager.resolveService(service, mResolveListener);
        return;
    }

    public void initializeDiscoveryListener() {
        mDiscoveryListener = new NsdManager.DiscoveryListener() {

            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                Log.d(TAG, "Service discovery success :" + service.getServiceName());

                if (!servicesList.contains(service.getServiceName())) {
                    servicesList.add(service.getServiceName());
                    serviceInfoList.add(service);
                }

//                mNsdManager.resolveService(service, mResolveListener); //only need to resolve when service selected by users
                if (!service.getServiceType().equals(SERVICE_TYPE)) {
                    Log.d(TAG, "Unknown Service Type: " + service.getServiceType());
                } else if (service.getServiceName().equals(mServiceName)) {
                    Log.d(TAG, "Same machine: " + mServiceName);
                } else if (service.getServiceName().contains("NsdChat")) {
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                Log.e(TAG, "service lost " + service);
                if (mService == service) {
                    mService = null;
                }
                //remove the old lost services
                if (servicesList.contains(service.getServiceName())) {
                    servicesList.remove(service.getServiceName());
                    serviceInfoList.remove(service);
                }
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(TAG, "Discovery stopped: " + serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code: " + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code: " + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }
        };
    }

    public void initializeResolveListener() {
        mResolveListener = new NsdManager.ResolveListener() {
            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.e(TAG, "Resolve failed" + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                Log.e(TAG, "Resolve Succeeded. " + serviceInfo);

                if (serviceInfo.getServiceName().equals(mServiceName)) {
                    Log.d(TAG, "Same IP.");
//                    return;
                }
                mService = serviceInfo;
            }
        };
    }

    public void initializeRegistrationListener() {
        mRegistrationListener = new NsdManager.RegistrationListener() {

            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                mServiceName = NsdServiceInfo.getServiceName();
                Log.d(TAG, "Registered: " + mServiceName);
                Toast toast = Toast.makeText(NsdChatActivity.getContext(), "Registered as " + mServiceName, Toast.LENGTH_LONG);
                toast.show();
            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo arg0, int arg1) {
                Log.e(TAG, "Registration of service failed.");
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
                Log.d(TAG, "Service Unregistered: " + arg0.getServiceName());
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
            }

        };


    }

    public void registerService(int port) {
        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setPort(port);
        serviceInfo.setServiceName(mServiceName);
        serviceInfo.setServiceType(SERVICE_TYPE);

        //unregister the old registered service before initiating new one.
        if (mRegistrationListener != null)
            tearDown();

        initializeRegistrationListener();

        mNsdManager.registerService(
                serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);

    }

    public void discoverServices() {

        //Start a new listener only if it has not been started previously.
        if (mDiscoveryListener == null) {
            initializeResolveListener();
            initializeDiscoveryListener();    //TODO : Discovers services multiple number of times on clicking discover multiple number of times

            Log.d(TAG, "DiscoveryListener initialised");
            mNsdManager.discoverServices(
                    SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
        }
    }

    public void stopDiscovery() {
        mNsdManager.stopServiceDiscovery(mDiscoveryListener);
    }

    public NsdServiceInfo getChosenServiceInfo() {
        return mService;
    }

    public void tearDown() {
        mNsdManager.unregisterService(mRegistrationListener);
    }
}
