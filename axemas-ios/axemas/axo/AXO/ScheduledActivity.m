//
//  AXO.m
//  AXO
//
//  Created by Alessandro Molina on 2/7/13.
//  Copyright (c) 2013 AXANT. All rights reserved.
//

#import "ScheduledActivity.h"
#import "AXO.h"
#import "MBProgressHUD.h"

@interface AXOScheduledActivity() {
    BOOL currentlyPerforming;
    BOOL withSpinner;
    AXOScheduledActivityCallback _action;
    AXOScheduledActivityCallback _onComplete;
}

@property (readwrite, strong, nonatomic) NSTimer *timer;

- (void)_runActivity;
- (void)_startPerforming;
- (void)_endPerforming;

@end

@implementation AXOScheduledActivity

- (id)init {
    if ( self = [super init] ) {
        self->withSpinner = NO;
        self->currentlyPerforming = NO;
        self.timer = nil;
        self->_action = nil;
        self->_onComplete = nil;
        [AXO attributize:self];
    }
    return self;
}

- (void)registerOnComplete:(AXOScheduledActivityCallback)callback {
    self->_onComplete = callback;
}

- (void)registerAction:(AXOScheduledActivityCallback)callback {
    self->_action = callback;
}

- (void)onComplete {
    if (self->_onComplete)
        self->_onComplete(self);
}

- (void)performAction {
    if (!self->_action)
        [NSException raise:NSInternalInconsistencyException
                    format:@"You must register at least an action to perform"];
    
    self->_action(self);
}


- (void)_startPerforming {
    self->currentlyPerforming = YES;
    if (self->withSpinner) {
        [MBProgressHUD hideHUDForView:[AXO appWindow] animated:NO];
        [MBProgressHUD showHUDAddedTo:[AXO appWindow] animated:YES];
    }
}

- (void)_endPerforming {
    if (self->withSpinner) {
        [MBProgressHUD hideHUDForView:[AXO appWindow] animated:YES];
    }
    self->currentlyPerforming = NO;
}

- (void)_runActivity {
    if (self->currentlyPerforming)
        return;
    
    [self _startPerforming];
    dispatch_async(dispatch_get_global_queue( DISPATCH_QUEUE_PRIORITY_LOW, 0), ^{
        [self performAction];
        dispatch_async(dispatch_get_main_queue(), ^{
            [self onComplete];
            [self _endPerforming];
        });
    });
}

- (void)stop {
    if (self.timer) {
        [self.timer invalidate];
        self.timer = nil;
    }
}

- (void)enableSpinner {
    self->withSpinner = YES;
}

- (void)fireAndForget {
    [self _runActivity];
}

- (void)schedule:(NSTimeInterval)seconds {
    self->currentlyPerforming = NO;
    self.timer = [NSTimer scheduledTimerWithTimeInterval:seconds
                                                  target:self
                                                selector:@selector(_runActivity)
                                                userInfo:nil
                                                 repeats:YES];
}

@end
