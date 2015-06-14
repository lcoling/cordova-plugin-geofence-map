//
//  VCGeofenceMap.h
//  location-test-app
//
//  Created by lcoling on 2015-06-12.
//
//
#import <UIKit/UIKit.h>
#import <Cordova/CDVPlugin.h>
#import <MapKit/MapKit.h>
#import <Cordova/CDVInvokedUrlCommand.h>


@interface VCGeofenceLocation : NSObject<MKAnnotation>

@property (nonatomic, strong) NSString *locationId;
@property (nonatomic, strong) NSNumber *radius;
@property (nonatomic, strong) NSNumber *transitionType;

- (id)initWithGeofence:(NSDictionary *)geofence;

@end


@class VCGeofenceMapViewController;

@interface VCGeofenceMap : CDVPlugin

@property (nonatomic, strong) VCGeofenceMapViewController *geofenceMapViewController;

@end


@interface VCGeofenceMapViewController : UIViewController<MKMapViewDelegate>
{
    NSString *_titleText;
    NSArray *_geofences;
}

@property (nonatomic, strong) IBOutlet MKMapView *mapView;
@property (nonatomic, strong) IBOutlet UIBarButtonItem *closeButton;
@property (nonatomic, strong) IBOutlet MKUserTrackingBarButtonItem *trackUserButton;
- (IBAction)close:(id)sender;

- (id)initWithTitle:(NSString *)titleText geofences:(NSArray *)geofences;

@end
