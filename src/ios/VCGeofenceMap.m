//
//  VCGeofenceMap.m
//  location-test-app
//
//  Created by lcoling on 2015-06-12.
//
//

#import "VCGeofenceMap.h"

#pragma mark VCGeofenceMap

@implementation VCGeofenceMap

@synthesize geofenceMapViewController;

- (void)pluginInitialize
{
    
}

- (void)deviceready:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)show:(CDVInvokedUrlCommand*)command
{
    dispatch_async(dispatch_get_global_queue( DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^(void){
        
        NSString *title = command.arguments[0];
        
        NSArray *geofences = [self buildGeofenceLocations:command.arguments[1]];
        
        dispatch_async(dispatch_get_main_queue(), ^{
            CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
            
            self.geofenceMapViewController = [[VCGeofenceMapViewController alloc] initWithTitle:title geofences:geofences];
            self.geofenceMapViewController.modalTransitionStyle = UIModalTransitionStyleCoverVertical;
            self.geofenceMapViewController.modalPresentationStyle = UIModalPresentationFullScreen;
            
            VCGeofenceMapNavigationController* nav = [[VCGeofenceMapNavigationController alloc] initWithRootViewController:self.geofenceMapViewController];
            nav.navigationBarHidden = NO;
            nav.navigationItem.hidesBackButton = YES;
            
            [self.viewController presentViewController:nav animated:YES completion:nil];
        });
    });
}

- (NSArray *)buildGeofenceLocations:(NSArray *)jsonGeofences
{
    NSMutableArray *geofenceLocations = [NSMutableArray array];
    
    for (NSDictionary *geofence in jsonGeofences)
        [geofenceLocations addObject:[[VCGeofenceLocation alloc] initWithGeofence:geofence]];
    
    return geofenceLocations;
}

@end


#pragma mark VCGeofenceLocation

@implementation VCGeofenceLocation

@synthesize coordinate = _coordinate;
@synthesize title = _title;
@synthesize subtitle = _subtitle;
@synthesize radius = _radius;
@synthesize locationId = _locationId;
@synthesize transitionType = _transitionType;

- (id)initWithGeofence:(NSDictionary *)geofence
{
    self = [super init];
    if (self != nil)
    {
        id locationId = [geofence objectForKey:@"id"];
        
        if ([locationId isKindOfClass:[NSNumber class]])
        {
            NSNumber *numberId = locationId;
            _locationId = [numberId stringValue];
        }
        else if ([locationId isKindOfClass:[NSString class]])
        {
            _locationId = locationId;
        }
        
        _locationId = [geofence objectForKey:@"id"];
        _title = [geofence objectForKey:@"title"];
        
        id radius = [geofence objectForKey:@"radius"];
        _radius = radius && [radius isKindOfClass:NSNumber.class] ? radius : [NSNumber numberWithInteger:0];
        
        id transitionType = [geofence objectForKey:@"transitionType"];
        _transitionType = transitionType && [transitionType isKindOfClass:NSNumber.class] ? transitionType : [NSNumber numberWithInteger:0];
        
        _subtitle = [self buildSubtitle];
        
        NSNumber *latitude = [geofence objectForKey:@"latitude"];
        NSNumber *longitude = [geofence objectForKey:@"longitude"];
        _coordinate = CLLocationCoordinate2DMake(latitude.floatValue, longitude.floatValue);
        
    }
    return self;
}

- (NSString *)buildSubtitle
{
    NSString *subtitle = nil;
    
    NSString *transitionTypeText = nil;
    
    switch (_transitionType.integerValue)
    {
        case 2:
            transitionTypeText = @"Exit";
            break;
        case 3:
            transitionTypeText = @"Enter and exit";
            break;
        case 1:
        default:
            transitionTypeText = @"Enter";
            break;
    }
    
    subtitle = [NSString stringWithFormat:@"%@ radius: %@m", transitionTypeText, _radius, nil];
    
    return subtitle;
}

@end

#pragma mark VCGeofenceMapViewController

@implementation VCGeofenceMapViewController

@synthesize mapView;
@synthesize closeButton;
@synthesize trackUserButton;

- (id)initWithTitle:(NSString *)titleText geofences:(NSArray *)geofences
{
    _recalcGeofenceId = @"00000000-0000-0000-0000-000000000000";
    
    if (geofences == nil)
    {
        NSLog(@"GeofenceMapPlugin - VCGeofenceMapViewController - initWithTitle:closeButtonText:geofences: - no geofences, no map");
        return nil;
    }
    
    self = [super init];
    if (self != nil)
    {
        _titleText = titleText != nil ? [titleText copy] : @"Community";
        _geofences = [NSArray arrayWithArray:geofences];
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    [self createViews];
}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
}

- (IBAction)close:(id)sender
{
    dispatch_async(dispatch_get_main_queue(), ^{
        if ([self respondsToSelector:@selector(presentingViewController)]) {
            [[self presentingViewController] dismissViewControllerAnimated:YES completion:nil];
        } else {
            [[self parentViewController] dismissViewControllerAnimated:YES completion:nil];
        }
    });
}


- (void)createViews
{
    [self.navigationItem setTitle:_titleText];
    
    self.closeButton = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemDone target:self action:@selector(close:)];
    self.closeButton.enabled = YES;
    [self.navigationItem setRightBarButtonItem:self.closeButton];
    
    self.mapView = [[MKMapView alloc] initWithFrame:self.view.bounds];
    self.mapView.delegate = self;
    self.mapView.scrollEnabled = YES;
    self.mapView.zoomEnabled = YES;
    self.mapView.showsPointsOfInterest = YES;
    self.mapView.showsBuildings = YES;
    self.mapView.showsUserLocation = YES;
    [self.mapView setUserTrackingMode:MKUserTrackingModeFollow animated:NO];
    [self.mapView setRegion:MKCoordinateRegionMakeWithDistance(self.mapView.userLocation.coordinate, 500, 500) animated:NO];
    [self addGeofencesToMapView];
    
    self.trackUserButton = [[MKUserTrackingBarButtonItem alloc] initWithMapView:self.mapView];
    self.trackUserButton.enabled = YES;
    [self.navigationItem setLeftBarButtonItem:self.trackUserButton];
    
    [self.view addSubview:self.mapView];
}

