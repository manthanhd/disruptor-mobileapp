//
//  NavigationSectionsManager.m
//  axemas
//
//  Copyright (c) 2013 AXANT. All rights reserved.
//

#import "NavigationSectionsManager.h"
#import "SectionViewController.h"

@interface SectionsManagerStatus : NSObject

@property (strong, nonatomic) SWRevealViewController *sidebarController;
@property (strong, nonatomic) NSMutableDictionary *sectionControllers;
@end

static SectionsManagerStatus *statusInstance = nil;

@implementation SectionsManagerStatus

- (instancetype)init {
    self = [super init];
    if (self) {
        self.sectionControllers = [[NSMutableDictionary alloc] init];
        self.sidebarController = nil;
    }
    return self;
}

+ (SectionsManagerStatus *)sharedInstance {
    if (statusInstance == nil) {
        statusInstance = [[SectionsManagerStatus alloc] init];
    }
    return statusInstance;
}

@end


@implementation NavigationSectionsManager

+ (UINavigationController*)makeTabController:(NSDictionary*)data {
    SectionViewController *viewController = [SectionViewController createWithData:data];
    UINavigationController *navController = [[UINavigationController alloc] initWithRootViewController:viewController];
    return navController;
}

+ (void)goto:(NSDictionary*)data animated:(BOOL)animated {
    BOOL replaceView = false;
    SectionViewController *subSection = [SectionViewController createWithData:data];
    
    if([data objectForKey:@"stackMaintainedElements"] != nil){
        replaceView = [self popViewsMaintaining: [[data objectForKey:@"stackMaintainedElements"] intValue]];
    }
    if([data objectForKey:@"stackPopElements"] != nil)
        replaceView = [self popViews: [[data objectForKey:@"stackPopElements"] intValue]];
    
    if(replaceView)
        [[NavigationSectionsManager activeNavigationController] setViewControllers:@[subSection] animated:NO];
    else{   //if from sidebar
        [[NavigationSectionsManager activeNavigationController] pushViewController:subSection animated:animated];
    }
}

+ (void)pushController:(UIViewController*)controller animated:(BOOL)animated{
    [[NavigationSectionsManager activeNavigationController] pushViewController:controller animated:animated];
}

+ (UIViewController*)makeApplicationRootController:(NSArray*)tabs {
    return [NavigationSectionsManager makeApplicationRootController:tabs withSidebar:nil];
}

+ (UIViewController*)makeApplicationRootController:(NSArray*)tabs withSidebar:(NSDictionary*)sidebarData {
    UIViewController *mainApplicationController = nil;
    NSArray *requestedTabs = tabs;
    
    if (requestedTabs.count > 1) {
        UITabBarController *tabBarController = [[UITabBarController alloc] init];
        
        NSMutableArray *controllersForTabs = [[NSMutableArray alloc] initWithCapacity:[requestedTabs count]];
        for (NSDictionary *tabData  in requestedTabs) {
            UINavigationController *navController = [NavigationSectionsManager makeTabController:tabData];
            [controllersForTabs addObject:navController];
        }
        
        tabBarController.viewControllers = controllersForTabs;
        mainApplicationController = tabBarController;
    }
    else {
        NSDictionary *rootViewData = [requestedTabs objectAtIndex:0];
        mainApplicationController = [NavigationSectionsManager makeTabController:rootViewData];
    }
    
    if (sidebarData != nil) {
        SWRevealViewController *sidebarControler = [[SWRevealViewController alloc] init];
        [SectionsManagerStatus sharedInstance].sidebarController = sidebarControler;
        
        sidebarControler.rearViewController = [SectionViewController createWithData:sidebarData];
        sidebarControler.frontViewController = mainApplicationController;
        sidebarControler.toggleAnimationType = SWRevealToggleAnimationTypeEaseOut;  // used for iOS6 compatibility
        sidebarControler.rearViewRevealOverdraw = 0;
        
        mainApplicationController = sidebarControler;
    }
    
    return mainApplicationController;
}

+ (UINavigationController*)activeNavigationController {
    UIWindow *mainWindow = [[UIApplication sharedApplication] keyWindow];
    UIViewController *rootController = mainWindow.rootViewController;
    
    if ([rootController isKindOfClass:[SWRevealViewController class]])
        rootController = ((SWRevealViewController*)rootController).frontViewController;
    
    if ([rootController isKindOfClass:[UITabBarController class]]) {
        UITabBarController *tabController = (UITabBarController*)rootController;
        return (UINavigationController*)tabController.selectedViewController;
    }
    else {
        return (UINavigationController*)rootController;
    }
}

+ (UIViewController*)activeController {
    UINavigationController *navController = [NavigationSectionsManager activeNavigationController];
    return navController.topViewController;
}

+ (void)registerController:(Class)controllerClass forRoute:(NSString*)path {
    [SectionsManagerStatus sharedInstance].sectionControllers[path] = controllerClass;
}

+ (Class)getControllerForRoute:(NSString*)path {
    return [SectionsManagerStatus sharedInstance].sectionControllers[path];
}

+ (SWRevealViewController*)activeSidebarController {
    return [SectionsManagerStatus sharedInstance].sidebarController;
}

+ (BOOL) popViews:(NSInteger) viewsToPop{
    /*
     
     NSInteger popToIndex = [viewStack count] - viewsToPop -1;
     if(popToIndex <= -1){
     replaceView = YES;
     popToIndex = 0;
     }
     UIViewController *controller = [viewStack objectAtIndex:popToIndex];
     [[NavigationSectionsManager activeNavigationController] popToViewController:controller animated:NO];
     */
    NSArray *viewStack = [[NavigationSectionsManager activeNavigationController] viewControllers];
    int limit = MIN(MAX(0, viewsToPop), [viewStack count]);
    BOOL replaceView = ([viewStack count] - limit ) < 1;
    for (int i = 0; i < limit; i++)
        [[NavigationSectionsManager activeNavigationController] popViewControllerAnimated:NO];
    return replaceView;
}

+ (BOOL) popViewsMaintaining:(NSInteger) viewsToMaintain{
    /*
     UINavigationController *nav = [NavigationSectionsManager activeNavigationController];
     NSArray *viewStack = [[NavigationSectionsManager activeNavigationController] viewControllers];
     NSInteger popToIndex = MIN(viewsToMaintain -1, nav.viewControllers.count-1);
     if(popToIndex < 0){
     replaceView = YES;
     popToIndex = 0;
     }
     UIViewController *controller = [viewStack objectAtIndex:popToIndex];
     [[NavigationSectionsManager activeNavigationController] popToViewController:controller animated:NO];
     */
    NSArray *viewStack = [[NavigationSectionsManager activeNavigationController] viewControllers];
    BOOL replaceView = viewsToMaintain < 1;
    int count = [viewStack count];
    while (count > MAX(1, viewsToMaintain)){
        [[NavigationSectionsManager activeNavigationController] popViewControllerAnimated:NO];
        count--;
    }
    return replaceView;
}

@end
