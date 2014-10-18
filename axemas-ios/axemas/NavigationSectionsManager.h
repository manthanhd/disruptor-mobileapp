//
//  NavigationSectionsManager.h
//  axemas
//
//  Copyright (c) 2013 AXANT. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "SWRevealViewController.h"

@interface NavigationSectionsManager : NSObject

+ (UINavigationController*)makeTabController:(NSDictionary*)data;
+ (UIViewController*)makeApplicationRootController:(NSArray*)tabs;
+ (UIViewController*)makeApplicationRootController:(NSArray*)tabs withSidebar:(NSDictionary*)sidebarData;
+ (UINavigationController*)activeNavigationController;
+ (UIViewController*)activeController;
+ (SWRevealViewController*)activeSidebarController;
+ (void)goto:(NSDictionary*)data animated:(BOOL)animated;
+ (void)pushController:(UIViewController*)controller animated:(BOOL)animated;

+ (void)registerController:(Class)controllerClass forRoute:(NSString*)path;
+ (Class)getControllerForRoute:(NSString*)path;
    
@end
