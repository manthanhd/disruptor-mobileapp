//
//  AXMSectionController.h
//  axemas
//
//  Copyright (c) 2014 AXANT. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "SectionViewController.h"

@interface AXMSectionController : NSObject

@property (nonatomic, weak) SectionViewController *section;

- (instancetype)initWithSection:(SectionViewController*)section;

- (void)sectionDidLoad;
- (void)sectionWillLoad;

@end
