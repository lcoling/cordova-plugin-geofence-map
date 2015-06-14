package com.visioncritical.cordova.geofenceMap;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by lcoling on 2015-06-13.
 */
public class Geofence {

    public static final int TRANSITION_TYPE_ENTER = 1;
    public static final int TRANSITION_TYPE_EXIT = 2;
    public static final int TRANSITION_TYPE_BOTH = 3;

    private String id;
    private String name;
    private double latitude;
    private double longitude;
    private int radius;
    private int transitionType;

    public static Geofence createInstance(JSONObject jsonObject) throws JSONException {
        Geofence inst = new Geofence();
        inst.id = jsonObject.getString("id");
        inst.latitude = jsonObject.getDouble("latitude");
        inst.longitude = jsonObject.getDouble("longitude");
        inst.name = jsonObject.getString("name");
        inst.radius = jsonObject.getInt("radius");
        inst.transitionType = jsonObject.getInt("transitionType");
        return inst;
    }

    private Geofence() {

    }



    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public int getRadius() {
        return radius;
    }

    public int getTransitionType() {
        return transitionType;
    }
}
