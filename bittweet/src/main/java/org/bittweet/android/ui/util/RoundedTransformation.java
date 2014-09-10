package org.bittweet.android.ui.util;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;

import com.koushikdutta.ion.bitmap.Transform;

// From: https://gist.github.com/aprock/6213395
// enables hardware accelerated rounded corners
// original idea here : http://www.curious-creature.org/2012/12/11/android-recipe-1-image-with-rounded-corners/
public class RoundedTransformation implements Transform {
    private final int radius;
    private final int margin; // dp
    private boolean topLeft;
    private boolean topRight;
    private boolean bottomLeft;
    private boolean bottomRight;

    // radius is corner radii in dp
    // margin is the board in dp
    public RoundedTransformation(final int radius, final int margin,
                                 boolean TL, boolean TR, boolean BL, boolean BR) {
        this.radius = radius;
        this.margin = margin;
        this.topLeft = TL;
        this.topRight = TR;
        this.bottomLeft = BL;
        this.bottomRight = BR;
    }

    @Override
    public Bitmap transform(final Bitmap source) {
        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(new BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));

        int width = source.getWidth();
        int height = source.getHeight();

        Bitmap output = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        canvas.drawRoundRect(new RectF(margin, margin, source.getWidth() - margin, source.getHeight() - margin), radius, radius, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        //draw rectangles over the corners we want to be square
        if (!topLeft){
            canvas.drawRect(0, 0, width/2, height/2, paint);
        }
        if (!topRight){
            canvas.drawRect(width/2, 0, width, height/2, paint);
        }
        if (!bottomLeft){
            canvas.drawRect(0, height/2, width/2, height, paint);
        }
        if (!bottomRight){
            canvas.drawRect(width/2, height/2, width, height, paint);
        }

        if (source != output) {
            source.recycle();
        }

        return output;
    }

    @Override
    public String key() {
        return "rounded";
    }
}
