package com.visioncritical.cordova.geofenceMap;

import android.app.Dialog;
import android.content.Context;

/**
 * Created by lcoling on 2015-06-13.
 */
public class GeofenceMapDialog extends Dialog {
    private Context context = null;

    public GeofenceMapDialog(Context context, int theme) {
        super(context, theme);
        this.context = context;
    }

    public void onBackPressed() {
        this.dismiss();
    }
}
