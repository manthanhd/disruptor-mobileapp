//
//  AXOImageTools.m
//  AXO
//
//  Created by Alessandro Molina on 3/13/13.
//  Copyright (c) 2013 AXANT. All rights reserved.
//

#import "AXOImageTools.h"
#import <QuartzCore/QuartzCore.h>
#import <SDWebImage/SDWebImageManager.h>
#import <objc/runtime.h>

static char CellImageDownloaderDelegate_key;

@interface CellImageDownloaderDelegate : NSObject <SDWebImageManagerDelegate> {
    CGSize imageSize;
    BOOL fill;
}

@property (nonatomic, weak) UITableViewCell *cell;

- (void)setSize:(CGSize)size;
- (void)setAspectFill:(BOOL)aspectFill;
+ (CellImageDownloaderDelegate*)getForCell:(UITableViewCell *)cell;
- (void)cancelCurrentImageLoad;
- (void)webImageManager:(SDWebImageManager *)imageManager didProgressWithPartialImage:(UIImage *)image forURL:(NSURL *)url;
- (void)webImageManager:(SDWebImageManager *)imageManager didFinishWithImage:(UIImage *)image;
@end

@implementation CellImageDownloaderDelegate

- (void)setSize:(CGSize)size {
    self->imageSize = size;
}

- (void)setAspectFill:(BOOL)aspectFill{
    self->fill=YES;
}

- (void)cancelCurrentImageLoad
{
    [[SDWebImageManager sharedManager] cancelForDelegate:self];
}

- (void)webImageManager:(SDWebImageManager *)imageManager didProgressWithPartialImage:(UIImage *)image forURL:(NSURL *)url
{
    self.cell.imageView.image = [AXOImageTools scaleImageKeepAspect:image toSize:self->imageSize];
    [self.cell.imageView setNeedsLayout];
}

- (void)webImageManager:(SDWebImageManager *)imageManager didFinishWithImage:(UIImage *)image
{
    if (self->fill)
        self.cell.imageView.image = [AXOImageTools scaleImageWithAspectFill:image toSize:self->imageSize];
    else
        self.cell.imageView.image = [AXOImageTools scaleImageKeepAspect:image toSize:self->imageSize];
    [self.cell.imageView setNeedsLayout];
}

+ (CellImageDownloaderDelegate*)getForCell:(UITableViewCell *)cell {
    CellImageDownloaderDelegate *delegate = objc_getAssociatedObject(cell, &CellImageDownloaderDelegate_key);
    if (delegate == nil) {
        delegate = [CellImageDownloaderDelegate new];
        delegate.cell = cell;
        delegate->fill = NO;
        objc_setAssociatedObject(cell, &CellImageDownloaderDelegate_key, delegate, OBJC_ASSOCIATION_RETAIN_NONATOMIC);
    }
    return delegate;
}

@end

@implementation AXOImageTools

+ (UIImage *)scaleImage:(UIImage *)image toSize:(CGSize)newSize {
    UIGraphicsBeginImageContextWithOptions(newSize, NO, 0.0f);
    [image drawInRect:CGRectMake(0, 0, newSize.width, newSize.height)];
    UIImage *newImage = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    return newImage;
}

+ (UIImage *)scaleImageKeepAspect:(UIImage *)image toSize:(CGSize)newSize {
    CGRect image_rect;
    
    float ratio = image.size.width/image.size.height;
    if (ratio >= 1) {
        float image_height = newSize.width / ratio;
        float image_center_y = (newSize.height - image_height) / 2;
        image_rect = CGRectMake(0, image_center_y, newSize.width, image_height);
    }
    else {
        float image_width = newSize.width * ratio;
        float image_center_x = (newSize.width - image_width) / 2;
        image_rect = CGRectMake(image_center_x, 0, image_width, newSize.height);
    }
    
    UIGraphicsBeginImageContextWithOptions(newSize, NO, 0.0f);
    [image drawInRect:image_rect];
    UIImage *newImage = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    return newImage;
}

+ (UIImage *)scaleImageWithAspectFill:(UIImage *)image toSize:(CGSize)newSize {
    CGRect image_rect;
    
    float ratio = image.size.width/image.size.height;
    if (ratio <= 1) {
        float image_height = newSize.width / ratio;
        float image_center_y = (newSize.height - image_height) / 2;
        image_rect = CGRectMake(0, image_center_y, newSize.width, image_height);
    }
    else {
        float image_width = newSize.width * ratio;
        float image_center_x = (newSize.width - image_width) / 2;
        image_rect = CGRectMake(image_center_x, 0, image_width, newSize.height);
    }
    
    UIGraphicsBeginImageContextWithOptions(newSize, YES, 0.0f);
    [image drawInRect:image_rect];
    UIImage *newImage = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    return newImage;
}

+ (void)asyncDownloadImageForCell:(UITableViewCell *)cell withPlaceHolder:(UIImage *)placeholder fromUrl:(NSURL *)url {
    SDWebImageManager *manager = [SDWebImageManager sharedManager];
    CellImageDownloaderDelegate *downloadDelegate = [CellImageDownloaderDelegate getForCell:cell];
    
    [downloadDelegate cancelCurrentImageLoad];
    cell.imageView.image = placeholder;
    if (url) {
        [downloadDelegate setSize:placeholder.size];
        [manager downloadWithURL:url delegate:downloadDelegate options:0 success:nil failure:nil];
    }
}

+ (void)asyncDownloadImageForCell:(UITableViewCell *)cell withPlaceHolder:(UIImage *)placeholder withCornerRadius:(CGFloat)cornerRadius fromUrl:(NSURL *)url {
    SDWebImageManager *manager = [SDWebImageManager sharedManager];
    CellImageDownloaderDelegate *downloadDelegate = [CellImageDownloaderDelegate getForCell:cell];
    
    [downloadDelegate cancelCurrentImageLoad];
    cell.imageView.image = placeholder;
    cell.imageView.layer.cornerRadius = cornerRadius;
    cell.imageView.layer.masksToBounds = YES;
    if (url) {
        [downloadDelegate setSize:placeholder.size];
        [manager downloadWithURL:url delegate:downloadDelegate options:0 success:nil failure:nil];
    }
}

+ (void)asyncDownloadImageForCell:(UITableViewCell *)cell withPlaceHolder:(UIImage *)placeholder withCornerRadius:(CGFloat)cornerRadius withAspectFill:(BOOL)aspectFill fromUrl:(NSURL *)url {
    SDWebImageManager *manager = [SDWebImageManager sharedManager];
    CellImageDownloaderDelegate *downloadDelegate = [CellImageDownloaderDelegate getForCell:cell];
    
    [downloadDelegate cancelCurrentImageLoad];
    cell.imageView.image = placeholder;
    cell.imageView.layer.cornerRadius = cornerRadius;
    cell.imageView.layer.masksToBounds = YES;
    if(aspectFill)
        [downloadDelegate setAspectFill:aspectFill];
    if (url) {
        [downloadDelegate setSize:placeholder.size];
        [manager downloadWithURL:url delegate:downloadDelegate options:0 success:nil failure:nil];
    }
}

+ (UIColor*)RGBcolorWithRed:(int)red green:(int)green blue:(int)blue alpha:(CGFloat)alpha {
    return [UIColor colorWithRed:red/255.0f green:green/255.0f blue:blue/255.0f alpha:alpha];
}


@end
