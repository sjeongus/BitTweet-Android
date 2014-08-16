package org.bittweet.android.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;

/**
 * Created by soomin on 2014/08/16.
 */
public class Blur {

    // Create a downsampled bitmap from view
    public static Bitmap loadBitmapFromView(View v) {
        float scale = 1f / 5;
        v.measure(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
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
                Bitmap scaled = Bitmap.createScaledBitmap(copy, orig.getWidth(), orig.getHeight(), false);
                Bitmap cropped = Bitmap.createBitmap(scaled, 0, orig.getHeight() - image.getHeight(), orig.getWidth(), image.getHeight());

                image.setImageBitmap(cropped);

                image.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
    }
}
