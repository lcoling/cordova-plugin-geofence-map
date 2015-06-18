package com.visioncritical.cordova.geofenceMap;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Button;
import android.widget.RelativeLayout;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.LOG;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class GeofenceMap extends CordovaPlugin {

    public static final String TAG = "GeofenceMap";

    private Context context = null;
    private Activity cordovaActivity = null;
    private GeofenceMapDialog dialog = null;
    private MapView mapView = null;
    private GoogleMap map = null;
    private RetrieveLocationCommand command = null;



    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        cordovaActivity = this.cordova.getActivity();
        context = this.cordovaActivity.getApplicationContext();
    }

    @Override
    public boolean execute(final String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {

        Log.d(TAG, "GeofenceMap execute action: " + action + " args: "
                + args.toString());

        if (action.equalsIgnoreCase("show")) {
            command = new RetrieveLocationCommand(context) {
                @Override
                protected void executeCommand(Location location) {
                    showGoogleMaps(args, location, callbackContext);
                }
            };
        }

        if (command != null)
            cordova.getThreadPool().execute(command);

        return true;
    }


    private void showGoogleMaps(JSONArray args, final Location currentLocation, final CallbackContext callbackContext) {
        final List<Geofence> geofences = new ArrayList<Geofence>();
        final List<MarkerOptions> markers = new ArrayList<MarkerOptions>();
        final List<CircleOptions> markerRadiusDisplays = new ArrayList<CircleOptions>();

        try {
            JSONArray geofenceArray = args.getJSONArray(1);
            for (int i = 0; i < geofenceArray.length(); i++) {
                Geofence toShow = Geofence.createInstance(geofenceArray.getJSONObject(i));
                geofences.add(toShow);
            }
        } catch (JSONException e) {
            LOG.e(TAG, "Error parsing arguments", e);
            callbackContext.error("Error parsing arguments: " + e.getMessage());
        }

        for (int i = 0; i < geofences.size(); i ++) {
            markers.add(buildMarkerFromGeofence(geofences.get(i)));
            markerRadiusDisplays.add(buildCircleOptionsFromGeofence(geofences.get(i)));
        }

        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                dialog = new GeofenceMapDialog(cordova.getActivity(), android.R.style.Theme_DeviceDefault);
                dialog.getWindow().getAttributes().windowAnimations = android.R.style.Animation_Dialog;
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCancelable(true);

                // Main container layout
                LinearLayout main = new LinearLayout(cordova.getActivity());
                main.setOrientation(LinearLayout.VERTICAL);
                main.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

                // Toolbar layout
                RelativeLayout toolbar = new RelativeLayout(cordova.getActivity());
                toolbar.setBackgroundColor(android.graphics.Color.BLACK);
                toolbar.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, dpToPixels(44)));
                toolbar.setPadding(dpToPixels(2), dpToPixels(2), dpToPixels(2), dpToPixels(2));
                toolbar.setHorizontalGravity(Gravity.RIGHT);
                toolbar.setVerticalGravity(Gravity.TOP);

                // Close/Done button
                Button close = new Button(cordova.getActivity());
                RelativeLayout.LayoutParams closeLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                closeLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                closeLayoutParams.setMargins(dpToPixels(0),dpToPixels(0),dpToPixels(10),dpToPixels(0));
                close.setLayoutParams(closeLayoutParams);
                close.setId(5);
                close.setText("Done");
                close.setGravity(Gravity.CENTER_VERTICAL);
                close.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        done();
                    }
                });
                close.setBackgroundColor(Color.TRANSPARENT);
                close.setTextColor(Color.WHITE);
                close.setTextSize(15.0f);

                // now add the google map
                GoogleMapOptions options = new GoogleMapOptions();
                options.compassEnabled(true);
                options.mapType(GoogleMap.MAP_TYPE_NORMAL);
                options.scrollGesturesEnabled(true);
                options.rotateGesturesEnabled(true);
                options.tiltGesturesEnabled(false);
                options.zoomGesturesEnabled(true);
                options.camera(
                        CameraPosition.fromLatLngZoom(
                                new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                15));

                mapView = new MapView(cordovaActivity, options);
                mapView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                mapView.onCreate(null);
                mapView.onResume();

                map = mapView.getMap();
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
                map.setBuildingsEnabled(true);
                map.setIndoorEnabled(true);
                map.setTrafficEnabled(false);


                for (int i = 0; i < geofences.size(); i ++) {
                    map.addMarker(markers.get(i));
                    map.addCircle(markerRadiusDisplays.get(i));
                }

                toolbar.addView(close);
                main.addView(toolbar);
                main.addView(mapView);

                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(dialog.getWindow().getAttributes());
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.MATCH_PARENT;

                dialog.setContentView(main);
                dialog.show();
                dialog.getWindow().setAttributes(lp);

                callbackContext.success();
            }
        };

        cordovaActivity.runOnUiThread(runnable);
    }

    private MarkerOptions buildMarkerFromGeofence(Geofence geofence) {
        StringBuilder snippetBuilder = new StringBuilder();

        switch (geofence.getTransitionType()) {
            case Geofence.TRANSITION_TYPE_EXIT:
                snippetBuilder.append("Exit");
                break;
            case Geofence.TRANSITION_TYPE_BOTH:
                snippetBuilder.append("Enter and exit");
                break;
            case Geofence.TRANSITION_TYPE_ENTER:
            default:
                snippetBuilder.append("Entry");
                break;
        }

        snippetBuilder.append(" radius: ");
        snippetBuilder.append(geofence.getRadius());
        snippetBuilder.append("m");

        return new MarkerOptions()
                .title(geofence.getName())
                .snippet(snippetBuilder.toString())
                .position(geofence.getCoordinates());
    }

    private CircleOptions buildCircleOptionsFromGeofence(Geofence geofence) {
        return new CircleOptions()
                .radius(geofence.getRadius())
                .center(geofence.getCoordinates())
                .fillColor(0x200000ff)
                .strokeColor(Color.BLACK)
                .strokeWidth(2.0f);
    }
    /**
     * Convert our DIP units to Pixels
     *
     * @return int
     */
    private int dpToPixels(int dipValue) {
        int value = (int) TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP,
                (float) dipValue,
                cordovaActivity.getResources().getDisplayMetrics()
        );

        return value;
    }

    private void done() {
        if (command != null) {
            command.disconnect();
        }
        dialog.dismiss();
    }

    private abstract class RetrieveLocationCommand implements Runnable,
            GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener  {

        private GoogleApiClient googleApiClient = null;

        public RetrieveLocationCommand(Context context) {
            googleApiClient = new GoogleApiClient.Builder(context)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }

        @Override
        public void run() {
            connectToGoogleServices();
        }

        public void disconnect() {
            googleApiClient.disconnect();
        }

        protected abstract void executeCommand(Location currentLocation);

        private void findLocationAndExecuteRunnable() {
            Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            executeCommand(currentLocation);
        }

        private void connectToGoogleServices() {
            if (!googleApiClient.isConnected() || !googleApiClient.isConnecting()) {
                Log.d(TAG, "Connecting location client");
                googleApiClient.connect();
            }
        }

        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            LOG.d(TAG, "Connecting to google services fail - " + connectionResult.toString());
        }

        @Override
        public void onConnectionSuspended(int i) {
            String reason = null;
            if (i == CAUSE_NETWORK_LOST) {
                reason = "Network lost";
            }
            else if (i == CAUSE_SERVICE_DISCONNECTED) {
                reason = "Service disconnected";
            }
            else {
                reason = "Unknown";
            }
            LOG.d(TAG, "Connection suspended: " + reason);
        }

        @Override
        public void onConnected(Bundle arg0) {
            LOG.d(TAG, "Google play services connected");
            findLocationAndExecuteRunnable();
        }
    }
}
