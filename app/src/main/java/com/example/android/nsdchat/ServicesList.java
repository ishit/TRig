package com.example.android.nsdchat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ishit on 6/6/15.
 */
public class ServicesList {
    private static ServicesList ourInstance = new ServicesList();
    private List<String> services = new ArrayList<String>();

    public static ServicesList getInstance() {
        return ourInstance;
    }

    public void addService(String service) {
        services.add(service);
        return;
    }

    public List<String> getServices() {
        return services;
    }

    private ServicesList() {
    }
}
