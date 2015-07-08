package com.visioncritical.cordova.geofenceMap;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by lcoling on 2015-06-13.
 */
public class Geofence {

    private static final String RECALC_ID = "00000000-0000-0000-0000-000000000000";

    public static final int TRANSITION_TYPE_ENTER = 1;
    public static final int TRANSITION_TYPE_EXIT = 2;
    public static final int TRANSITION_TYPE_BOTH = 3;

    private String id;
    private String name;
    private int radius;
    private int transitionType;
    private LatLng coordinates;

    public static Geofence createInstance(JSONObject jsonObject) throws JSONException {
        Geofence inst = new Geofence();
        inst.id = jsonObject.getString("id");
        double latitude = jsonObject.getDouble("latitude");
        double longitude = jsonObject.getDouble("longitude");
        inst.name = jsonObject.getString("title");
        inst.radius = jsonObject.getInt("radius");
        inst.transitionType = jsonObject.getInt("transitionType");
        inst.coordinates = new LatLng(latitude, longitude);
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

    public int getRadius() {
        return radius;
    }

    public int getTransitionType() {
        return transitionType;
    }

    public LatLng getCoordinates() { return coordinates; }

    public boolean isRecalcGeofence() {
        return RECALC_ID.equalsIgnoreCase(id);
    }
}
