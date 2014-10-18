//
//  HomeSectionController.m
//  axemas
//
//  Created by Alessandro Molina on 4/10/14.
//  Copyright (c) 2014 AXANT. All rights reserved.
//

#import "HomeSectionController.h"
#import "WebViewJavascriptBridge.h"
#import "NavigationSectionsManager.h"
#import "MapViewController.h"

@implementation HomeSectionController

- (void)sectionWillLoad {
    [self.section.bridge registerHandler:@"openMap" handler:^(id data, WVJBResponseCallback responseCallback) {
        [NavigationSectionsManager pushController:[[MapViewController alloc] init] animated:YES];
        
        if (responseCallback) {
            responseCallback(nil);
        }
    }];
    
    [self.section.bridge registerHandler:@"openNativeController" handler:^(id data, WVJBResponseCallback responseCallback) {
        
        NSDictionary * datum  = @{@"url":@"www/index.html",
                                  @"title":@"Home"};
        [NavigationSectionsManager goto: datum animated:YES];
        
        if (responseCallback) {
            responseCallback(nil);
        }
    }];
}

@end
