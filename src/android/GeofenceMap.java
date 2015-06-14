package com.visioncritical.cordova.geofenceMap;

import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
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

public class GeofenceMap extends CordovaPlugin {

    public static final String TAG = "";

    private Context context = null;
    private GeofenceMapDialog dialog = null;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        context = this.cordova.getActivity().getApplicationContext();
    }

    @Override
    public boolean execute(final String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {

        Log.d(TAG, "GeofenceMap execute action: " + action + " args: "
                + args.toString());

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (action.equalsIgnoreCase("show")) {
                    showGoogleMaps(args, callbackContext);
                }
            }
        };

        cordova.getActivity().runOnUiThread(runnable);

        return true;
    }

    private void showGoogleMaps(JSONArray args, CallbackContext callbackContext) {
        try {
            String title = args.getString(0);
            JSONArray geofenceArray = args.getJSONArray(1);

            List<Geofence> geofences = new ArrayList<Geofence>();

            for (int i = 0; i < geofenceArray.length(); i++) {
                Geofence toShow = Geofence.createInstance(geofenceArray.getJSONObject(i));
                geofences.add(toShow);
            }

            this.dialog = new GeofenceMapDialog(cordova.getActivity(), android.R.style.Theme_DeviceDefault);
            this.dialog.getWindow().getAttributes().windowAnimations = android.R.style.Animation_Dialog;
            this.dialog.setCancelable(true);

            // Main container layout
            LinearLayout main = new LinearLayout(cordova.getActivity());
            main.setOrientation(LinearLayout.VERTICAL);

            // Toolbar layout
            RelativeLayout toolbar = new RelativeLayout(cordova.getActivity());
            toolbar.setBackgroundColor(android.graphics.Color.LTGRAY);
            toolbar.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, this.dpToPixels(44)));
            toolbar.setPadding(this.dpToPixels(2), this.dpToPixels(2), this.dpToPixels(2), this.dpToPixels(2));
            toolbar.setHorizontalGravity(Gravity.RIGHT);
            toolbar.setVerticalGravity(Gravity.TOP);

            // Close/Done button
            Button close = new Button(cordova.getActivity());
            RelativeLayout.LayoutParams closeLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            closeLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            close.setLayoutParams(closeLayoutParams);
            close.setId(5);
            close.setText("Done");
            close.setGravity(Gravity.CENTER_VERTICAL);
            close.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            // now add the google map
            toolbar.addView(close);
            main.addView(toolbar);

            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;

            dialog.setContentView(main);
            dialog.show();
            dialog.getWindow().setAttributes(lp);

            callbackContext.success();
        } catch (JSONException e) {
            LOG.e(TAG, "Error parsing arguments", e);
            callbackContext.error("Error parsing arguments: " + e.getMessage());
        }
    }


    /**
     * Convert our DIP units to Pixels
     *
     * @return int
     */
    private int dpToPixels(int dipValue) {
        int value = (int) TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP,
                (float) dipValue,
                cordova.getActivity().getResources().getDisplayMetrics()
        );

        return value;
    }
}
