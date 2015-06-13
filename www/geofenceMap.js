
var exec = require('cordova/exec'),
    channel = require('cordova/channel'),
    geofenceMap,
    GeofenceMap = function () {

    };

GeofenceMap.prototype.show = function (title, geofences, success, error) {
	geofences = geofences || [];
	title = title || 'Community';
	
    if (!Array.isArray(geofences)) {
        geofences = [geofences];
    }
	
    exec(success, error, "GeofenceMap", "show", [title, geofences]);
};

// Called after 'deviceready' event
channel.deviceready.subscribe(function () {
    // Device is ready now, the listeners are registered
    // and all queued events can be executed.
    exec(null, null, 'GeofenceMap', 'deviceready', []);
});

geofenceMap = new GeofenceMap();
module.exports = geofenceMap;
