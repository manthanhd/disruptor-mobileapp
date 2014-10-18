//
//  AXOImageTools.h
//  AXO
//
//  Created by Alessandro Molina on 3/13/13.
//  Copyright (c) 2013 AXANT. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface AXOImageTools : NSObject

+ (void)asyncDownloadImageForCell:(UITableViewCell *)cell withPlaceHolder:(UIImage *)placeholder fromUrl:(NSURL *)url;
+ (void)asyncDownloadImageForCell:(UITableViewCell *)cell withPlaceHolder:(UIImage *)placeholder withCornerRadius:(CGFloat)cornerRadius fromUrl:(NSURL *)url;
+ (void)asyncDownloadImageForCell:(UITableViewCell *)cell withPlaceHolder:(UIImage *)placeholder withCornerRadius:(CGFloat)cornerRadius withAspectFill:(BOOL)aspectFill fromUrl:(NSURL *)url;

+ (UIImage *)scaleImage:(UIImage *)image toSize:(CGSize)newSize;
+ (UIImage *)scaleImageKeepAspect:(UIImage *)image toSize:(CGSize)newSize;
+ (UIImage *)scaleImageWithAspectFill:(UIImage *)image toSize:(CGSize)newSize;

+ (UIColor*)RGBcolorWithRed:(int)red green:(int)green blue:(int)blue alpha:(CGFloat)alpha;
@end