- (void)addGeofencesToMapView
{
    for (VCGeofenceLocation *geofence in _geofences) {
        
        if ([_recalcGeofenceId isEqualToString:geofence.locationId]) {
            _recalcGeofence = geofence;
        }
        
        [self.mapView addAnnotation:geofence];
        
        MKCircle *fenceCircle = [MKCircle circleWithCenterCoordinate:geofence.coordinate radius:geofence.radius.integerValue];
        [self.mapView addOverlay:fenceCircle];
    }
}

- (MKOverlayRenderer *)mapView:(MKMapView *)mapView rendererForOverlay:(id<MKOverlay>)overlay
{
    BOOL isRecalcGeofence = NO;
    
    if (_recalcGeofence) {
        MKCircle *overlayCircle = overlay;
        isRecalcGeofence = (_recalcGeofence.coordinate.latitude == overlayCircle.coordinate.latitude) &&
            (_recalcGeofence.coordinate.longitude == overlayCircle.coordinate.longitude) &&
            (_recalcGeofence.radius.doubleValue == overlayCircle.radius);
    }
    
    MKCircleRenderer *circleRenderer = [[MKCircleRenderer alloc] initWithOverlay:overlay];
    circleRenderer.strokeColor = [UIColor blackColor];
    circleRenderer.lineWidth = 1.0f;
    circleRenderer.alpha = 0.5;
    circleRenderer.fillColor = isRecalcGeofence ?
        [UIColor colorWithRed:0/255.0f green:255.0f/255.0f blue:0/255.0f alpha:0.1] :
        [UIColor colorWithRed:0/255.0f green:0/255.0f blue:255.0f/255.0f alpha:0.2];
    
    return circleRenderer;
}


- (BOOL)shouldAutorotate {
    return NO;
}

- (NSUInteger)supportedInterfaceOrientations {
    return UIInterfaceOrientationMaskPortrait;
}

- (UIInterfaceOrientation)preferredInterfaceOrientationForPresentation {
    return UIInterfaceOrientationPortrait;
}

@end

#pragma mark VCGeofenceMapNavigationController

@implementation VCGeofenceMapNavigationController

- (BOOL)shouldAutorotate {
    return NO;
}

- (NSUInteger)supportedInterfaceOrientations {
    return UIInterfaceOrientationMaskPortrait;
}

- (UIInterfaceOrientation)preferredInterfaceOrientationForPresentation {
    return UIInterfaceOrientationPortrait;
}

@end