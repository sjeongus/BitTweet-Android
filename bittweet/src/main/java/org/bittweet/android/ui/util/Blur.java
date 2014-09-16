package org.bittweet.android.ui.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.os.Build;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import static org.bittweet.android.ui.util.ImageUtils.scaleCenterCrop;

/**
 * Created by soomin on 2014/08/16.
 */
public class Blur {

    // Create a downsampled bitmap from view
    public static Bitmap loadBitmapFromView(View v) {
        float scale = 1f / 5;
        v.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int bmpWidth = (int)(v.getMeasuredWidth() * scale);
        int bmpHeight = (int)(v.getMeasuredHeight() * scale);
        Bitmap b = Bitmap.createBitmap(bmpWidth, bmpHeight, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        c.scale(scale, scale);
        v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
        v.draw(c);
        return b;
    }

    // Blur bitmap and set to an imageview
    public static void blurBitmap(Bitmap bitmap, ImageView original, ImageView view, Context context) {
        final ImageView image = view;
        final ImageView orig = original;

        //Let's create an empty bitmap with the same size of the bitmap we want to blur
        Bitmap outBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);

        //Instantiate a new Renderscript
        RenderScript rs = RenderScript.create(context);

        //Create an Intrinsic Blur Script using the Renderscript
        ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));

        //Create the Allocations (in/out) with the Renderscript and the in/out bitmaps
        Allocation allIn = Allocation.createFromBitmap(rs, bitmap);
        //Allocation allOut = Allocation.createFromBitmap(rs, outBitmap);
        Allocation allOut = Allocation.createTyped(rs, allIn.getType());

        //Set the radius of the blur
        blurScript.setRadius(25.f);

        //Perform the Renderscript
        blurScript.setInput(allIn);
        blurScript.forEach(allOut);

        //Copy the final bitmap created by the out Allocation to the outBitmap
        allOut.copyTo(outBitmap);

        //recycle the original bitmap
        bitmap.recycle();

        //After finishing everything, we destroy the Renderscript.
        rs.destroy();

        //image.setImageBitmap(outBitmap);

        final Bitmap copy = outBitmap;

        image.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Bitmap scaled = scaleCenterCrop(copy, orig.getWidth(), orig.getHeight());
                Bitmap cropped = Bitmap.createBitmap(scaled, 0, orig.getHeight() - image.getHeight(), orig.getWidth(), image.getHeight());

                image.setImageBitmap(cropped);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    image.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                   image.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });
    }
}
