package com.tyc129.nfcmap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Created by Code on 2017/10/27 0027.
 *
 * @author 谈永成
 * @version 1.0
 */
public class Utils {
    public static Bitmap readBitmapFitBound(Context context, int resId, int width, int height) {
        if (context == null || width <= 0 || height <= 0)
            return null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(context.getResources(), resId, options);
        int inSampleSize = 1;
        if (width < options.outWidth || height < options.outHeight) {
            int heightRatio = Math.round((float) options.outHeight / (float) height);
            int widthRatio = Math.round((float) options.outWidth / (float) width);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        options.inJustDecodeBounds = false;
        options.inSampleSize = inSampleSize;
        return BitmapFactory.decodeResource(context.getResources(), resId, options);
    }
}
